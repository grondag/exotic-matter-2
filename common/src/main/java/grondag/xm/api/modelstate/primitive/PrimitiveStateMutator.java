/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.api.modelstate.primitive;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.modelstate.PrimitiveStateMutatorImpl;

@Experimental
@FunctionalInterface
public interface PrimitiveStateMutator {
	void mutate(MutablePrimitiveState modelState, BlockState blockState, @Nullable BlockGetter world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld);

	default MutablePrimitiveState mutate(MutablePrimitiveState modelState, BlockState blockState) {
		mutate(modelState, blockState, null, null, null, false);
		return modelState;
	}

	static Builder builder() {
		return PrimitiveStateMutatorImpl.builder();
	}

	public interface Builder {
		Builder withJoin(BlockTest<PrimitiveState> joinTest);

		Builder withUpdate(PrimitiveStateMutator update);

		PrimitiveStateMutator build();

		Builder clear();
	}
}
