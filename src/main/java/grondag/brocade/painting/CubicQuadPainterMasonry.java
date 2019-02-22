package grondag.brocade.painting;

import grondag.exotic_matter.model.primitives.FaceQuadInputs;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.stream.IMutablePolyStream;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.world.Rotation;
import grondag.exotic_matter.world.SimpleJoin;
import grondag.exotic_matter.world.SimpleJoinFaceState;
import net.minecraft.util.EnumFacing;

public abstract class CubicQuadPainterMasonry extends QuadPainter {
    protected final static FaceQuadInputs[][] FACE_INPUTS = new FaceQuadInputs[EnumFacing.VALUES.length][SimpleJoinFaceState
            .values().length];

    private static enum Textures {
        BOTTOM_LEFT_RIGHT, BOTTOM_LEFT, LEFT_RIGHT, BOTTOM, ALL;
    }

    static {
        // mapping is unusual in that a join indicates a border IS present on texture
        for (EnumFacing face : EnumFacing.VALUES) {
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.NONE.ordinal()] = null; // new
                                                                                    // ImmutableList.Builder<BakedQuad>().build();
                                                                                    // // NO BORDER
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.NO_FACE.ordinal()] = null; // new
                                                                                       // ImmutableList.Builder<BakedQuad>().build();
                                                                                       // // NO BORDER

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT_RIGHT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_LEFT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT_RIGHT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT_RIGHT.ordinal(), Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_LEFT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_LEFT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_RIGHT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM_RIGHT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM_LEFT.ordinal(), Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT_RIGHT.ordinal()] = new FaceQuadInputs(
                    Textures.LEFT_RIGHT.ordinal(), Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP_BOTTOM.ordinal()] = new FaceQuadInputs(
                    Textures.LEFT_RIGHT.ordinal(), Rotation.ROTATE_90, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.BOTTOM.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM.ordinal(), Rotation.ROTATE_NONE, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.LEFT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM.ordinal(), Rotation.ROTATE_90, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.TOP.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM.ordinal(), Rotation.ROTATE_180, false, false);
            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.RIGHT.ordinal()] = new FaceQuadInputs(
                    Textures.BOTTOM.ordinal(), Rotation.ROTATE_270, false, false);

            FACE_INPUTS[face.ordinal()][SimpleJoinFaceState.ALL.ordinal()] = new FaceQuadInputs(Textures.ALL.ordinal(),
                    Rotation.ROTATE_NONE, false, false);
        }
    }

    public static void paintQuads(IMutablePolyStream stream, ISuperModelState modelState, PaintLayer paintLayer) {
        IMutablePolygon editor = stream.editor();
        do {
            final SimpleJoin bjs = modelState.getMasonryJoin();
            final EnumFacing face = editor.getNominalFace();
            final SimpleJoinFaceState fjs = SimpleJoinFaceState.find(face, bjs);
            final FaceQuadInputs inputs = FACE_INPUTS[face.ordinal()][fjs.ordinal()];

            // if can't identify a face, skip texturing
            if (inputs == null)
                return;

            final int layerIndex = firstAvailableTextureLayer(editor);
            editor.setLockUV(layerIndex, true);
            editor.assignLockedUVCoordinates(layerIndex);

            editor.setRotation(layerIndex, inputs.rotation);
            editor.setMinU(layerIndex, inputs.flipU ? 1 : 0);
            editor.setMinV(layerIndex, inputs.flipV ? 1 : 0);
            editor.setMaxU(layerIndex, inputs.flipU ? 0 : 1);
            editor.setMaxV(layerIndex, inputs.flipV ? 0 : 1);

            final ITexturePalette tex = getTexture(modelState, paintLayer);
            editor.setTextureName(layerIndex,
                    tex.textureName(textureVersionForFace(face, tex, modelState), inputs.textureOffset));

            commonPostPaint(editor, layerIndex, modelState, paintLayer);

        } while (stream.editorNext());
    }
}
