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

import grondag.xm.api.connect.state.CornerJoinState;

@Internal
class CornerJoinStateImpl implements CornerJoinState {
	private final int index;

	/** Join state considering only direct neighbors. */
	public final SimpleJoinStateImpl simpleJoin;

	private final byte[] faceJoinIndex;

	CornerJoinStateImpl(int index, SimpleJoinStateImpl simpleJoin, byte[] faceJoinIndex) {
		this.index = index;
		this.simpleJoin = simpleJoin;
		this.faceJoinIndex = faceJoinIndex;
	}

	@Override
	public SimpleJoinStateImpl simpleJoin() {
		return simpleJoin;
	}

	@Override
	public int ordinal() {
		return index;
	}

	@Override
	public CornerJoinFaceStateImpl faceState(Direction face) {
		return CornerJoinFaceStateImpl.fromOrdinal(faceJoinIndex[face.ordinal()]);
	}
}
