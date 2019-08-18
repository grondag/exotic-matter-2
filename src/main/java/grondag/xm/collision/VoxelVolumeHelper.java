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

package grondag.xm.collision;

class VoxelVolumeHelper {

    static void setBit(int index, long[] target) {
        target[index >> 6] |= (1L << (index & 63));
    }

    static void clearBit(int index, long[] target) {
        target[index >> 6] &= ~(1L << (index & 63));
    }

    static boolean isClear(int index, long[] src) {
        return (src[index >> 6] & (1L << (index & 63))) == 0;
    }

    static boolean isSet(int index, long[] src) {
        return !isClear(index, src);
    }

}
