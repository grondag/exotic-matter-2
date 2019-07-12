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

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Read-only terrain world cache
 */
public class TerrainWorldCache extends TerrainWorldAdapter {
    @Override
    public void setBlockState(long packedBlockPos, BlockState newState) {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }

    @Override
    protected void setBlockState(long packedBlockPos, BlockState newState, boolean callback) {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }

    @Override
    public void setBlockState(BlockPos blockPos, BlockState newState) {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }
}
