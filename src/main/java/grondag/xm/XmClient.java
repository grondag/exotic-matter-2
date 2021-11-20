/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import io.vram.frex.api.renderloop.BlockOutlineListener;
import io.vram.frex.api.renderloop.RenderReloadListener;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.collision.CollisionDispatcherImpl;
import grondag.xm.dispatch.XmDispatcher;
import grondag.xm.dispatch.XmVariantProvider;
import grondag.xm.mesh.helper.PolyTransformImpl;
import grondag.xm.modelstate.AbstractPrimitiveModelState;
import grondag.xm.network.Packets;
import grondag.xm.paint.XmPaintRegistryImpl;
import grondag.xm.primitive.ModelPrimitiveRegistryImpl;
import grondag.xm.render.OutlineRenderer;
import grondag.xm.texture.XmTexturesImpl;

// WIP: remove fabric deps
@Internal
public class XmClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		XmTexturesImpl.init();
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(r -> new XmVariantProvider());
		RenderReloadListener.register(XmClient::invalidate);
		Packets.initializeClient();
		AbstractPrimitiveModelState.useClientHandler();
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(XmPaintRegistryImpl.INSTANCE);

		SidedHelper.RENDER_LAYER_REMAPPER = (b, s) -> BlockRenderLayerMap.INSTANCE.putBlock(b, ItemBlockRenderTypes.getChunkRenderType(s));
		SidedHelper.RENDER_LAYER_REMAPS.forEach(SidedHelper.RENDER_LAYER_REMAPPER);
		SidedHelper.RENDER_LAYER_REMAPS.clear();

		BlockOutlineListener.register((ctx, btx) -> {
			final ClientLevel world = ctx.world();
			final BlockPos blockPos = btx.blockPos();
			final BlockState blockState = btx.blockState();
			final ModelState modelState = XmBlockState.modelState(blockState, world, blockPos, true);

			if (modelState != null && !XmConfig.debugCollisionBoxes) {
				final Vec3 cameraPos = ctx.camera().getPosition();

				OutlineRenderer.drawModelOutline(
					ctx.poseStack(),
					ctx.consumers().getBuffer(RenderType.lines()),
					modelState,
					blockPos.getX() - cameraPos.x,
					blockPos.getY() - cameraPos.y,
					blockPos.getZ() - cameraPos.z,
					0.0F, 0.0F, 0.0F, 0.4f
				);

				return false;
			}

			return true;
		});
	}

	public static void invalidate() {
		PolyTransformImpl.invalidateCache();
		XmDispatcher.INSTANCE.clear();
		CollisionDispatcherImpl.clear();
		ModelPrimitiveRegistryImpl.INSTANCE.invalidateCache();
	}
}
