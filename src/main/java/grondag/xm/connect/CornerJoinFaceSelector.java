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

import grondag.xm.api.connect.world.BlockNeighbors;

@API(status = INTERNAL)
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
