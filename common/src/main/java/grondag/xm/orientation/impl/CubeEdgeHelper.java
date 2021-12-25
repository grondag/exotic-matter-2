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

import grondag.xm.orientation.api.CubeEdge;

@Internal
public abstract class CubeEdgeHelper {
	private CubeEdgeHelper() {
	}

	private static final CubeEdge[] VALUES = CubeEdge.values();
	public static final int COUNT = VALUES.length;
	private static final CubeEdge[][] CORNER_LOOKUP = new CubeEdge[6][6];

	static {
		for (final CubeEdge corner : VALUES) {
			CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()] = corner;
			CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()] = corner;
		}
	}

	public static CubeEdge find(Direction face1, Direction face2) {
		return CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
	}

	public static final CubeEdge fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<CubeEdge> consumer) {
		for (final CubeEdge val : VALUES) {
			consumer.accept(val);
		}
	}
}
