package grondag.xm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Frustum;

import grondag.xm.render.XmRenderHelper;

@Mixin(Frustum.class)
public abstract class MixinFrustum {
	@Inject(at = @At(value = "HEAD"), method = "setPosition", require = 1)
	private void onSetPosition(CallbackInfo ci) {
		XmRenderHelper.frustum((Frustum)(Object) this);
	}
}
