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

package grondag.brocade.connect.api.state;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

import grondag.brocade.connect.api.world.BlockNeighbors;
import grondag.brocade.connect.impl.CornerJoinStateSelector;
import net.minecraft.util.math.Direction;

/**
 * Describes the state of a block model with connected textures/shapes that
 * depend on the presence or absence of corner neighbors. (For a total of 26.)<p>
 * 
 * A corner join state is a super set of and will always be consistent with
 * the {@link #simpleJoin()} state given the same block neighbors / test.<p>
 */
@API(status = STABLE)
public interface CornerJoinState {
    int STATE_COUNT = CornerJoinStateSelector.BLOCK_JOIN_STATE_COUNT;
    
    int ordinal();

    CornerJoinFaceState faceState(Direction face);
    
    /**
     * Access to underlying simple 6-sides join.
     */
    SimpleJoinState simpleJoin();
    
    static int ordinalFromWorld(BlockNeighbors tests) {
        return CornerJoinStateSelector.ordinalFromWorld(tests);
    }

    static CornerJoinState fromWorld(BlockNeighbors tests) {
        return CornerJoinStateSelector.fromWorld(tests);
    }
    
    static CornerJoinState fromOrdinal(int ordinal) {
        return CornerJoinStateSelector.fromOrdinal(ordinal);
    }
}
