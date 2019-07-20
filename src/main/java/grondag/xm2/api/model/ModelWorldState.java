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
package grondag.xm2.api.model;

import blue.endless.jankson.annotation.Nullable;
import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;

/**
 * Block-specific elements of model state, determined from BlockState or block
 * state of neighboring blocks, or bloc position. For static model state, may be
 * serialized vs. derived from world.
 *
 */
public interface ModelWorldState {
    default int posX() {
        return 0;
    }

    default int posY() {
        return 0;
    }

    default int posZ() {
        return 0;
    }

    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    default boolean hasSpecies() {
        return false;
    }

    /**
     * Will return 0 if model state does not include species. This is more
     * convenient than checking each place species is used.
     * 
     * @return
     */
    default int species() {
        return 0;
    }

    default @Nullable SimpleJoinState simpleJoin() {
        return null;
    }

    default @Nullable CornerJoinState cornerJoin() {
        return null;
    }

    default @Nullable SimpleJoinState masonryJoin() {
        return null;
    }
}
