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
package grondag.xm.api.connect.world;

import static org.apiguardian.api.API.Status.STABLE;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import grondag.xm.api.modelstate.ModelState;

@API(status = STABLE)
public interface BlockTestContext<T extends ModelState> {
	BlockView world();

	BlockPos fromPos();

	BlockState fromBlockState();

	@Nullable
	BlockEntity fromBlockEntity();

	@Nullable
	T fromModelState();

	BlockPos toPos();

	BlockState toBlockState();

	@Nullable
	BlockEntity toBlockEntity();

	@Nullable
	T toModelState();

	/**
	 * If this test is for a face adjacent to the "from" block,
	 * the face that describes the relative position of the "to" block.
	 *
	 * @return Face relative to from block, as described above.
	 * Or null if this test is for a corner block.
	 */
	@Nullable
	Direction toFace();
}
