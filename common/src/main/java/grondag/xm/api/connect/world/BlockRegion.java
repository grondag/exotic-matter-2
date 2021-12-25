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

package grondag.xm.api.connect.world;

import net.minecraft.core.BlockPos;

/**
 * Iterators for multi-block regions. Mainly used for species detection.
 */
public interface BlockRegion {
	/** All positions on the surface of the region. */
	Iterable<BlockPos> surfacePositions();

	/** All positions adjacent to the surface of the region. */
	Iterable<BlockPos> adjacentPositions();

	static BlockRegion of(BlockPos pos) {
		return new SingleBlockRegion(pos);
	}
}
