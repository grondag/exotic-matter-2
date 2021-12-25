/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.terrain;

import java.util.HashMap;

import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.level.block.Block;

/** Tracks which terrain blocks can be frozen or thawed from each other. */
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
