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
package grondag.xm.terrain;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.HashMap;

import org.apiguardian.api.API;

import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;

/** tracks which terrain blocks can be frozen or thawed from each other */
@API(status = INTERNAL)
public class TerrainBlockRegistry {
    private HashBiMap<Block, Block> stateMap = HashBiMap.create(16);
    private HashBiMap<Block, Block> fillerMap = HashBiMap.create(16);
    private HashMap<Block, Block> cubicMap = new HashMap<Block, Block>(16);
    public static final TerrainBlockRegistry TERRAIN_STATE_REGISTRY = new TerrainBlockRegistry();

    public void registerStateTransition(Block dynamicBlock, Block staticBlock) {
        stateMap.put(dynamicBlock, staticBlock);
    }

    public TerrainStaticBlock getStaticBlock(Block dynamicBlock) {
        return (TerrainStaticBlock) this.stateMap.get(dynamicBlock);
    }

    public Block getDynamicBlock(Block staticBlock) {
        return this.stateMap.inverse().get(staticBlock);
    }

    public void registerFiller(Block heightBlock, Block fillerBlock) {
        fillerMap.put(heightBlock, fillerBlock);
    }

    public Block getFillerBlock(Block hieghtBlock) {
        return this.fillerMap.get(hieghtBlock);
    }

    public Block getHeightBlock(Block fillerBlock) {
        return this.fillerMap.inverse().get(fillerBlock);
    }

    public void registerCubic(Block flowBlock, Block cubicBlock) {
        this.cubicMap.put(flowBlock, cubicBlock);
    }

    public Block getCubicBlock(Block flowBlock) {
        return this.cubicMap.get(flowBlock);
    }
}
