package grondag.xm2.painting;

import grondag.fermion.varia.Useful;
import grondag.fermion.world.Rotation;
import grondag.xm2.api.texture.TextureSet;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.stream.IMutablePolyStream;
import grondag.xm2.state.ModelState;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.Direction;

public abstract class CubicQuadPainterTiles extends QuadPainter {
    public static void paintQuads(IMutablePolyStream stream, ModelState modelState, PaintLayer paintLayer) {
        IMutablePolygon editor = stream.editor();
        do {
            final int layerIndex = firstAvailableTextureLayer(editor);
            editor.setLockUV(layerIndex, true);
            editor.assignLockedUVCoordinates(layerIndex);

            final Direction nominalFace = editor.nominalFace();
            final TextureSet tex = getTexture(modelState, paintLayer);

            Rotation rotation = textureRotationForFace(nominalFace, tex, modelState);
            int textureVersion = textureVersionForFace(nominalFace, tex, modelState);

            final int salt = editor.getTextureSalt();
            if (salt != 0) {
                int saltHash = HashCommon.mix(salt);
                rotation = Useful.offsetEnumValue(rotation, saltHash & 3);
                textureVersion = (textureVersion + (saltHash >> 2)) & tex.versionMask();
            }

            editor.setRotation(layerIndex, rotation);
            editor.setTextureName(layerIndex, tex.textureName(textureVersion));
            editor.setShouldContractUVs(layerIndex, true);

            commonPostPaint(editor, layerIndex, modelState, paintLayer);

        } while (stream.editorNext());
    }
}
