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

package grondag.xm.painting;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.connect.state.SimpleJoinFaceState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.model.ModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.mesh.helper.FaceQuadInputs;
import grondag.xm.mesh.polygon.IMutablePolygon;
import grondag.xm.mesh.stream.IMutablePolyStream;
import net.minecraft.util.math.Direction;

public abstract class CubicQuadPainterMasonry extends QuadPainter {
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[6][SimpleJoinFaceState.values().length];

    private static enum Textures {
        BOTTOM_LEFT_RIGHT, BOTTOM_LEFT, LEFT_RIGHT, BOTTOM, ALL;
    }

    static {
        // mapping is unusual in that a join indicates a border IS present on texture
        for (Direction face : Direction.values()) {
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.NO_FACE.ordinal()] = null;

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
                    Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
                    Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
                    Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT_RIGHT.ordinal(),
                    Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_NONE,
                    false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_90, false,
                    false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_180,
                    false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_270,
                    false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT_RIGHT.ordinal()] = new FaceQuadInputs(Textures.LEFT_RIGHT.ordinal(), Rotation.ROTATE_NONE,
                    false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM.ordinal()] = new FaceQuadInputs(Textures.LEFT_RIGHT.ordinal(), Rotation.ROTATE_90, false,
                    false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), Rotation.ROTATE_NONE, false,
                    false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.RIGHT.ordinal()] = new FaceQuadInputs(Textures.BOTTOM.ordinal(), Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.ALL.ordinal()] = new FaceQuadInputs(Textures.ALL.ordinal(), Rotation.ROTATE_NONE, false, false);
        }
    }

    public static void paintQuads(IMutablePolyStream stream, ModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
        IMutablePolygon editor = stream.editor();
        do {
            final SimpleJoinState bjs = modelState.masonryJoin();
            final Direction face = editor.nominalFace();
            final SimpleJoinFaceState fjs = SimpleJoinFaceState.find(face, bjs);
            final FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][fjs.ordinal()];

            // if can't identify a face, skip texturing
            if (inputs == null)
                return;

            editor.setLockUV(textureIndex, true);
            editor.assignLockedUVCoordinates(textureIndex);

            editor.setRotation(textureIndex, inputs.rotation);
            editor.setMinU(textureIndex, inputs.flipU ? 1 : 0);
            editor.setMinV(textureIndex, inputs.flipV ? 1 : 0);
            editor.setMaxU(textureIndex, inputs.flipU ? 0 : 1);
            editor.setMaxV(textureIndex, inputs.flipV ? 0 : 1);

            final TextureSet tex = paint.texture(textureIndex);
            editor.setTextureName(textureIndex, tex.textureName(textureVersionForFace(face, tex, modelState), inputs.textureOffset));

            commonPostPaint(editor, textureIndex, modelState, surface, paint);

        } while (stream.editorNext());
    }
}
