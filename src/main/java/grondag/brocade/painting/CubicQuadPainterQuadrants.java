package grondag.brocade.painting;

import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.stream.IMutablePolyStream;
import grondag.brocade.model.state.ISuperModelState;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.fermion.world.CornerJoinFaceState;
import grondag.fermion.world.FaceCorner;
import net.minecraft.util.math.Direction;

/**
 * Applies quadrant-style border textures. Quads must have a nominal face. Will
 * split quads that span quadrants.
 */
public abstract class CubicQuadPainterQuadrants extends QuadPainter {
    private static final TextureQuadrant[][] TEXTURE_MAP = new TextureQuadrant[FaceCorner
            .values().length][CornerJoinFaceState.values().length];

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
            for (CornerJoinFaceState faceState : CornerJoinFaceState.values()) {
                TEXTURE_MAP[corner.ordinal()][faceState.ordinal()] = textureMap(corner, faceState);
            }
        }
    }

    public static void paintQuads(IMutablePolyStream stream, ISuperModelState modelState, PaintLayer paintLayer) {
        IMutablePolygon editor = stream.editor();

        do {
            int layerIndex = firstAvailableTextureLayer(editor);
            editor.setLockUV(layerIndex, true);
            editor.assignLockedUVCoordinates(layerIndex);

            final FaceCorner quadrant = QuadrantSplitter.uvQuadrant(editor, layerIndex);
            if (quadrant == null) {
                QuadrantSplitter.split(stream, layerIndex);
                // skip the (now-deleted) original and paint split outputs later in loop
                assert editor.isDeleted();
                continue;
            }

            final Direction nominalFace = editor.getNominalFace();
            ITexturePalette tex = getTexture(modelState, paintLayer);
            final int textureVersion = tex.versionMask()
                    & (textureHashForFace(nominalFace, tex, modelState) >> (quadrant.ordinal() * 4));

            editor.setTextureName(layerIndex, tex.textureName(textureVersion));
            editor.setShouldContractUVs(layerIndex, true);

            final CornerJoinFaceState faceState = modelState.getCornerJoin().getFaceJoinState(nominalFace);

            TEXTURE_MAP[quadrant.ordinal()][faceState.ordinal()].applyForQuadrant(editor, layerIndex, quadrant);

            commonPostPaint(editor, layerIndex, modelState, paintLayer);

        } while (stream.editorNext());
    }
}
