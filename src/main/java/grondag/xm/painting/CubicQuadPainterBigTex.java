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

import grondag.fermion.varia.Useful;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.texture.TextureRotation;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.mesh.polygon.IMutablePolygon;
import grondag.xm.mesh.stream.IMutablePolyStream;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public abstract class CubicQuadPainterBigTex extends QuadPainter {
    // Determine what type of randomizations to apply so that we have a different
    // appearance based on depth and species.
    // If we are applying a single texture, then we alternate by translating,
    // flipping and rotating the texture.
    // In this case, the normal variation logic in superclass does not apply.
    //
    // If the texture has alternates, we simply use the normal alternation/rotation
    // logic in super class.
    // But we won't don't translate because then we'd need a way to know the
    // texture version of adjacent volumes. This would be possible if we got a
    // reference to the
    // alternator array instead of the alternator result, but it would needlessly
    // complex.
    //
    // If the texture has alternates, we also vary the texture selection and, if
    // supported,
    // based on depth within the plane, to provide variation between adjacent
    // layers.
    // This depth-based variation can be disabled with a setting in the surface
    // instance.
    public static void paintQuads(IMutablePolyStream stream, ModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
        IMutablePolygon editor = stream.editor();
        do {
            editor.setLockUV(textureIndex, true);
            editor.assignLockedUVCoordinates(textureIndex);

            final Direction nominalFace = editor.nominalFace();
            final TextureSet tex = paint.texture(textureIndex);
            final boolean allowTexRotation = tex.rotation() != TextureRotation.ROTATE_NONE;
            final TextureScale scale = tex.scale();

            Vec3i surfaceVec = getSurfaceVector(modelState.posX(), modelState.posY(), modelState.posZ(), nominalFace, scale);

            if (tex.versionCount() == 1) {
                // no alternates, so do uv flip and offset and rotation based on depth & species
                // only

                // abs is necessary so that hash input components combine together properly
                // Small random numbers already have most bits set.
                int depthAndSpeciesHash = editor.surface().ignoreDepthForRandomization() ? HashCommon.mix((modelState.species() << 8) | editor.getTextureSalt())
                        : HashCommon.mix(Math.abs(surfaceVec.getZ()) | (modelState.species() << 8) | (editor.getTextureSalt() << 12));

                // rotation
                editor.setRotation(textureIndex,
                        allowTexRotation ? Useful.offsetEnumValue(tex.rotation().rotation, depthAndSpeciesHash & 3) : tex.rotation().rotation);

                surfaceVec = rotateFacePerspective(surfaceVec, editor.getRotation(textureIndex), scale);

                editor.setTextureName(textureIndex, tex.textureName(0));

                final int xOffset = (depthAndSpeciesHash >> 2) & scale.sliceCountMask;
                final int yOffset = (depthAndSpeciesHash >> 8) & scale.sliceCountMask;

                final int newX = (surfaceVec.getX() + xOffset) & scale.sliceCountMask;
                final int newY = (surfaceVec.getY() + yOffset) & scale.sliceCountMask;
                surfaceVec = new Vec3i(newX, newY, surfaceVec.getZ());

                final boolean flipU = allowTexRotation && (depthAndSpeciesHash & 256) == 0;
                final boolean flipV = allowTexRotation && (depthAndSpeciesHash & 512) == 0;

                final float sliceIncrement = scale.sliceIncrement;

                final int x = flipU ? scale.sliceCount - surfaceVec.getX() : surfaceVec.getX();
                final int y = flipV ? scale.sliceCount - surfaceVec.getY() : surfaceVec.getY();

                editor.setMinU(textureIndex, x * sliceIncrement);
                editor.setMaxU(textureIndex, editor.getMinU(textureIndex) + (flipU ? -sliceIncrement : sliceIncrement));

                editor.setMinV(textureIndex, y * sliceIncrement);
                editor.setMaxV(textureIndex, editor.getMinV(textureIndex) + (flipV ? -sliceIncrement : sliceIncrement));

            } else {
                // multiple texture versions, so do rotation and alternation normally, except
                // add additional variation for depth;

                // abs is necessary so that hash input components combine together properly
                // Small random numbers already have most bits set.
                final int depthHash = editor.surface().ignoreDepthForRandomization() && editor.getTextureSalt() == 0 ? 0
                        : HashCommon.mix(Math.abs(surfaceVec.getZ()) | (editor.getTextureSalt() << 8));

                editor.setTextureName(textureIndex, tex.textureName((textureVersionForFace(nominalFace, tex, modelState) + depthHash) & tex.versionMask()));

                editor.setRotation(textureIndex,
                        allowTexRotation ? Useful.offsetEnumValue(textureRotationForFace(nominalFace, tex, modelState), (depthHash >> 16) & 3)
                                : textureRotationForFace(nominalFace, tex, modelState));

                surfaceVec = rotateFacePerspective(surfaceVec, editor.getRotation(textureIndex), scale);

                final float sliceIncrement = scale.sliceIncrement;

                editor.setMinU(textureIndex, surfaceVec.getX() * sliceIncrement);
                editor.setMaxU(textureIndex, editor.getMinU(textureIndex) + sliceIncrement);

                editor.setMinV(textureIndex, surfaceVec.getY() * sliceIncrement);
                editor.setMaxV(textureIndex, editor.getMinV(textureIndex) + sliceIncrement);
            }

            commonPostPaint(editor, textureIndex, modelState, surface, paint);

        } while (stream.editorNext());
    }
}
