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

package grondag.xm2.state;

/**
 * Defines how difference shapes/blocks use block/item metadata. <br>
 * <br>
 * 
 * For SuperBlocks with tile entities, metadata is redundant of information
 * already in modelState. However it may still be stored in block metadata so
 * that it can be searched/retrieved without the need for modelState. <br>
 * <br>
 * 
 * For SuperBlocks without tile entities, metadata may be used to partially
 * derive modelstate for a given world location.
 */
public enum MetaUsage {
    /**
     * Metadata is used to segregate visual block/border boundaries.
     * get/setMetaData() acts as an alias for get/setSpecies()
     */
    SPECIES,

    /** metadata drives some aspect of geometry - usually height/thickness */
    SHAPE,

    /** metadata is not used/related to modelstate */
    NONE
}
