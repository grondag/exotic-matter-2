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

package grondag.xm.connect;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

import grondag.xm.api.connect.state.SimpleJoinFaceState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;

@Internal
public class SimpleJoinStateImpl implements SimpleJoinState {
	public static final SimpleJoinState NO_JOINS;
	public static final SimpleJoinState ALL_JOINS;
	public static final SimpleJoinState X_JOINS;
	public static final SimpleJoinState Y_JOINS;
	public static final SimpleJoinState Z_JOINS;
	public static final SimpleJoinState EAST_JOIN;
	public static final SimpleJoinState WEST_JOIN;
	public static final SimpleJoinState NORTH_JOIN;
	public static final SimpleJoinState SOUTH_JOIN;
	public static final SimpleJoinState UP_JOIN;
	public static final SimpleJoinState DOWN_JOIN;

	private static final int X_MASK = (1 << Direction.EAST.ordinal()) | (1 << Direction.WEST.ordinal());
	private static final int Y_MASK = (1 << Direction.UP.ordinal()) | (1 << Direction.DOWN.ordinal());
	private static final int Z_MASK = (1 << Direction.NORTH.ordinal()) | (1 << Direction.SOUTH.ordinal());

	private static int nextAxisJoinIndex = 0;

	private final int joins;

	private final int axisJoinIndex;

	@Override
	public boolean isJoined(Direction face) {
		final int flag = 1 << face.ordinal();
		return (joins & flag) == flag;
	}

	@Override
	public int ordinal() {
		return joins;
	}

	private SimpleJoinStateImpl(int joins) {
		this.joins = joins;

		int axisCount = 0;

		if (hasJoins(Axis.X)) {
			++axisCount;
		}

		if (hasJoins(Axis.Y)) {
			++axisCount;
		}

		if (hasJoins(Axis.Z)) {
			++axisCount;
		}

		if (axisCount <= 1) {
			axisJoinIndex = nextAxisJoinIndex++;
			AXIS_JOIN_LOOKUP[axisJoinIndex] = this;
			assert axisJoinIndex < AXIS_JOIN_STATE_COUNT;
		} else {
			axisJoinIndex = -1;
		}
	}

	private static final Direction[] FACES = Direction.values();

	private static final SimpleJoinStateImpl[] JOINS = new SimpleJoinStateImpl[STATE_COUNT];

	private static final SimpleJoinStateImpl[] AXIS_JOIN_LOOKUP = new SimpleJoinStateImpl[AXIS_JOIN_STATE_COUNT];

	static {
		for (int i = 0; i < 64; i++) {
			JOINS[i] = new SimpleJoinStateImpl(i);
		}

		NO_JOINS = JOINS[0];
		ALL_JOINS = JOINS[0b111111];
		X_JOINS = JOINS[X_MASK];
		Y_JOINS = JOINS[Y_MASK];
		Z_JOINS = JOINS[Z_MASK];

		EAST_JOIN = JOINS[1 << Direction.EAST.ordinal()];
		WEST_JOIN = JOINS[1 << Direction.WEST.ordinal()];
		NORTH_JOIN = JOINS[1 << Direction.NORTH.ordinal()];
		SOUTH_JOIN = JOINS[1 << Direction.SOUTH.ordinal()];
		UP_JOIN = JOINS[1 << Direction.UP.ordinal()];
		DOWN_JOIN = JOINS[1 << Direction.DOWN.ordinal()];
	}

	public static SimpleJoinStateImpl fromOrdinal(int index) {
		return JOINS[index];
	}

	public static SimpleJoinStateImpl fromWorld(BlockNeighbors testResults) {
		return fromOrdinal(ordinalFromWorld(testResults));
	}

	public static int ordinalFromWorld(BlockNeighbors testResults) {
		byte j = 0;

		for (int i = 0; i < 6; i++) {
			if (testResults.result(FACES[i])) {
				j |= (1 << i);
			}
		}

		return j;
	}

	@Override
	public boolean hasJoins(Axis axis) {
		switch (axis) {
			case X:
				return (joins & X_MASK) != 0;
			case Y:
				return (joins & Y_MASK) != 0;
			case Z:
				return (joins & Z_MASK) != 0;
			default:
				return false;
		}
	}

	public static int toAxisJoinIndex(SimpleJoinState fromJoin) {
		return ((SimpleJoinStateImpl) fromJoin).axisJoinIndex;
	}

	public static SimpleJoinState fromAxisJoinIndex(int fromIndex) {
		return AXIS_JOIN_LOOKUP[fromIndex];
	}

	@Override
	public SimpleJoinFaceState faceState(Direction nominalFace) {
		return SimpleJoinFaceState.find(nominalFace, this);
	}
}
