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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.orientation.FaceCorner;
import grondag.xm.api.orientation.FaceEdge;
import grondag.xm.api.texture.TextureOrientation;

/**
 * Identifies content of each quadrant within texture file, starting at top
 * left.
 */
@API(status = EXPERIMENTAL)
public enum TextureQuadrant {
    /**
     * Will position texture to display no border. Should not be used unless texture
     * is configured to render no border as a tile.
     */
    FULL(0),

    /**
     * Will position texture to border both side of a quadrant, with no corner.
     */
    ROUND(1),

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
     * Will position texture to border both sides of a quadrant.
     */
    CORNER(3);

    /**
     * Rotations needed to position this portion of the texture at upper left. For
     * the SIDE_RIGHT texture, this puts the texture on the right side of the
     * texture but it also requires a uv shift to display properly.
     */
    public final int rotation;

    private TextureQuadrant(int rotation) {
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
        FACE_CORNER_ROTATION_MAP[FaceCorner.TOP_LEFT.ordinal()] = Rotation.ROTATE_NONE.ordinal();
        FACE_CORNER_ROTATION_MAP[FaceCorner.TOP_RIGHT.ordinal()] = Rotation.ROTATE_90.ordinal();
        FACE_CORNER_ROTATION_MAP[FaceCorner.BOTTOM_RIGHT.ordinal()] = Rotation.ROTATE_180.ordinal();
        FACE_CORNER_ROTATION_MAP[FaceCorner.BOTTOM_LEFT.ordinal()] = Rotation.ROTATE_270.ordinal();

        RIGHT_SIDE_U_SHIFT[FaceEdge.BOTTOM_EDGE.ordinal()] = -0.5f;
        RIGHT_SIDE_U_SHIFT[FaceEdge.TOP_EDGE.ordinal()] = 0.5f;
        RIGHT_SIDE_V_SHIFT[FaceEdge.LEFT_EDGE.ordinal()] = -0.5f;
        RIGHT_SIDE_V_SHIFT[FaceEdge.RIGHT_EDGE.ordinal()] = 0.5f;
    }

    /**
     * Applies the texture rotation needed to position this texture in the quadrant
     * identified by the given corner.
     * <p>
     */
    public void applyForQuadrant(MutablePolygon polygon, int layerIndex, FaceCorner quadrant) {
        polygon.rotation(layerIndex, TextureOrientation.find(Rotation.VALUES[(4 + FACE_CORNER_ROTATION_MAP[quadrant.ordinal()] - this.rotation) & 3], false, false));
    }
}
