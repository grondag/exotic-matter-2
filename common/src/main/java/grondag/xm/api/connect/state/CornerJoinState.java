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

package grondag.xm.api.connect.state;

import net.minecraft.core.Direction;

import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.connect.CornerJoinStateSelector;

/**
 * Describes the state of a block model with connected textures/shapes that
 * depend on the presence or absence of corner neighbors. (For a total of 26.)
 *
 * <p>A corner join state is a super set of and will always be consistent with the
 * {@link #simpleJoin()} state given the same block neighbors / test.
 */
public interface CornerJoinState {
	int STATE_COUNT = CornerJoinStateSelector.BLOCK_JOIN_STATE_COUNT;

	int ordinal();

	CornerJoinFaceState faceState(Direction face);

	/**
	 * Access to underlying simple 6-sides join.
	 */
	SimpleJoinState simpleJoin();

	static int ordinalFromWorld(BlockNeighbors tests) {
		return CornerJoinStateSelector.ordinalFromWorld(tests);
	}

	static CornerJoinState fromWorld(BlockNeighbors tests) {
		return CornerJoinStateSelector.fromWorld(tests);
	}

	static CornerJoinState fromOrdinal(int ordinal) {
		return CornerJoinStateSelector.fromOrdinal(ordinal);
	}
}
