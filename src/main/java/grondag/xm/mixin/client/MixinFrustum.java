package grondag.xm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import grondag.xm.render.XmRenderHelper;
import net.minecraft.client.render.Frustum;

@Mixin(Frustum.class)
public abstract class MixinFrustum {
	@Inject(at = @At(value = "HEAD"), method = "setPosition", require = 1)
	private void onSetPosition(CallbackInfo ci) {
		XmRenderHelper.frustum((Frustum)(Object) this);
	}
}
