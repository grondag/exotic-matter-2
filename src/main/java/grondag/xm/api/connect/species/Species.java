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

package grondag.xm.api.connect.species;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import grondag.fermion.position.BlockRegion;
import grondag.xm.connect.SpeciesImpl;

@Experimental
public class Species {
	private Species() { }

	public static int speciesForPlacement(BlockGetter world, BlockPos onPos, Direction onFace, SpeciesMode mode, SpeciesFunction func) {
		return SpeciesImpl.speciesForPlacement(world, onPos, onFace, mode, func, null);
	}

	public static int speciesForPlacement(BlockGetter world, BlockPos onPos, Direction onFace, SpeciesMode mode, SpeciesFunction func, BlockRegion region) {
		return SpeciesImpl.speciesForPlacement(world, onPos, onFace, mode, func, region);
	}
}
