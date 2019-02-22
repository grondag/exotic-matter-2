package grondag.brocade.painting;

import grondag.exotic_matter.model.primitives.FaceQuadInputs;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.stream.IMutablePolyStream;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.world.CornerJoinBlockState;
import grondag.exotic_matter.world.CornerJoinFaceState;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.EnumFacing;

public abstract class CubicQuadPainterBorders extends QuadPainter {
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[EnumFacing.VALUES.length][CornerJoinFaceState
            .values().length];

    /** Texture offsets */
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT = 0;
    private final static int TEXTURE_BOTTOM_LEFT = 1;
    private final static int TEXTURE_LEFT_RIGHT = 2;
    private final static int TEXTURE_BOTTOM = 3;
    private final static int TEXTURE_JOIN_NONE = 4;
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT_BR = 5;
    private final static int TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR = 6;
    private final static int TEXTURE_BOTTOM_LEFT_BL = 7;
    private final static int TEXTURE_JOIN_ALL_TR = 8;
    private final static int TEXTURE_JOIN_ALL_TL_TR = 9;
    private final static int TEXTURE_JOIN_ALL_TR_BL = 10;
    private final static int TEXTURE_JOIN_ALL_TR_BL_BR = 11;
    private final static int TEXTURE_JOIN_ALL_ALL_CORNERS = 12;
    // this last one will be a blank texture unless this is a completed texture vs
    // just a border
    private final static int TEXTURE_JOIN_ALL_NO_CORNERS = 13;

    /**
     * Used only when a border is rendered in the solid layer. Declared at module
     * level so that we can check for it.
     */
    private final static FaceQuadInputs NO_BORDER = new FaceQuadInputs(TEXTURE_JOIN_ALL_NO_CORNERS,
            Rotation.ROTATE_NONE, false, false);

    static {
        for (EnumFacing face : EnumFacing.VALUES) {
            // First one will only be used if we are rendering in solid layer.
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_NO_CORNERS.ordinal()] = NO_BORDER;
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.NO_FACE.ordinal()] = null; // NULL FACE
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.NONE.ordinal()] = new FaceQuadInputs(TEXTURE_JOIN_NONE,
                    Rotation.ROTATE_NONE, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM.ordinal()] = new FaceQuadInputs(TEXTURE_BOTTOM,
                    Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.LEFT.ordinal()] = new FaceQuadInputs(TEXTURE_BOTTOM,
                    Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP.ordinal()] = new FaceQuadInputs(TEXTURE_BOTTOM,
                    Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.RIGHT.ordinal()] = new FaceQuadInputs(TEXTURE_BOTTOM,
                    Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_NO_CORNER.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_RIGHT_NO_CORNER.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_NO_CORNERS
                    .ordinal()] = new FaceQuadInputs(TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_NO_CORNERS.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_NO_CORNERS.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.LEFT_RIGHT.ordinal()] = new FaceQuadInputs(
                    TEXTURE_LEFT_RIGHT, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM.ordinal()] = new FaceQuadInputs(
                    TEXTURE_LEFT_RIGHT, Rotation.ROTATE_90, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_NONE, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_TL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_90, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_180, true, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_TR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_270, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BR, Rotation.ROTATE_270, true, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_RIGHT_BL_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_LEFT_TL_BL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_RIGHT_TL_TR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_BOTTOM_RIGHT_TR_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_RIGHT_BL_BR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_LEFT_BL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_LEFT_TL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.TOP_RIGHT_TR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.BOTTOM_RIGHT_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_BOTTOM_LEFT_BL, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR.ordinal()] = new FaceQuadInputs(TEXTURE_JOIN_ALL_TR,
                    Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BR.ordinal()] = new FaceQuadInputs(TEXTURE_JOIN_ALL_TR,
                    Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BL.ordinal()] = new FaceQuadInputs(TEXTURE_JOIN_ALL_TR,
                    Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL.ordinal()] = new FaceQuadInputs(TEXTURE_JOIN_ALL_TR,
                    Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_BL_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TL_TR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TR_BL, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TR_BL, Rotation.ROTATE_90, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TR_BL_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_BL_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BL.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_TR_BL_BR, Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][CornerJoinFaceState.ALL_TL_TR_BL_BR.ordinal()] = new FaceQuadInputs(
                    TEXTURE_JOIN_ALL_ALL_CORNERS, Rotation.ROTATE_NONE, false, false);

            // rotate bottom face 180 so that it works - bottom face orientation is
            // different from others
            if (face == EnumFacing.DOWN) {
                for (int i = 0; i < FACE_INPUTS[EnumFacing.DOWN.ordinal()].length; i++) {
                    FaceQuadInputs fqi = FACE_INPUTS[EnumFacing.DOWN.ordinal()][i];
                    if (fqi != null && fqi != NO_BORDER) {
                        FACE_INPUTS[EnumFacing.DOWN.ordinal()][i] = new FaceQuadInputs(fqi.textureOffset,
                                fqi.rotation.clockwise().clockwise(), fqi.flipU, fqi.flipV);
                    }
                }
            }
        }
    }

    public static void paintQuads(IMutablePolyStream stream, ISuperModelState modelState, PaintLayer paintLayer) {
        IMutablePolygon editor = stream.editor();
        do {

            CornerJoinBlockState bjs = modelState.getCornerJoin();
            EnumFacing face = editor.getNominalFace();
            FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][bjs.getFaceJoinState(face).ordinal()];

            // if can't identify a face, skip texturing
            if (inputs == null)
                continue;

            final ITexturePalette tex = getTexture(modelState, paintLayer);

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
