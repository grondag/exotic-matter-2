/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.api.paint;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fermion.orientation.api.ClockwiseRotation;
import grondag.fermion.orientation.api.FaceCorner;
import grondag.fermion.orientation.api.FaceEdge;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.texture.TextureOrientation;

/**
 * Like RotatableQuadrant but for cable-like geometry and textures with end caps.
 */
@Experimental
public enum RotatableCableQuadrant {
	/**
	 * Will position texture to border the left (counter-clockwise) side of a
	 * quadrant with end-cap at the top.
	 */
	END_CAP_LEFT(0),

	/**
	 * Will position texture to border the right (clockwise) side of a quadrant
	 * with end cap at top.
	 */
	END_CAP_RIGHT(0) {
		@Override
		public void applyForQuadrant(MutablePolygon polygon, int layerIndex, FaceCorner quadrant) {
			switch (quadrant) {
			case BOTTOM_LEFT:
				polygon.rotation(layerIndex, TextureOrientation.find(ClockwiseRotation.fromOrdinal((FACE_CORNER_ROTATION_MAP[quadrant.ordinal()] + 1) & 3), false, true));
				break;
			case BOTTOM_RIGHT:
				polygon.rotation(layerIndex, TextureOrientation.find(ClockwiseRotation.fromOrdinal((FACE_CORNER_ROTATION_MAP[quadrant.ordinal()] - 1) & 3), true, false));
				break;
			case TOP_LEFT:
				polygon.rotation(layerIndex, TextureOrientation.find(ClockwiseRotation.fromOrdinal((FACE_CORNER_ROTATION_MAP[quadrant.ordinal()] - 1) & 3), true, false));
				break;
			case TOP_RIGHT:
				polygon.rotation(layerIndex, TextureOrientation.FLIP_U);
				break;
			default:
				break;
			}
		}
	},

	/**
	 * Small closing corner on outside edge of L connections.
	 */
	OUTSIDE_CORNER(1),

	/**
	 * Will position texture to border the left (counter-clockwise) side of a
	 * quadrant
	 */
	SIDE_LEFT(2),

	/**
	 * Will position texture to border the right (clockwise) side of a quadrant
	 */
	SIDE_RIGHT(1) {
		@Override
		public void applyForQuadrant(MutablePolygon polygon, int layerIndex, FaceCorner quadrant) {
			super.applyForQuadrant(polygon, layerIndex, quadrant);
			final int i = quadrant.rightSide.ordinal();
			polygon.offsetVertexUV(layerIndex, RIGHT_SIDE_U_SHIFT[i], RIGHT_SIDE_V_SHIFT[i]);
		}
	},

	/**
	 * Inside edge of L connections.
	 */
	INSIDE_CORNER(3);

	/**
	 * Rotations needed to position this portion of the texture at upper left. For
	 * the SIDE_RIGHT texture, this puts the texture on the right side of the
	 * texture but it also requires a uv shift to display properly.
	 */
	public final int rotation;

	RotatableCableQuadrant(int rotation) {
		this.rotation = rotation;
	}

	/**
	 * Maps face corner value to rotations from upper left. Values correspond to
	 * Rotation ordinals.
	 */
	private static final int FACE_CORNER_ROTATION_MAP[] = new int[FaceCorner.values().length];

	/**
	 * Holds u offset when mapping the side texture to the right side of a quadrant
	 */
	private static final float RIGHT_SIDE_U_SHIFT[] = new float[FaceEdge.values().length];

	/**
	 * Holds v offset when mapping the side texture to the right side of a quadrant
	 */
	private static final float RIGHT_SIDE_V_SHIFT[] = new float[FaceEdge.values().length];

	static {
		FACE_CORNER_ROTATION_MAP[FaceCorner.TOP_LEFT.ordinal()] = ClockwiseRotation.ROTATE_NONE.ordinal();
		FACE_CORNER_ROTATION_MAP[FaceCorner.TOP_RIGHT.ordinal()] = ClockwiseRotation.ROTATE_90.ordinal();
		FACE_CORNER_ROTATION_MAP[FaceCorner.BOTTOM_RIGHT.ordinal()] = ClockwiseRotation.ROTATE_180.ordinal();
		FACE_CORNER_ROTATION_MAP[FaceCorner.BOTTOM_LEFT.ordinal()] = ClockwiseRotation.ROTATE_270.ordinal();

		RIGHT_SIDE_U_SHIFT[FaceEdge.BOTTOM_EDGE.ordinal()] = -0.5f;
		RIGHT_SIDE_U_SHIFT[FaceEdge.TOP_EDGE.ordinal()] = 0.5f;
		RIGHT_SIDE_V_SHIFT[FaceEdge.LEFT_EDGE.ordinal()] = -0.5f;
		RIGHT_SIDE_V_SHIFT[FaceEdge.RIGHT_EDGE.ordinal()] = 0.5f;
	}

	/**
	 * Applies the texture rotation needed to position this texture in
	 * the quadrant identified by the given corner.
	 */
	public void applyForQuadrant(MutablePolygon polygon, int layerIndex, FaceCorner quadrant) {
		polygon.rotation(layerIndex, TextureOrientation.find(ClockwiseRotation.fromOrdinal((4 + FACE_CORNER_ROTATION_MAP[quadrant.ordinal()] - rotation) & 3), false, false));
	}
}
