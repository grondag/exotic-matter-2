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

package grondag.xm2.painting;

import static grondag.xm2.texture.api.TextureNameFunction.BORDER_CORNERS_ALL;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_CORNERS_BL_TR;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_CORNERS_BL_TR_BR;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_CORNERS_TL_TR;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_CORNER_TR;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_MIXED_TOP_BL_BR;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_MIXED_TOP_BR;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_MIXED_TOP_RIGHT_BL;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_NONE;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_SIDES_ALL;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_SIDES_TOP_BOTTOM;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_SIDES_TOP_LEFT_RIGHT;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_SIDES_TOP_RIGHT;
import static grondag.xm2.texture.api.TextureNameFunction.BORDER_SIDE_TOP;

import grondag.fermion.world.Rotation;
import grondag.xm2.connect.api.state.CornerJoinFaceStates;
import grondag.xm2.connect.api.state.CornerJoinState;
import grondag.xm2.primitives.FaceQuadInputs;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.stream.IMutablePolyStream;
import grondag.xm2.state.ModelState;
import grondag.xm2.texture.api.TextureSet;
import net.minecraft.util.math.Direction;

public abstract class CubicQuadPainterBorders extends QuadPainter {
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[6][CornerJoinFaceStates.COUNT];
    /**
     * Used only when a border is rendered in the solid layer. Declared at module
     * level so that we can check for it.
     */
    private final static FaceQuadInputs NO_BORDER = new FaceQuadInputs(BORDER_NONE,
            Rotation.ROTATE_NONE, false, false);

    static {
        for (Direction face : Direction.values()) {
            // First one will only be used if we are rendering in solid layer.
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_NO_CORNERS.ordinal()] = NO_BORDER;
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.NO_FACE.ordinal()] = null; // NULL FACE
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.NONE.ordinal()] = new FaceQuadInputs(BORDER_SIDES_ALL,
                    Rotation.ROTATE_NONE, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT,
                    Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.LEFT.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT,
                    Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT,
                    Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.RIGHT.ordinal()] = new FaceQuadInputs(BORDER_SIDES_TOP_LEFT_RIGHT,
                    Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDES_TOP_RIGHT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDES_TOP_RIGHT, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDES_TOP_RIGHT, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDES_TOP_RIGHT, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_NO_CORNERS
                    .ordinal()] = new FaceQuadInputs(BORDER_SIDE_TOP, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_NO_CORNERS.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDE_TOP, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDE_TOP, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDE_TOP, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.LEFT_RIGHT.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDES_TOP_BOTTOM, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM.ordinal()] = new FaceQuadInputs(
            		BORDER_SIDES_TOP_BOTTOM, Rotation.ROTATE_90, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_NONE, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_90, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_TL.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_TR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_180, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BR, Rotation.ROTATE_270, true, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BL_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL_BL.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BL_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_RIGHT_TL_TR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BL_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_BL_BR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_RIGHT_BL, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_LEFT_TL.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_RIGHT_BL, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.TOP_RIGHT_TR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_RIGHT_BL, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_MIXED_TOP_RIGHT_BL, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR,
                    Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_BR.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR,
                    Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_BL.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR,
                    Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL.ordinal()] = new FaceQuadInputs(BORDER_CORNER_TR,
                    Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_TL_TR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_TL_TR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_BL_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_TL_TR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_BL.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_TL_TR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR_BL.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_BL_TR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_BL_TR, Rotation.ROTATE_90, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TR_BL_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_BL_TR_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_BL_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_BL_TR_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR_BL.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_BL_TR_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_BL_TR_BR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceStates.ALL_TL_TR_BL_BR.ordinal()] = new FaceQuadInputs(
            		BORDER_CORNERS_ALL, Rotation.ROTATE_NONE, false, false);

            // rotate top face 180 so that it works - top face orientation is
            // different from others
            if (face == Direction.UP) {
                for (int i = 0; i < FACE_INPUTS[Direction.UP.ordinal()].length; i++) {
                    FaceQuadInputs fqi = FACE_INPUTS[Direction.UP.ordinal()][i];
                    if (fqi != null && fqi != NO_BORDER) {
                        FACE_INPUTS[Direction.UP.ordinal()][i] = new FaceQuadInputs(fqi.textureOffset,
                                fqi.rotation.clockwise().clockwise(), fqi.flipU, fqi.flipV);
                    }
                }
            }
        }
    }

    public static void paintQuads(IMutablePolyStream stream, ModelState modelState, PaintLayer paintLayer) {
        IMutablePolygon editor = stream.editor();
        do {

            CornerJoinState bjs = modelState.getCornerJoin();
            Direction face = editor.nominalFace();
            FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][bjs.faceState(face).ordinal()];

            // if can't identify a face, skip texturing
            if (inputs == null)
                continue;

            final TextureSet tex = getTexture(modelState, paintLayer);

            // don't render the "no border" texture unless this is a tile of some kind
            if (inputs == NO_BORDER && !tex.renderNoBorderAsTile())
                continue;

            int layerIndex = firstAvailableTextureLayer(editor);
            editor.setLockUV(layerIndex, true);
            editor.assignLockedUVCoordinates(layerIndex);

            editor.setRotation(layerIndex, inputs.rotation);
//            cubeInputs.rotateBottom = false;
            editor.setMinU(layerIndex, inputs.flipU ? 1 : 0);
            editor.setMinV(layerIndex, inputs.flipV ? 1 : 0);
            editor.setMaxU(layerIndex, inputs.flipU ? 0 : 1);
            editor.setMaxV(layerIndex, inputs.flipV ? 0 : 1);
            editor.setTextureName(layerIndex,
                    tex.textureName(textureVersionForFace(face, tex, modelState), inputs.textureOffset));

            commonPostPaint(editor, layerIndex, modelState, paintLayer);

        } while (stream.editorNext());
    }
}