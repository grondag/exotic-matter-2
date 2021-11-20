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

package grondag.xm.connect;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

import grondag.xm.api.connect.world.BlockNeighbors;

@Internal
public class CornerJoinStateSelector {
	private static final Direction[] FACES = Direction.values();
	public static final int BLOCK_JOIN_STATE_COUNT = 20115;
	private static final CornerJoinStateImpl[] BLOCK_JOIN_STATES = new CornerJoinStateImpl[BLOCK_JOIN_STATE_COUNT];
	private static final CornerJoinStateSelector[] BLOCK_JOIN_SELECTOR = new CornerJoinStateSelector[64];

	static {
		int firstIndex = 0;

		for (int i = 0; i < 64; i++) {
			final SimpleJoinStateImpl baseJoin = SimpleJoinStateImpl.fromOrdinal(i);
			BLOCK_JOIN_SELECTOR[i] = new CornerJoinStateSelector(baseJoin, firstIndex);

			for (int j = 0; j < BLOCK_JOIN_SELECTOR[i].stateCount(); j++) {
				BLOCK_JOIN_STATES[firstIndex + j] = BLOCK_JOIN_SELECTOR[i].createChildState(firstIndex + j);
			}

			firstIndex += BLOCK_JOIN_SELECTOR[i].stateCount();
		}
	}

	public static int ordinalFromWorld(BlockNeighbors tests) {
		final SimpleJoinStateImpl baseJoin = SimpleJoinStateImpl.fromWorld(tests);
		return BLOCK_JOIN_SELECTOR[baseJoin.ordinal()].indexFromNeighbors(tests);
	}

	public static CornerJoinStateImpl fromWorld(BlockNeighbors tests) {
		return fromOrdinal(ordinalFromWorld(tests));
	}

	public static CornerJoinStateImpl fromOrdinal(int index) {
		return BLOCK_JOIN_STATES[index];
	}

	private final int firstIndex;
	private final SimpleJoinStateImpl simpleJoin;

	private final CornerJoinFaceSelector[] faceSelector = new CornerJoinFaceSelector[6];

	private CornerJoinStateSelector(SimpleJoinStateImpl baseJoinState, int firstIndex) {
		this.firstIndex = firstIndex;
		simpleJoin = baseJoinState;

		for (int i = 0; i < 6; i++) {
			faceSelector[i] = new CornerJoinFaceSelector(FACES[i], baseJoinState);
		}
	}

	private CornerJoinStateImpl createChildState(int index) {
		int shift = 1;
		final int localIndex = index - firstIndex;
		final byte[] faceJoinIndex = new byte[6];

		for (int i = 0; i < 6; i++) {
			final Direction face = FACES[i];

			if (faceSelector[i].faceCount == 1) {
				faceJoinIndex[face.ordinal()] = (byte) faceSelector[i].getFaceJoinFromIndex(0).ordinal();
			} else {
				final int faceIndex = (localIndex / shift) % faceSelector[i].faceCount;
				faceJoinIndex[face.ordinal()] = (byte) faceSelector[i].getFaceJoinFromIndex(faceIndex).ordinal();
				shift *= faceSelector[i].faceCount;
			}
		}

		return new CornerJoinStateImpl(index, simpleJoin, faceJoinIndex);
	}

	private int stateCount() {
		int count = 1;

		for (int i = 0; i < 6; i++) {
			count *= faceSelector[i].faceCount;
		}

		return count;
	}

	private int indexFromNeighbors(BlockNeighbors tests) {
		int index = 0;
		int shift = 1;

		for (int i = 0; i < 6; i++) {
			if (faceSelector[i].faceCount > 1) {
				index += shift * faceSelector[i].getIndexFromNeighbors(tests);
				shift *= faceSelector[i].faceCount;
			}
		}

		return index + firstIndex;
	}
}
