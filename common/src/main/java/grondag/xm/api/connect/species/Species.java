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

package grondag.xm.api.connect.species;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import grondag.xm.api.connect.world.BlockRegion;
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
