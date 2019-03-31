package grondag.brocade.painting;

import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.stream.IMutablePolyStream;
import grondag.brocade.model.state.ISuperModelState;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.fermion.varia.Useful;
import grondag.fermion.world.Rotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public abstract class CubicQuadPainterTiles extends QuadPainter {
    public static void paintQuads(IMutablePolyStream stream, ISuperModelState modelState, PaintLayer paintLayer) {
        IMutablePolygon editor = stream.editor();
        do {
            final int layerIndex = firstAvailableTextureLayer(editor);
            editor.setLockUV(layerIndex, true);
            editor.assignLockedUVCoordinates(layerIndex);

            final Direction nominalFace = editor.getNominalFace();
            final ITexturePalette tex = getTexture(modelState, paintLayer);

            Rotation rotation = textureRotationForFace(nominalFace, tex, modelState);
            int textureVersion = textureVersionForFace(nominalFace, tex, modelState);

            final int salt = editor.getTextureSalt();
            if (salt != 0) {
                int saltHash = MathHelper.hash(salt);
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
