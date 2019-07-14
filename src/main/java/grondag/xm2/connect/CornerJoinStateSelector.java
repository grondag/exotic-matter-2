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

import grondag.xm2.api.connect.world.BlockNeighbors;
import net.minecraft.util.math.Direction;

@API(status = INTERNAL)
public class CornerJoinStateSelector {
    private static final Direction[] FACES = Direction.values();
    public static final int BLOCK_JOIN_STATE_COUNT = 20115;
    private static final CornerJoinStateImpl BLOCK_JOIN_STATES[] = new CornerJoinStateImpl[BLOCK_JOIN_STATE_COUNT];
    private static final CornerJoinStateSelector BLOCK_JOIN_SELECTOR[] = new CornerJoinStateSelector[64];

    static {
        int firstIndex = 0;

        for (int i = 0; i < 64; i++) {
            SimpleJoinStateImpl baseJoin = SimpleJoinStateImpl.fromOrdinal(i);
            BLOCK_JOIN_SELECTOR[i] = new CornerJoinStateSelector(baseJoin, firstIndex);

            for (int j = 0; j < BLOCK_JOIN_SELECTOR[i].stateCount(); j++) {
                BLOCK_JOIN_STATES[firstIndex + j] = BLOCK_JOIN_SELECTOR[i].createChildState(firstIndex + j);
            }

            firstIndex += BLOCK_JOIN_SELECTOR[i].stateCount();
        }
    }

    public static int ordinalFromWorld(BlockNeighbors tests) {
        SimpleJoinStateImpl baseJoin = SimpleJoinStateImpl.fromWorld(tests);
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

    private CornerJoinFaceSelector faceSelector[] = new CornerJoinFaceSelector[6];

    private CornerJoinStateSelector(SimpleJoinStateImpl baseJoinState, int firstIndex) {
        this.firstIndex = firstIndex;
        this.simpleJoin = baseJoinState;
        for (int i = 0; i < 6; i++) {
            faceSelector[i] = new CornerJoinFaceSelector(FACES[i], baseJoinState);
        }
    }

    private CornerJoinStateImpl createChildState(int index) {
        int shift = 1;
        int localIndex = index - firstIndex;
        byte[] faceJoinIndex = new byte[6];
        
        for (int i = 0; i < 6; i++) {
            final Direction face = FACES[i];
            if (faceSelector[i].faceCount == 1) {
                faceJoinIndex[face.ordinal()] = (byte) faceSelector[i].getFaceJoinFromIndex(0).ordinal();
            } else {
                int faceIndex = (localIndex / shift) % faceSelector[i].faceCount;
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
