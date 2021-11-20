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

package grondag.xm.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelStateFunction;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmBlockStateAccess;

@Mixin(BlockState.class)
public class MixinBlockState implements XmBlockState, XmBlockStateAccess {
	private ModelStateFunction<?> modelStateFunc = null;

	@Override
	public BlockState blockState() {
		return (BlockState) (Object) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MutableModelState> T modelState(BlockGetter world, BlockPos pos, boolean refreshFromWorld) {
		final ModelStateFunction<?> func = modelStateFunc;
		return func == null ? null : (T) func.apply((BlockState) (Object) this, world, pos, refreshFromWorld);
	}

	@Override
	public void xm_modelStateFunc(ModelStateFunction<?> func) {
		modelStateFunc = func;
	}

	@Override
	public ModelStateFunction<?> xm_modelStateFunc() {
		return modelStateFunc;
	}

	@Override
	@Nullable
	public XmBlockState xm_toXmBlockState() {
		return modelStateFunc == null ? null : (XmBlockState) this;
	}
}
