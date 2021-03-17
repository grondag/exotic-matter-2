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
package grondag.xm.api.connect.state;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.connect.SimpleJoinStateImpl;

/**
 * Describes the state of a block model with connected textures/shapes that
 * depend on only on six directly adjacent neighbors.
 * <p>
 */
public interface SimpleJoinState {
	int STATE_COUNT = 64; // 2^6
	int AXIS_JOIN_STATE_COUNT = 1 + 3 * 3; // No joins, plus +/-/both states for each axis
	int AXIS_JOIN_BIT_COUNT = Integer.bitCount(MathHelper.smallestEncompassingPowerOfTwo(AXIS_JOIN_STATE_COUNT) - 1);
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
