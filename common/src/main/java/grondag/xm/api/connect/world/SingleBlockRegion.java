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

public class SingleBlockRegion implements BlockRegion {
	private final BlockPos pos;

	public SingleBlockRegion(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public Iterable<BlockPos> surfacePositions() {
		return BlockPos.betweenClosed(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public Iterable<BlockPos> adjacentPositions() {
		return CubicBlockRegion.getAllOnBoxSurfaceMutable(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
			pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}
}
