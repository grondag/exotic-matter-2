/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmDispatcher;

@Mixin(TerrainParticle.class)
public abstract class MixinTerrainParticle extends TextureSheetParticle {
	// not used
	protected MixinTerrainParticle(ClientLevel world_1, double double_1, double double_2, double double_3) {
		super(world_1, double_1, double_2, double_3);
	}

	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "RETURN"), cancellable = false, require = 0)
	void onNew(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
		final MutableModelState lookupState = XmBlockState.modelState(blockState, world, blockPos, false);

		if (lookupState != null) {
			final ModelState renderState = XmDispatcher.INSTANCE.get(lookupState);
			lookupState.release();
			this.setSprite(renderState.particleSprite());
			final int color = renderState.particleColorARBG();
			rCol = ((color >> 16) & 0xFF) / 255f;
			gCol = ((color >> 8) & 0xFF) / 255f;
			bCol = (color & 0xFF) / 255f;
		}
	}
}
