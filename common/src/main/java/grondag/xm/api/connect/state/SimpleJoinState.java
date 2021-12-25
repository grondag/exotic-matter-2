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
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.connect.SimpleJoinStateImpl;

/**
 * Describes the state of a block model with connected textures/shapes that
 * depend on only on six directly adjacent neighbors.
 */
public interface SimpleJoinState {
	int STATE_COUNT = 64; // 2^6
	int AXIS_JOIN_STATE_COUNT = 1 + 3 * 3; // No joins, plus +/-/both states for each axis
	int AXIS_JOIN_BIT_COUNT = Integer.bitCount(Mth.smallestEncompassingPowerOfTwo(AXIS_JOIN_STATE_COUNT) - 1);
	SimpleJoinState NO_JOINS = SimpleJoinStateImpl.NO_JOINS;
	SimpleJoinState ALL_JOINS = SimpleJoinStateImpl.ALL_JOINS;
	SimpleJoinState X_JOINS = SimpleJoinStateImpl.X_JOINS;
	SimpleJoinState Y_JOINS = SimpleJoinStateImpl.Y_JOINS;
	SimpleJoinState Z_JOINS = SimpleJoinStateImpl.Z_JOINS;

	SimpleJoinState EAST_JOIN = SimpleJoinStateImpl.EAST_JOIN;
	SimpleJoinState WEST_JOIN = SimpleJoinStateImpl.WEST_JOIN;
	SimpleJoinState NORTH_JOIN = SimpleJoinStateImpl.NORTH_JOIN;
	SimpleJoinState SOUTH_JOIN = SimpleJoinStateImpl.SOUTH_JOIN;
	SimpleJoinState UP_JOIN = SimpleJoinStateImpl.UP_JOIN;
	SimpleJoinState DOWN_JOIN = SimpleJoinStateImpl.DOWN_JOIN;

	boolean isJoined(Direction face);

	boolean hasJoins(Axis axis);

	int ordinal();

	SimpleJoinFaceState faceState(Direction nominalFace);

	static SimpleJoinState fromOrdinal(int ordinal) {
		return SimpleJoinStateImpl.fromOrdinal(ordinal);
	}

	static SimpleJoinState fromWorld(BlockNeighbors neighbors) {
		return SimpleJoinStateImpl.fromWorld(neighbors);
	}

	static int ordinalFromWorld(BlockNeighbors neighbors) {
		return SimpleJoinStateImpl.ordinalFromWorld(neighbors);
	}

	static int toAxisJoinIndex(SimpleJoinState fromJoin) {
		return SimpleJoinStateImpl.toAxisJoinIndex(fromJoin);
	}

	static SimpleJoinState fromAxisJoinIndex(int fromIndex) {
		return SimpleJoinStateImpl.fromAxisJoinIndex(fromIndex);
	}
}
