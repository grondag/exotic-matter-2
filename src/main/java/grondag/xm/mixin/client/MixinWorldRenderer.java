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

import grondag.xm.XmConfig;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.render.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow private ClientWorld world;
    
    private static ModelState modelState;
    
    @Inject(method = "drawHighlightedBlockOutline", at = @At(value = "HEAD"), cancellable = false, require = 1)
    private void onBlockHighlight(Camera camera, HitResult hit, int zero, CallbackInfo ci) {
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)hit).getBlockPos();
            BlockState blockState = world.getBlockState(pos);
            modelState = XmBlockState.modelState(blockState, world, pos, true);
        }
    }
    
    @Inject(method = "drawShapeOutline", at = @At(value = "HEAD"), cancellable = true, require = 1)
    private static void onDrawShapeOutline(VoxelShape voxelShape_1, double x, double y, double z, float r, float g, float b, float a, CallbackInfo ci) {
        if(modelState != null && !XmConfig.debugCollisionBoxes) {
            RenderUtil.drawModelOutline(modelState, x, y, z, r, g, b, a);
            ci.cancel();
        }
    }
}