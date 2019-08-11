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

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.block.XmBlockStateAccess;
import grondag.xm.dispatch.XmDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockCrackParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BlockCrackParticle.class)
public abstract class MixinBlockCrackParticle extends SpriteBillboardParticle {
    // not used
    protected MixinBlockCrackParticle(World world_1, double double_1, double double_2, double double_3) {
        super(world_1, double_1, double_2, double_3);
    }

    private static ThreadLocal<BlockPos.Mutable> POS = ThreadLocal.withInitial(BlockPos.Mutable::new);
    
    @Inject(method = "<init>", at = @At(value = "RETURN"), cancellable = false, require = 0)
    void onNew(World world, double double_1, double double_2, double double_3, double double_4, double double_5, double double_6, BlockState blockState, CallbackInfo ci) {
        final ModelState.Mutable lookupState = XmBlockStateAccess.modelState(blockState, world, POS.get().set(x, y, z), false);
        if(lookupState != null) {
            final ModelState renderState = XmDispatcher.INSTANCE.get(lookupState);
            lookupState.release();
            this.setSprite(renderState.particleSprite());
            final int color = renderState.particleColorARBG();
            this.colorRed = ((color >> 16) & 0xFF) / 255f;
            this.colorGreen = ((color >> 8) & 0xFF) / 255f;
            this.colorBlue = (color& 0xFF) / 255f;
        }
    }
}
