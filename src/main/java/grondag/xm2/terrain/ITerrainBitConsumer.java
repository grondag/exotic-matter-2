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

package grondag.xm2.terrain;

/**
 * Terrain state is longer than 64 bits and we don't always want/need to
 * instantiate a TerrainState object to pass the raw bits around. Implement this
 * for objects that can consume the raw state directly. (Including terrain
 * state.)
 */
@FunctionalInterface
public interface ITerrainBitConsumer<T> {
    public T apply(long terrainBits, int hotnessBits);
}
