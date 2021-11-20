/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm;

import java.util.HashMap;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utilities to prevents dedicated server references to client-only classes.
 */
@Internal
public final class SidedHelper {
	private SidedHelper() { }

	static BiConsumer<Block, BlockState> RENDER_LAYER_REMAPPER = null;

	static HashMap<Block, BlockState> RENDER_LAYER_REMAPS = new HashMap<>();

	public static void mapRenderLayerLike(Block blockToMap, BlockState mapLike) {
		if (RENDER_LAYER_REMAPPER == null) {
			RENDER_LAYER_REMAPS.put(blockToMap, mapLike);
		} else {
			RENDER_LAYER_REMAPPER.accept(blockToMap, mapLike);
		}
	}
}
