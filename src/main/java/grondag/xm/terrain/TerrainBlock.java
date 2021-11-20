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

package grondag.xm.terrain;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import grondag.xm.api.modelstate.MutableModelState;

@Internal
public class TerrainBlock extends Block implements IHotBlock {
	public static final IntegerProperty HEAT = IntegerProperty.create("xm2_heat", 0, 15);
	public static final EnumProperty<TerrainType> TERRAIN_TYPE = EnumProperty.create("xm2_terrain", TerrainType.class);

	public TerrainBlock(Properties blockSettings, MutableModelState defaultModelState) {
		super(blockSettings);
	}
}
