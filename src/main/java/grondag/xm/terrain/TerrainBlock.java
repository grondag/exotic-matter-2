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

import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.xm.api.modelstate.MutableModelState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@Internal
public class TerrainBlock extends Block implements IHotBlock {
	public static final IntegerProperty HEAT = IntegerProperty.create("xm2_heat", 0, 15);
	public static final EnumProperty<TerrainType> TERRAIN_TYPE = EnumProperty.create("xm2_terrain", TerrainType.class);

	public TerrainBlock(Properties blockSettings, MutableModelState defaultModelState) {
		super(blockSettings);
	}

}
