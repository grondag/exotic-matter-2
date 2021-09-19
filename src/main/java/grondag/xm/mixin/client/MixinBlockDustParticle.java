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
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmDispatcher;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(TerrainParticle.class)
public abstract class MixinBlockDustParticle extends TextureSheetParticle {
	// not used
	protected MixinBlockDustParticle(ClientLevel world_1, double double_1, double double_2, double double_3) {
		super(world_1, double_1, double_2, double_3);
	}

	@Inject(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "RETURN"), cancellable = false, require = 0)
	void onNew(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
		final MutableModelState lookupState = XmBlockState.modelState(blockState, world, blockPos, false);

		if(lookupState != null) {
			final ModelState renderState = XmDispatcher.INSTANCE.get(lookupState);
			lookupState.release();
			this.setSprite(renderState.particleSprite());
			final int color = renderState.particleColorARBG();
			rCol = ((color >> 16) & 0xFF) / 255f;
			gCol = ((color >> 8) & 0xFF) / 255f;
			bCol = (color& 0xFF) / 255f;
		}
	}
}
