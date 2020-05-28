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
package grondag.xm.connect;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;

@API(status = INTERNAL)
public class SimpleJoinStateImpl implements SimpleJoinState {
	public static final SimpleJoinState NO_JOINS;
	public static final SimpleJoinState ALL_JOINS;
	public static final SimpleJoinState X_JOINS;
	public static final SimpleJoinState Y_JOINS;
	public static final SimpleJoinState Z_JOINS;

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

		if(hasJoins(Axis.X)) {
			++axisCount;
		}
		if(hasJoins(Axis.Y)) {
			++axisCount;
		}

		if(hasJoins(Axis.Z)) {
			++axisCount;
		}

		if (axisCount <= 1) {
			axisJoinIndex = nextAxisJoinIndex++;
			AXIS_JOIN_LOOKUP[axisJoinIndex] = this;
			assert axisJoinIndex <  AXIS_JOIN_STATE_COUNT;
		} else {
			axisJoinIndex = -1;
		}
	}

	private static final Direction[] FACES = Direction.values();

	private static final SimpleJoinStateImpl JOINS[] = new SimpleJoinStateImpl[STATE_COUNT];

	private static final SimpleJoinStateImpl AXIS_JOIN_LOOKUP[] = new SimpleJoinStateImpl[AXIS_JOIN_STATE_COUNT];

	static {
		for (int i = 0; i < 64; i++) {
			JOINS[i] = new SimpleJoinStateImpl(i);
		}

		NO_JOINS = JOINS[0];
		ALL_JOINS = JOINS[0b111111];
		X_JOINS = JOINS[X_MASK];
		Y_JOINS = JOINS[Y_MASK];
		Z_JOINS = JOINS[Z_MASK];
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
		switch(axis) {
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
		return ((SimpleJoinStateImpl) fromJoin).axisJoinIndex ;
	}

	public static SimpleJoinState fromAxisJoinIndex(int fromIndex) {
		return AXIS_JOIN_LOOKUP[fromIndex];
	}
}
