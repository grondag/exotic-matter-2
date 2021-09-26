/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm;

import io.vram.frex.api.event.RenderReloadListener;
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
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

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

		SidedHelper.RENDER_LAYER_REMAPPER  = (b, s) -> BlockRenderLayerMap.INSTANCE.putBlock(b, ItemBlockRenderTypes.getChunkRenderType(s));
		SidedHelper.RENDER_LAYER_REMAPS.forEach(SidedHelper.RENDER_LAYER_REMAPPER);
		SidedHelper.RENDER_LAYER_REMAPS.clear();

		WorldRenderEvents.BLOCK_OUTLINE.register((ctx, btx) -> {
			final ClientLevel world = ctx.world();
			final BlockPos blockPos = btx.blockPos();
            final BlockState blockState = btx.blockState();
			final ModelState modelState = XmBlockState.modelState(blockState, world, blockPos, true);

			if(modelState != null && !XmConfig.debugCollisionBoxes) {
				final Vec3 cameraPos = ctx.camera().getPosition();

				OutlineRenderer.drawModelOutline(
					ctx.matrixStack(),
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
