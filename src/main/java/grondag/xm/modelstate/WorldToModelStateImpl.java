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
package grondag.xm.modelstate;

import static grondag.xm.api.modelstate.ModelStateFlags.CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.POSITION;
import static grondag.xm.api.modelstate.ModelStateFlags.SIMPLE_JOIN;

import java.util.ArrayList;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.MasonryHelper;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveStateFunction;
import grondag.xm.api.modelstate.primitive.PrimitiveStateMutator;
import grondag.xm.api.primitive.simple.CubeWithRotation;

@Experimental
public class WorldToModelStateImpl implements PrimitiveStateFunction {
	private final BlockTest<PrimitiveState> joinTest;
	private final PrimitiveStateMutator updater;
	private final PrimitiveState defaultState;

	private WorldToModelStateImpl(BuilderImpl builder) {
		joinTest = builder.joinTest;
		defaultState = builder.defaultState;
		if(builder.updaters.isEmpty()) {
			updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {};
		} else if(builder.updaters.size() == 1) {
			updater = builder.updaters.get(0);
		} else {
			final PrimitiveStateMutator[] funcs = builder.updaters.toArray(new PrimitiveStateMutator[builder.updaters.size()]);
			updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {
				for(final PrimitiveStateMutator func : funcs) {
					func.mutate(modelState, xmBlockState, world, pos, neighbors, refreshFromWorld);
				}
			};
		}
	}

	@Override
	public MutablePrimitiveState apply(BlockState blockState, BlockView world, BlockPos pos, boolean refreshFromWorld) {
		final MutablePrimitiveState modelState = defaultState.mutableCopy();
		if(!modelState.isStatic() && refreshFromWorld) {

			BlockNeighbors neighbors = null;

			final int stateFlags = modelState.stateFlags();
			if ((stateFlags & POSITION) == POSITION) {
				modelState.pos(pos);
			}

			if ((CORNER_JOIN & stateFlags) == CORNER_JOIN) {
				neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, joinTest).withBlockState(blockState);
				modelState.cornerJoin(CornerJoinState.fromWorld(neighbors));

			} else if ((SIMPLE_JOIN & stateFlags) == SIMPLE_JOIN) {
				neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, joinTest).withBlockState(blockState);
				modelState.simpleJoin(SimpleJoinState.fromWorld(neighbors));
			}

			if ((MASONRY_JOIN & stateFlags) == MASONRY_JOIN) {
				if (neighbors == null) {
					neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, MasonryHelper.wrap(joinTest)).withBlockState(blockState);
				} else {
					neighbors.withTest(MasonryHelper.wrap(joinTest));
				}

				modelState.alternateJoin(SimpleJoinState.fromWorld(neighbors));
			}

			updater.mutate(modelState, blockState, world, pos, neighbors, refreshFromWorld);

			if (neighbors != null) {
				neighbors.release();
			}
		}

		return modelState;
	}

	private static class BuilderImpl implements PrimitiveStateFunction.Builder {
		private BlockTest<PrimitiveState> joinTest = BlockTest.sameBlock();
		private final ArrayList<PrimitiveStateMutator> updaters = new ArrayList<>();
		private PrimitiveState defaultState = CubeWithRotation.INSTANCE.defaultState();

		private BuilderImpl() {}

		@Override
		public Builder withDefaultState(PrimitiveState defaultState) {
			this.defaultState = defaultState == null ? CubeWithRotation.INSTANCE.defaultState() : defaultState;
			return this;
		}

		@Override
		public Builder withJoin(BlockTest<PrimitiveState> joinTest) {
			this.joinTest = joinTest == null ? BlockTest.sameBlock() : joinTest;
			return this;
		}

		@Override
		public Builder withUpdate(PrimitiveStateMutator function) {
			if(function != null) {
				updaters.add(function);
			}
			return this;
		}

		@Override
		public Builder clear() {
			joinTest = BlockTest.sameBlock();
			defaultState = CubeWithRotation.INSTANCE.defaultState();
			updaters.clear();
			return this;
		}

		@Override
		public
		PrimitiveStateFunction build() {
			return new WorldToModelStateImpl(this);
		}
	}

	public static Builder builder() {
		return new BuilderImpl();
	}
}
