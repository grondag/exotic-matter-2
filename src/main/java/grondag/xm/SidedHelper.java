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

package grondag.xm;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.HashMap;
import java.util.function.BiConsumer;

import org.apiguardian.api.API;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

/**
 * Utilities to prevents dedicated server references to client-only classes.
 */
@API(status = INTERNAL)
public enum SidedHelper {
	;

	static BiConsumer<Block, BlockState> RENDER_LAYER_REMAPPER = null;

	static HashMap<Block, BlockState> RENDER_LAYER_REMAPS = new HashMap<>();

	public static void mapRenderLayerLike(Block blockToMap, BlockState mapLike) {
		if (RENDER_LAYER_REMAPPER == null) {
			RENDER_LAYER_REMAPS.put(blockToMap, mapLike);
		} else  {
			RENDER_LAYER_REMAPPER.accept(blockToMap, mapLike);
		}
	}
}
