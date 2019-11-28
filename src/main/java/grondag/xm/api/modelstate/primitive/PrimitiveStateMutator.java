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
package grondag.xm.api.modelstate.primitive;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import grondag.xm.api.connect.world.BlockNeighbors;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface PrimitiveStateMutator {
	void mutate(MutablePrimitiveState modelState, BlockState blockState, @Nullable BlockView world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld);

	default MutablePrimitiveState mutate(MutablePrimitiveState modelState, BlockState blockState) {
		mutate(modelState, blockState, null, null, null, false);
		return modelState;
	}
}
