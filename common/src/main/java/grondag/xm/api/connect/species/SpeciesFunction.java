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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface SpeciesFunction {
	int NO_SPECIES = -1;

	/**
	 *
	 * @param world
	 * @param blockState
	 * @param pos
	 * @return Numeric species value >=0 if block has one, {@link #NO_SPECIES} otherwise.
	 */
	int species(BlockGetter world, BlockState blockState, BlockPos pos);

	default int species(BlockGetter world, BlockPos pos) {
		return species(world, world.getBlockState(pos), pos);
	}
}
