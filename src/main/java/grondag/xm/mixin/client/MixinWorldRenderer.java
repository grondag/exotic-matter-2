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
package grondag.xm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import grondag.xm.XmConfig;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.render.OutlineRenderer;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@Shadow private ClientWorld world;

	@Inject(method = "drawBlockOutline", at = @At(value = "HEAD"), cancellable = true, require = 1)
	private void onDrawBlockOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double x, double y, double z, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		final ModelState modelState = XmBlockState.modelState(blockState, world, blockPos, true);

		if(modelState != null && !XmConfig.debugCollisionBoxes) {
			OutlineRenderer.drawModelOutline(matrixStack, vertexConsumer, modelState, blockPos.getX() - x, blockPos.getY() - y, blockPos.getZ() - z, 0.0F, 0.0F, 0.0F, 0.4f);
			ci.cancel();
		}
	}

	// TODO: reimplement placement preview
	//	@SuppressWarnings({ "unused", "deprecation" })
	//	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V"), cancellable = false, require = 1)
	//	void blockHighlightHook(MatrixStack matrixStack, float tickDelta, long l, boolean blockOutlineEnabled, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
	//		if (blockOutlineEnabled && false) {
	//			PlacementPreviewRenderer.renderPreview(tickDelta);
	//		}
	//	}
}