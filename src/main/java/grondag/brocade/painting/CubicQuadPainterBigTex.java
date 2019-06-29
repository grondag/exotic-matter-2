package grondag.brocade.painting;

import grondag.brocade.api.texture.TextureRotation;
import grondag.brocade.api.texture.TextureScale;
import grondag.brocade.api.texture.TextureSet;
import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.stream.IMutablePolyStream;
import grondag.brocade.state.MeshState;
import grondag.fermion.varia.Useful;
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
    public static void paintQuads(IMutablePolyStream stream, MeshState modelState, PaintLayer paintLayer) {
        IMutablePolygon editor = stream.editor();
        do {
            int layerIndex = firstAvailableTextureLayer(editor);
            editor.setLockUV(layerIndex, true);
            editor.assignLockedUVCoordinates(layerIndex);

            final Direction nominalFace = editor.nominalFace();
            final TextureSet tex = getTexture(modelState, paintLayer);
            final boolean allowTexRotation = tex.rotation() != TextureRotation.ROTATE_NONE;
            final TextureScale scale = tex.scale();

            Vec3i surfaceVec = getSurfaceVector(modelState.getPosX(), modelState.getPosY(), modelState.getPosZ(),
                    nominalFace, scale);

            if (tex.versionCount() == 1) {
                // no alternates, so do uv flip and offset and rotation based on depth & species
                // only

                // abs is necessary so that hash input components combine together properly
                // Small random numbers already have most bits set.
                int depthAndSpeciesHash = editor.getSurface().ignoreDepthForRandomization
                        ? HashCommon.mix((modelState.getSpecies() << 8) | editor.getTextureSalt())
                        : HashCommon.mix(Math.abs(surfaceVec.getZ()) | (modelState.getSpecies() << 8)
                                | (editor.getTextureSalt() << 12));

                // rotation
                editor.setRotation(layerIndex,
                        allowTexRotation ? Useful.offsetEnumValue(tex.rotation().rotation, depthAndSpeciesHash & 3)
                                : tex.rotation().rotation);

                surfaceVec = rotateFacePerspective(surfaceVec, editor.getRotation(layerIndex), scale);

                editor.setTextureName(layerIndex, tex.textureName(0));

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

                editor.setMinU(layerIndex, x * sliceIncrement);
                editor.setMaxU(layerIndex, editor.getMinU(layerIndex) + (flipU ? -sliceIncrement : sliceIncrement));

                editor.setMinV(layerIndex, y * sliceIncrement);
                editor.setMaxV(layerIndex, editor.getMinV(layerIndex) + (flipV ? -sliceIncrement : sliceIncrement));

            } else {
                // multiple texture versions, so do rotation and alternation normally, except
                // add additional variation for depth;

                // abs is necessary so that hash input components combine together properly
                // Small random numbers already have most bits set.
                final int depthHash = editor.getSurface().ignoreDepthForRandomization && editor.getTextureSalt() == 0
                        ? 0
                        : HashCommon.mix(Math.abs(surfaceVec.getZ()) | (editor.getTextureSalt() << 8));

                editor.setTextureName(layerIndex, tex.textureName(
                        (textureVersionForFace(nominalFace, tex, modelState) + depthHash) & tex.versionMask()));

                editor.setRotation(layerIndex,
                        allowTexRotation ? Useful.offsetEnumValue(textureRotationForFace(nominalFace, tex, modelState),
                                (depthHash >> 16) & 3) : textureRotationForFace(nominalFace, tex, modelState));

                surfaceVec = rotateFacePerspective(surfaceVec, editor.getRotation(layerIndex), scale);

                final float sliceIncrement = scale.sliceIncrement;

                editor.setMinU(layerIndex, surfaceVec.getX() * sliceIncrement);
                editor.setMaxU(layerIndex, editor.getMinU(layerIndex) + sliceIncrement);

                editor.setMinV(layerIndex, surfaceVec.getY() * sliceIncrement);
                editor.setMaxV(layerIndex, editor.getMinV(layerIndex) + sliceIncrement);
            }

            commonPostPaint(editor, layerIndex, modelState, paintLayer);

        } while (stream.editorNext());
    }
}
