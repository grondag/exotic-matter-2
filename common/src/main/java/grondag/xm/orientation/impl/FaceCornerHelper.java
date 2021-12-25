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

import grondag.xm.orientation.api.FaceCorner;
import grondag.xm.orientation.api.FaceEdge;

@Internal
public abstract class FaceCornerHelper {
	private FaceCornerHelper() {
	}

	private static final FaceCorner[] VALUES = FaceCorner.values();
	public static final int COUNT = VALUES.length;
	private static FaceCorner[][] LOOKUP = new FaceCorner[4][4];

	static {
		for (final FaceCorner corner : VALUES) {
			LOOKUP[corner.leftSide.ordinal()][corner.rightSide.ordinal()] = corner;
			LOOKUP[corner.rightSide.ordinal()][corner.leftSide.ordinal()] = corner;
		}
	}

	public static FaceCorner find(FaceEdge side1, FaceEdge side2) {
		return LOOKUP[side1.ordinal()][side2.ordinal()];
	}

	public static final FaceCorner fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<FaceCorner> consumer) {
		for (final FaceCorner val : VALUES) {
			consumer.accept(val);
		}
	}
}
