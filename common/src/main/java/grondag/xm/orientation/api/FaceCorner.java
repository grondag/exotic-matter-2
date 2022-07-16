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

package grondag.xm.orientation.api;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.orientation.impl.FaceCornerHelper;

@Experimental
public enum FaceCorner {
	TOP_LEFT(FaceEdge.LEFT_EDGE, FaceEdge.TOP_EDGE), TOP_RIGHT(FaceEdge.TOP_EDGE, FaceEdge.RIGHT_EDGE), BOTTOM_LEFT(FaceEdge.BOTTOM_EDGE, FaceEdge.LEFT_EDGE),
	BOTTOM_RIGHT(FaceEdge.RIGHT_EDGE, FaceEdge.BOTTOM_EDGE);

	/**
	 * Face edge that is counterclockwise from this block corner.
	 */
	public final FaceEdge leftSide;

	/**
	 * Face edge that is clockwise from this block corner.
	 */
	public final FaceEdge rightSide;

	@Internal
	public final int ordinalBit;

	FaceCorner(FaceEdge leftSide, FaceEdge rightSide) {
		this.leftSide = leftSide;
		this.rightSide = rightSide;
		ordinalBit = 1 << ordinal();
	}

	public static final int COUNT = FaceCornerHelper.COUNT;

	public static FaceCorner find(FaceEdge side1, FaceEdge side2) {
		return FaceCornerHelper.find(side1, side2);
	}

	public static FaceCorner fromOrdinal(int ordinal) {
		return FaceCornerHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<FaceCorner> consumer) {
		FaceCornerHelper.forEach(consumer);
	}
}
