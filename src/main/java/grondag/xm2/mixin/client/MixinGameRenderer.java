package grondag.xm2.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import grondag.xm2.placement.PlacementPreviewRenderer;
import grondag.xm2.render.XmRenderHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VisibleRegion;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow private boolean blockOutlineEnabled;
    @Shadow private Camera camera;
    
    @Inject(method = "renderCenter", at = @At(value = "HEAD"), cancellable = false, require = 1)
    void renderCenterHook(float partialTick, long nanos, CallbackInfo ci) {
        XmRenderHelper.tickDelta(partialTick);
    }
    
    @ModifyVariable(
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/FrustumWithOrigin;<init>(Lnet/minecraft/client/render/Frustum;)V"
            ),
            method = "renderCenter", require = 1
        )
        private VisibleRegion visibleRegionHook(VisibleRegion original) {
            XmRenderHelper.visibleRegion(original);
            return original;
        }
    
    @Inject(method = "renderCenter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;shouldRender()Z"), cancellable = false, require = 1)
    void blockHighlightHook(float tickDelta, long nanos, CallbackInfo ci) {
        if(blockOutlineEnabled) {
            PlacementPreviewRenderer.renderPreview(tickDelta);
        }
    }
}
