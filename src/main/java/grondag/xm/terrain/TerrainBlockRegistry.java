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

import java.util.HashMap;

import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.block.Block;

/** tracks which terrain blocks can be frozen or thawed from each other */
@Internal
public class TerrainBlockRegistry {
	private final HashBiMap<Block, Block> stateMap = HashBiMap.create(16);
	private final HashBiMap<Block, Block> fillerMap = HashBiMap.create(16);
	private final HashMap<Block, Block> cubicMap = new HashMap<>(16);
	public static final TerrainBlockRegistry TERRAIN_STATE_REGISTRY = new TerrainBlockRegistry();

	public void registerStateTransition(Block dynamicBlock, Block staticBlock) {
		stateMap.put(dynamicBlock, staticBlock);
	}

	public TerrainStaticBlock getStaticBlock(Block dynamicBlock) {
		return (TerrainStaticBlock) stateMap.get(dynamicBlock);
	}

	public Block getDynamicBlock(Block staticBlock) {
		return stateMap.inverse().get(staticBlock);
	}

	public void registerFiller(Block heightBlock, Block fillerBlock) {
		fillerMap.put(heightBlock, fillerBlock);
	}

	public Block getFillerBlock(Block hieghtBlock) {
		return fillerMap.get(hieghtBlock);
	}

	public Block getHeightBlock(Block fillerBlock) {
		return fillerMap.inverse().get(fillerBlock);
	}

	public void registerCubic(Block flowBlock, Block cubicBlock) {
		cubicMap.put(flowBlock, cubicBlock);
	}

	public Block getCubicBlock(Block flowBlock) {
		return cubicMap.get(flowBlock);
	}
}
