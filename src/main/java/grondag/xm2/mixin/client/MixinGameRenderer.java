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
package grondag.xm2.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import grondag.xm2.placement.PlacementPreviewRenderer;
import grondag.xm2.render.XmRenderHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VisibleRegion;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    private boolean blockOutlineEnabled;
    @Shadow
    private Camera camera;

    @Inject(method = "renderCenter", at = @At(value = "HEAD"), cancellable = false, require = 1)
    void renderCenterHook(float partialTick, long nanos, CallbackInfo ci) {
        XmRenderHelper.tickDelta(partialTick);
    }
    
//FIXME: make this work
//    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/FrustumWithOrigin;<init>(Lnet/minecraft/client/render/Frustum;)V"), method = "renderCenter", require = 1)
    @SuppressWarnings("unused")
    private VisibleRegion visibleRegionHook(VisibleRegion original) {
        XmRenderHelper.visibleRegion(original);
        return original;
    }

    @SuppressWarnings("unused")
    @Inject(method = "renderCenter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;shouldRender()Z"), cancellable = false, require = 1)
    void blockHighlightHook(float tickDelta, long nanos, CallbackInfo ci) {
        //FIXME: re-enable
        if (blockOutlineEnabled && false) {
            PlacementPreviewRenderer.renderPreview(tickDelta);
        }
    }
}
