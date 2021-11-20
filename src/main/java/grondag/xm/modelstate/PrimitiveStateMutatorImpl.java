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

package grondag.xm.modelstate;

import static grondag.xm.api.modelstate.ModelStateFlags.CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.POSITION;
import static grondag.xm.api.modelstate.ModelStateFlags.SIMPLE_JOIN;

import java.util.ArrayList;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.MasonryHelper;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveStateMutator;

@Experimental
public class PrimitiveStateMutatorImpl implements PrimitiveStateMutator {
	private final BlockTest<PrimitiveState> joinTest;
	private final PrimitiveStateMutator updater;

	private PrimitiveStateMutatorImpl(BuilderImpl builder) {
		joinTest = builder.joinTest;

		if (builder.updaters.isEmpty()) {
			updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> { };
		} else if (builder.updaters.size() == 1) {
			updater = builder.updaters.get(0);
		} else {
			final PrimitiveStateMutator[] funcs = builder.updaters.toArray(new PrimitiveStateMutator[builder.updaters.size()]);
			updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {
				for (final PrimitiveStateMutator func : funcs) {
					func.mutate(modelState, xmBlockState, world, pos, neighbors, refreshFromWorld);
				}
			};
		}
	}

	@Override
	public void mutate(MutablePrimitiveState modelState, BlockState blockState, BlockGetter world, BlockPos pos, BlockNeighbors neighbors, boolean refreshFromWorld) {
		if (!modelState.isStatic() && refreshFromWorld) {
			final int stateFlags = modelState.stateFlags();

			if ((stateFlags & POSITION) == POSITION) {
				modelState.pos(pos);
			}

			if ((CORNER_JOIN & stateFlags) == CORNER_JOIN) {
				neighbors = BlockNeighbors.claimIfNull(neighbors, world, pos, ModelStateFunction.STATIC, joinTest, blockState);
				modelState.cornerJoin(CornerJoinState.fromWorld(neighbors));
			} else if ((SIMPLE_JOIN & stateFlags) == SIMPLE_JOIN) {
				neighbors = BlockNeighbors.claimIfNull(neighbors, world, pos, ModelStateFunction.STATIC, joinTest, blockState);
				modelState.simpleJoin(SimpleJoinState.fromWorld(neighbors));
			}

			if ((MASONRY_JOIN & stateFlags) == MASONRY_JOIN) {
				neighbors = BlockNeighbors.claimIfNull(neighbors, world, pos, ModelStateFunction.STATIC, MasonryHelper.wrap(joinTest), blockState);
				modelState.alternateJoin(SimpleJoinState.fromWorld(neighbors));
			}

			updater.mutate(modelState, blockState, world, pos, neighbors, refreshFromWorld);

			if (neighbors != null) {
				neighbors.release();
			}
		}
	}

	private static class BuilderImpl implements PrimitiveStateMutator.Builder {
		private BlockTest<PrimitiveState> joinTest = BlockTest.sameBlock();
		private final ArrayList<PrimitiveStateMutator> updaters = new ArrayList<>();

		private BuilderImpl() { }

		@Override
		public Builder withJoin(BlockTest<PrimitiveState> joinTest) {
			this.joinTest = joinTest == null ? BlockTest.sameBlock() : joinTest;
			return this;
		}

		@Override
		public Builder withUpdate(PrimitiveStateMutator function) {
			if (function != null) {
				updaters.add(function);
			}

			return this;
		}

		@Override
		public Builder clear() {
			joinTest = BlockTest.sameBlock();
			updaters.clear();
			return this;
		}

		@Override
		public PrimitiveStateMutator build() {
			return new PrimitiveStateMutatorImpl(this);
		}
	}

	public static Builder builder() {
		return new BuilderImpl();
	}
}
