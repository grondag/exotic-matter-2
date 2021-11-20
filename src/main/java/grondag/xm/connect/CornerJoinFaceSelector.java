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
public class CornerJoinFaceSelector {
	public final Direction face;

	public final int faceCount;
	public final CornerJoinFaceStateImpl[] faceJoins;
	public final int[] joinIndex = new int[48];

	public CornerJoinFaceSelector(Direction face, SimpleJoinStateImpl baseJoinState) {
		this.face = face;
		faceJoins = CornerJoinFaceStateImpl.find(face, baseJoinState).subStates();
		faceCount = faceJoins.length;

		for (int i = 0; i < faceCount; i++) {
			joinIndex[faceJoins[i].ordinal()] = i;
		}
	}

	public <V> int getIndexFromNeighbors(BlockNeighbors tests) {
		return joinIndex[CornerJoinFaceStateImpl.find(face, tests).ordinal()];
	}

	public CornerJoinFaceStateImpl getFaceJoinFromIndex(int index) {
		return faceJoins[index];
	}
}
