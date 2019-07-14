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

package grondag.xm2.connect;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm2.api.connect.state.CornerJoinState;
import net.minecraft.util.math.Direction;

@API(status = INTERNAL)
class CornerJoinStateImpl implements CornerJoinState {
    private final int index;

    /** join state considering only direct neighbors */
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
