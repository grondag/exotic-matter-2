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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmDispatcher;

@Mixin(BlockDustParticle.class)
public abstract class MixinBlockDustParticle extends SpriteBillboardParticle {
	// not used
	protected MixinBlockDustParticle(ClientWorld world_1, double double_1, double double_2, double double_3) {
		super(world_1, double_1, double_2, double_3);
	}

	private static ThreadLocal<BlockPos.Mutable> POS = ThreadLocal.withInitial(BlockPos.Mutable::new);

	@Inject(method = "<init>", at = @At(value = "RETURN"), cancellable = false, require = 0)
	void onNew(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState blockState, CallbackInfo ci) {
		final MutableModelState lookupState = XmBlockState.modelState(blockState, world, POS.get().set(x, y, z), false);

		if(lookupState != null) {
			final ModelState renderState = XmDispatcher.INSTANCE.get(lookupState);
			lookupState.release();
			this.setSprite(renderState.particleSprite());
			final int color = renderState.particleColorARBG();
			colorRed = ((color >> 16) & 0xFF) / 255f;
			colorGreen = ((color >> 8) & 0xFF) / 255f;
			colorBlue = (color& 0xFF) / 255f;
		}
	}
}
