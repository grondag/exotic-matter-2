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
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureTransform;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

@API(status = INTERNAL)
public abstract class CubicPainterBigTex extends AbstractQuadPainter {
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
    @SuppressWarnings("rawtypes")
    public static void paintQuads(MutableMesh mesh, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
        MutablePolygon editor = mesh.editor();
        do {
            editor.lockUV(textureIndex, true);
            editor.assignLockedUVCoordinates(textureIndex);

            final Direction nominalFace = editor.nominalFace();
            final TextureSet tex = paint.texture(textureIndex);
            final boolean allowTexRotation = tex.transform() != TextureTransform.IDENTITY;
            final TextureScale scale = tex.scale();

            Vec3i surfaceVec = getSurfaceVector(modelState.posX(), modelState.posY(), modelState.posZ(), nominalFace, scale);

            if (tex.versionCount() == 1) {
                // no alternates, so do uv flip and offset and rotation based on depth & species
                // only

                // abs is necessary so that hash input components combine together properly
                // Small random numbers already have most bits set.
                int depthAndSpeciesHash = editor.surface().ignoreDepthForRandomization() ? HashCommon.mix((modelState.species() << 8) | editor.textureSalt())
                        : HashCommon.mix(Math.abs(surfaceVec.getZ()) | (modelState.species() << 8) | (editor.textureSalt() << 12));

                // rotation
                final Rotation rot = allowTexRotation ? Useful.offsetEnumValue(tex.transform().baseRotation, depthAndSpeciesHash & 3) : tex.transform().baseRotation;
                editor.rotation(textureIndex,TextureOrientation.find(rot, false, false));

                surfaceVec = rotateFacePerspective(surfaceVec, rot, scale);

                editor.sprite(textureIndex, tex.textureName(0));

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

                editor.minU(textureIndex, x * sliceIncrement);
                editor.maxU(textureIndex, editor.minU(textureIndex) + (flipU ? -sliceIncrement : sliceIncrement));

                editor.minV(textureIndex, y * sliceIncrement);
                editor.maxV(textureIndex, editor.minV(textureIndex) + (flipV ? -sliceIncrement : sliceIncrement));

            } else {
                // multiple texture versions, so do rotation and alternation normally, except
                // add additional variation for depth;

                // abs is necessary so that hash input components combine together properly
                // Small random numbers already have most bits set.
                final int depthHash = editor.surface().ignoreDepthForRandomization() && editor.textureSalt() == 0 ? 0
                        : HashCommon.mix(Math.abs(surfaceVec.getZ()) | (editor.textureSalt() << 8));

                editor.sprite(textureIndex, tex.textureName((textureVersionForFace(nominalFace, tex, modelState) + depthHash) & tex.versionMask()));

                editor.rotation(textureIndex,
                        allowTexRotation ? Useful.offsetEnumValue(textureRotationForFace(nominalFace, tex, modelState), (depthHash >> 16) & 3)
                                : textureRotationForFace(nominalFace, tex, modelState));

                surfaceVec = rotateFacePerspective(surfaceVec, editor.rotation(textureIndex).rotation, scale);

                final float sliceIncrement = scale.sliceIncrement;

                editor.minU(textureIndex, surfaceVec.getX() * sliceIncrement);
                editor.maxU(textureIndex, editor.minU(textureIndex) + sliceIncrement);

                editor.minV(textureIndex, surfaceVec.getY() * sliceIncrement);
                editor.maxV(textureIndex, editor.minV(textureIndex) + sliceIncrement);
            }

            commonPostPaint(editor, modelState, surface, paint, textureIndex);

        } while (editor.next());
    }
}
