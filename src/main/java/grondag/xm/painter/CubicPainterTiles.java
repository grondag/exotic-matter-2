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

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.fermion.spatial.Rotation;
import grondag.fermion.varia.Useful;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureSet;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.Direction;

@API(status = INTERNAL)
public abstract class CubicPainterTiles extends AbstractQuadPainter {
    @SuppressWarnings("rawtypes")
    public static void paintQuads(MutableMesh stream, PrimitiveModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
        MutablePolygon editor = stream.editor();
        do {
            editor.lockUV(textureIndex, true);
            editor.assignLockedUVCoordinates(textureIndex);

            final Direction nominalFace = editor.nominalFace();
            final TextureSet tex = paint.texture(textureIndex);

            Rotation rotation = textureRotationForFace(nominalFace, tex, modelState);
            int textureVersion = textureVersionForFace(nominalFace, tex, modelState);

            final int salt = editor.textureSalt();
            if (salt != 0) {
                int saltHash = HashCommon.mix(salt);
                rotation = Useful.offsetEnumValue(rotation, saltHash & 3);
                textureVersion = (textureVersion + (saltHash >> 2)) & tex.versionMask();
            }

            editor.rotation(textureIndex, rotation);
            editor.sprite(textureIndex, tex.textureName(textureVersion));
            editor.contractUV(textureIndex, true);

            commonPostPaint(editor, modelState, surface, paint, textureIndex);

        } while (stream.editorNext());
    }
}
