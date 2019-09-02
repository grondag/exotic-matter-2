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

import grondag.xm.render.RenderUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow private ClientWorld world;
    
    @Inject(method = "drawHighlightedBlockOutline", at = @At(value = "INVOKE", 
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;popMatrix()V"),
            cancellable = false, require = 1)
    void blockHighlightHook(Camera camera, HitResult hit, int zero, CallbackInfo ci) {
        RenderUtil.drawModelOutline(camera, hit, world);
    }
}
