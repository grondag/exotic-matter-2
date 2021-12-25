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

package grondag.xm.api.connect.world;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.modelstate.ModelState;

public interface BlockTestContext<T extends ModelState> {
	BlockGetter world();

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
