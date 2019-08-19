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

package grondag.xm.painter;

import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.state.CornerJoinFaceStates;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.orientation.FaceCorner;
import grondag.xm.api.paint.TextureQuadrant;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureSet;
import net.minecraft.util.math.Direction;

/**
 * Applies quadrant-style border textures. Quads must have a nominal face. Will
 * split quads that span quadrants.
 */
public abstract class CubicPainterQuadrants extends AbstractQuadPainter {
    private static final TextureQuadrant[][] TEXTURE_MAP = new TextureQuadrant[FaceCorner.values().length][CornerJoinFaceStates.COUNT];

    private static TextureQuadrant textureMap(FaceCorner corner, CornerJoinFaceState faceState) {
        if (faceState.isJoined(corner.leftSide)) {
            if (faceState.isJoined(corner.rightSide))
                return faceState.needsCorner(corner) ? TextureQuadrant.CORNER : TextureQuadrant.FULL;
            else
                return TextureQuadrant.SIDE_RIGHT;
        } else if (faceState.isJoined(corner.rightSide))
            return TextureQuadrant.SIDE_LEFT;
        else
            return TextureQuadrant.ROUND;
    }

    static {
        for (FaceCorner corner : FaceCorner.values()) {
            CornerJoinFaceStates.forEach(faceState -> {
                TEXTURE_MAP[corner.ordinal()][faceState.ordinal()] = textureMap(corner, faceState);
            });
        }
    }

    @SuppressWarnings("rawtypes")
    public static void paintQuads(MutableMesh stream, PrimitiveModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
        MutablePolygon editor = stream.editor();

        do {
            editor.lockUV(textureIndex, true);
            editor.assignLockedUVCoordinates(textureIndex);

            final FaceCorner quadrant = QuadrantSplitter.uvQuadrant(editor, textureIndex);
            if (quadrant == null) {
                QuadrantSplitter.split(stream, textureIndex);
                // skip the (now-deleted) original and paint split outputs later in loop
                assert editor.isDeleted();
                continue;
            }

            final Direction nominalFace = editor.nominalFace();
            TextureSet tex = paint.texture(textureIndex);
            final int textureVersion = tex.versionMask() & (textureHashForFace(nominalFace, tex, modelState) >> (quadrant.ordinal() * 4));

            editor.sprite(textureIndex, tex.textureName(textureVersion));
            editor.contractUV(textureIndex, true);

            final CornerJoinFaceState faceState = modelState.cornerJoin().faceState(nominalFace);

            TEXTURE_MAP[quadrant.ordinal()][faceState.ordinal()].applyForQuadrant(editor, textureIndex, quadrant);

            commonPostPaint(editor, modelState, surface, paint, textureIndex);

        } while (stream.editorNext());
    }
}
