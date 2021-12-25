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

import net.minecraft.util.Mth;

import grondag.xm.orientation.api.HorizontalEdge;
import grondag.xm.orientation.api.HorizontalFace;

@Internal
public abstract class HorizontalEdgeHelper {
	private HorizontalEdgeHelper() {
	}

	private static final HorizontalEdge[] VALUES = HorizontalEdge.values();
	public static final int COUNT = VALUES.length;

	private static final HorizontalEdge[][] HORIZONTAL_CORNER_LOOKUP = new HorizontalEdge[4][4];

	static {
		for (final HorizontalEdge corner : HorizontalEdge.values()) {
			HORIZONTAL_CORNER_LOOKUP[corner.left.ordinal()][corner.right.ordinal()] = corner;
			HORIZONTAL_CORNER_LOOKUP[corner.right.ordinal()][corner.left.ordinal()] = corner;
		}
	}

	public static HorizontalEdge find(HorizontalFace face1, HorizontalFace face2) {
		return HORIZONTAL_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
	}

	public static HorizontalEdge fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<HorizontalEdge> consumer) {
		for (final HorizontalEdge val : VALUES) {
			consumer.accept(val);
		}
	}

	public static HorizontalEdge fromRotation(double yawDegrees) {
		final int ordinal = Mth.floor(yawDegrees / 90.0D) & 3;

		switch (ordinal) {
			case 0:
				return HorizontalEdge.SOUTH_WEST;
			case 1:
				return HorizontalEdge.NORTH_WEST;
			case 2:
				return HorizontalEdge.NORTH_EAST;
			default:
				return HorizontalEdge.SOUTH_EAST;
		}
	}
}
