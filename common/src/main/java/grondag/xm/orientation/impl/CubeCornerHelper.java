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

package grondag.xm.orientation.impl;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

import grondag.xm.orientation.api.CubeCorner;

@Internal
public abstract class CubeCornerHelper {
	private CubeCornerHelper() {
	}

	private static final CubeCorner[] VALUES = CubeCorner.values();
	public static final int COUNT = VALUES.length;
	private static final CubeCorner[][][] FAR_CORNER_LOOKUP = new CubeCorner[6][6][6];

	static {
		for (final CubeCorner corner : VALUES) {
			FAR_CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()][corner.face3.ordinal()] = corner;
			FAR_CORNER_LOOKUP[corner.face1.ordinal()][corner.face3.ordinal()][corner.face2.ordinal()] = corner;
			FAR_CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()][corner.face3.ordinal()] = corner;
			FAR_CORNER_LOOKUP[corner.face2.ordinal()][corner.face3.ordinal()][corner.face1.ordinal()] = corner;
			FAR_CORNER_LOOKUP[corner.face3.ordinal()][corner.face2.ordinal()][corner.face1.ordinal()] = corner;
			FAR_CORNER_LOOKUP[corner.face3.ordinal()][corner.face1.ordinal()][corner.face2.ordinal()] = corner;
		}
	}

	public static CubeCorner find(Direction face1, Direction face2, Direction face3) {
		return FAR_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()][face3.ordinal()];
	}

	public static final CubeCorner fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<CubeCorner> consumer) {
		for (final CubeCorner val : VALUES) {
			consumer.accept(val);
		}
	}
}
