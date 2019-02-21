package grondag.brocade.mesh;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.collision.CubeCollisionHandler;
import grondag.exotic_matter.model.collision.ICollisionHandler;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.painting.SurfaceTopology;
import grondag.exotic_matter.model.primitives.FaceVertex;
import grondag.exotic_matter.model.primitives.PolyFactory;
import grondag.exotic_matter.model.primitives.QuadHelper;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelStateData;
import grondag.exotic_matter.model.state.StateFormat;
import grondag.exotic_matter.model.varia.SideShape;
import grondag.exotic_matter.model.varia.SimpleQuadBounds;
import grondag.exotic_matter.varia.BitPacker64;
import grondag.exotic_matter.varia.Color;
import grondag.exotic_matter.world.CornerJoinBlockState;
import grondag.exotic_matter.world.CornerJoinFaceState;
import grondag.exotic_matter.world.FaceSide;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SquareColumnMeshFactory extends ShapeMeshGenerator {
    public static final int MIN_CUTS = 1;
    public static final int MAX_CUTS = 3;

    private static final Surface INSTANCE_CUT = Surface.builder(SurfaceTopology.CUBIC).withAllowBorders(false)
            .withEnabledLayers(PaintLayer.CUT).build();

    private static final Surface INSTANCE_CUT_LAMP = Surface.builder(INSTANCE_CUT).withLampGradient(true)
            .withEnabledLayers(PaintLayer.CUT).build();

    private static final Surface INSTANCE_MAIN = Surface.builder(SurfaceTopology.CUBIC)
            .withDisabledLayers(PaintLayer.CUT, PaintLayer.LAMP).build();

    private static final Surface INSTANCE_LAMP = Surface.builder(SurfaceTopology.CUBIC)
            .withEnabledLayers(PaintLayer.LAMP).build();

    @SuppressWarnings("null")
    private static final BitPacker64<SquareColumnMeshFactory> STATE_PACKER = new BitPacker64<SquareColumnMeshFactory>(
            null, null);
    private static final BitPacker64<SquareColumnMeshFactory>.BooleanElement STATE_ARE_CUTS_ON_EDGE = STATE_PACKER
            .createBooleanElement();
    private static final BitPacker64<SquareColumnMeshFactory>.IntElement STATE_CUT_COUNT = STATE_PACKER
            .createIntElement(MIN_CUTS, MAX_CUTS);

    private static class FaceSpec {
        private final int cutCount;
//        private final boolean areCutsOnEdge;
        private final float cutWidth;
        private final float baseMarginWidth;
        private final float marginOffset;
        private final float cutDepth;

        private FaceSpec(int cutCount, boolean areCutsOnEdge) {
            this.cutCount = cutCount;
//            this.areCutsOnEdge = areCutsOnEdge;

            if (areCutsOnEdge) {
                cutWidth = 0.5f / (cutCount + 1);
                baseMarginWidth = 1.5f * cutWidth;
                marginOffset = -0.5f;
                cutDepth = cutWidth * 0.8f;
            } else {
                cutWidth = 0.5f / (cutCount + 2);
                baseMarginWidth = 2.5f * cutWidth;
                marginOffset = 0.5f;
                cutDepth = cutWidth / 2;
            }
        }
    }

    public SquareColumnMeshFactory() {
        super(StateFormat.BLOCK, ModelStateData.STATE_FLAG_NEEDS_CORNER_JOIN | ModelStateData.STATE_FLAG_HAS_AXIS,
                STATE_CUT_COUNT.setValue(3, STATE_ARE_CUTS_ON_EDGE.setValue(true, 0)));
    }

    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target) {
        FaceSpec spec = new FaceSpec(getCutCount(modelState), areCutsOnEdge(modelState));

        for (EnumFacing face : EnumFacing.VALUES) {
            this.makeFaceQuads(modelState, face, spec, target);
        }
    }

    @Override
    public @Nonnull ICollisionHandler collisionHandler() {
        return CubeCollisionHandler.INSTANCE;
    }

    @Override
    public boolean isCube(ISuperModelState modelState) {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block,
            ISuperModelState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState) {
        return 255;
    }

    @Override
    public BlockOrientationType orientationType(ISuperModelState modelState) {
        return BlockOrientationType.AXIS;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, EnumFacing side) {
        return SideShape.PARTIAL;
    }

    private void makeFaceQuads(ISuperModelState state, @Nullable EnumFacing face, FaceSpec spec,
            Consumer<IPolygon> target) {
        if (face == null)
            return;

        CornerJoinBlockState bjs = state.getCornerJoin();
        EnumFacing.Axis axis = state.getAxis();

        IMutablePolygon quadInputs = PolyFactory.COMMON_POOL.newPaintable(4);
        quadInputs.setLockUV(0, true);

        Surface cutSurface = state.isEmissive(PaintLayer.LAMP) ? INSTANCE_CUT_LAMP : INSTANCE_CUT;

        if (face.getAxis() == axis) {
            makeCapFace(face, quadInputs, bjs.getFaceJoinState(face), spec, axis, cutSurface, target);
        } else {
            makeSideFace(face, quadInputs, bjs.getFaceJoinState(face), spec, axis, cutSurface, target);
        }
        quadInputs.release();
    }

    private void makeSideFace(EnumFacing face, IMutablePolygon template, CornerJoinFaceState fjs, FaceSpec spec,
            EnumFacing.Axis axis, Surface cutSurface, Consumer<IPolygon> target) {
        if (fjs == CornerJoinFaceState.NO_FACE)
            return;

        EnumFacing topFace = QuadHelper.getAxisTop(axis);
        EnumFacing bottomFace = topFace.getOpposite();
        EnumFacing leftFace = QuadHelper.leftOf(face, topFace);
        EnumFacing rightFace = QuadHelper.rightOf(face, topFace);

        int actualCutCount = spec.cutCount;

        /** used to randomize cut textures */
        int salt = 1;

        boolean hasLeftJoin = fjs.isJoined(leftFace, face);
        boolean hasRightJoin = fjs.isJoined(rightFace, face);
        boolean hasTopJoin = fjs.isJoined(topFace, face);
        boolean hasBottomJoin = fjs.isJoined(bottomFace, face);

        if (hasLeftJoin)
            actualCutCount++;
        if (hasRightJoin)
            actualCutCount++;

        float leftMarginWidth = hasLeftJoin ? spec.marginOffset * spec.cutWidth : spec.baseMarginWidth;
        float rightMarginWidth = hasRightJoin ? spec.marginOffset * spec.cutWidth : spec.baseMarginWidth;
        float topCapHeight = hasTopJoin ? 0 : spec.baseMarginWidth;
        float bottomCapHeight = hasBottomJoin ? 0 : spec.baseMarginWidth;

        // bottom
        if (!hasBottomJoin) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, 0, 0, 1, bottomCapHeight, 0, topFace);
            target.accept(quad);
        }

        // top
        if (!hasTopJoin) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, 0, 1 - topCapHeight, 1, 1, 0, topFace);
            target.accept(quad);
        }

        // left margin
        if (leftMarginWidth > 0) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, 0, bottomCapHeight, leftMarginWidth, 1 - topCapHeight, 0, topFace);
            target.accept(quad);
        }

        // right margin
        if (rightMarginWidth > 0) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, 1 - rightMarginWidth, bottomCapHeight, 1, 1 - topCapHeight, 0, topFace);
            target.accept(quad);
        }

        // splines
        for (int i = 0; i < actualCutCount - 1; i++) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth, bottomCapHeight,
                    leftMarginWidth + spec.cutWidth * 2 * (i + 1), 1 - topCapHeight, 0, topFace);
            target.accept(quad);
        }

        // top left corner
        if (fjs.needsCorner(topFace, leftFace, face)) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 1 - spec.baseMarginWidth,
                    leftMarginWidth + spec.cutWidth, 1, 0, topFace);
            target.accept(quad);
        }

        // bottom left corner
        if (fjs.needsCorner(bottomFace, leftFace, face)) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 0, leftMarginWidth + spec.cutWidth,
                    spec.baseMarginWidth, 0, topFace);
            target.accept(quad);
        }

        // top right corner
        if (fjs.needsCorner(topFace, rightFace, face)) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 1 - spec.baseMarginWidth,
                    Math.min(1 - rightMarginWidth, 1), 1, 0, topFace);
            target.accept(quad);
        }

        // bottom right corner
        if (fjs.needsCorner(bottomFace, rightFace, face)) {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_MAIN);
            quad.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 0, Math.min(1 - rightMarginWidth, 1),
                    spec.baseMarginWidth, 0, topFace);
            target.accept(quad);
        }

        for (int i = 0; i < actualCutCount; i++) {
            float sx0 = Math.max(0, leftMarginWidth + spec.cutWidth * 2 * i);
            float sx1 = Math.min(1, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth);

            // left face
            if (sx0 > 0.0001f) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad, new SimpleQuadBounds(rightFace, bottomCapHeight, 1 - spec.cutDepth,
                        1 - topCapHeight, 1, 1 - sx0, face));
                target.accept(quad);
            }

            // right face
            if (sx1 < 0.9999) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad, new SimpleQuadBounds(leftFace, topCapHeight, 1 - spec.cutDepth,
                        1 - bottomCapHeight, 1, sx1, face));
                target.accept(quad);
            }

            // top face
            if (topCapHeight > 0) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad,
                        new SimpleQuadBounds(bottomFace, sx0, 1 - spec.cutDepth, sx1, 1, 1 - topCapHeight, face));
                target.accept(quad);
            }

            // bottom face
            if (bottomCapHeight > 0) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad, new SimpleQuadBounds(topFace, 1 - sx1, 1 - spec.cutDepth, 1 - sx0, 1,
                        1 - bottomCapHeight, face));
                target.accept(quad);
            }

            // top left corner
            if (fjs.needsCorner(topFace, leftFace, face)) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad, new SimpleQuadBounds(bottomFace, Math.max(leftMarginWidth, 0), 1 - spec.cutDepth,
                        leftMarginWidth + spec.cutWidth, 1, 1 - spec.baseMarginWidth, face));
                target.accept(quad);
            }

            // bottom left corner
            if (fjs.needsCorner(bottomFace, leftFace, face)) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad, new SimpleQuadBounds(topFace, 1 - leftMarginWidth - spec.cutWidth,
                        1 - spec.cutDepth, Math.min(1 - leftMarginWidth, 1), 1, 1 - spec.baseMarginWidth, face));
                target.accept(quad);

            }

            // top right corner
            if (fjs.needsCorner(topFace, rightFace, face)) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad, new SimpleQuadBounds(bottomFace, 1 - rightMarginWidth - spec.cutWidth,
                        1 - spec.cutDepth, Math.min(1 - rightMarginWidth, 1), 1, 1 - spec.baseMarginWidth, face));
                target.accept(quad);
            }

            // bottom right corner
            if (fjs.needsCorner(bottomFace, rightFace, face)) {
                IMutablePolygon quad = template.claimCopy(4);
                quad.setSurface(cutSurface);
                quad.setTextureSalt(salt++);
                setupCutSideQuad(quad, new SimpleQuadBounds(topFace, Math.max(rightMarginWidth, 0), 1 - spec.cutDepth,
                        rightMarginWidth + spec.cutWidth, 1, 1 - spec.baseMarginWidth, face));
                target.accept(quad);
            }
        }

        // inner lamp surface can be a single poly
        {
            IMutablePolygon quad = template.claimCopy(4);
            quad.setSurface(INSTANCE_LAMP);
            quad.setupFaceQuad(face, Math.max(0, leftMarginWidth), bottomCapHeight, Math.min(1, 1 - rightMarginWidth),
                    1 - topCapHeight, spec.cutDepth, topFace);
            target.accept(quad);
        }
    }

    private void makeCapFace(EnumFacing face, IMutablePolygon template, CornerJoinFaceState fjs, FaceSpec spec,
            EnumFacing.Axis axis, Surface cutSurface, Consumer<IPolygon> target) {
        if (fjs == CornerJoinFaceState.NO_FACE)
            return;

        /** used to randomize cut textures */
        int salt = 1;

        // lamp bottom can be a single poly
        {
            IMutablePolygon quad = template.claimCopy();
            quad.setSurface(INSTANCE_LAMP);
            quad.setupFaceQuad(face, fjs.isJoined(FaceSide.LEFT) ? 0 : spec.baseMarginWidth,
                    fjs.isJoined(FaceSide.BOTTOM) ? 0 : spec.baseMarginWidth,
                    fjs.isJoined(FaceSide.RIGHT) ? 1 : 1 - spec.baseMarginWidth,
                    fjs.isJoined(FaceSide.TOP) ? 1 : 1 - spec.baseMarginWidth, spec.cutDepth,
                    FaceSide.TOP.getRelativeFace(face));
            target.accept(quad);
        }

        // build quarter slice of cap for each side separately
        // specifications below are oriented with the side at top of cap face

        for (FaceSide joinSide : FaceSide.values()) {

            EnumFacing side = joinSide.getRelativeFace(face);

            if (fjs.isJoined(joinSide)) {
                // This side is joined, so connect cuts to other block on this side.

                // margin corner faces
                {
                    IMutablePolygon tri = template.claimCopy(3);
                    tri.setSurface(INSTANCE_MAIN);
                    tri.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(spec.baseMarginWidth, 1, 0), new FaceVertex(0, 1, 0), side);
                    target.accept(tri);
                }
                {
                    IMutablePolygon tri = template.claimCopy(3);
                    tri.setSurface(INSTANCE_MAIN);
                    tri.setupFaceQuad(face, new FaceVertex(1 - spec.baseMarginWidth, 1, 0),
                            new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(1, 1, 0), side);
                    target.accept(tri);
                }

                // margin corner sides
                {
                    IMutablePolygon quad = template.claimCopy(4);
                    quad.setSurface(cutSurface);
                    quad.setTextureSalt(salt++);
                    setupCutSideQuad(quad, new SimpleQuadBounds(QuadHelper.rightOf(face, side),
                            1 - spec.baseMarginWidth, 1 - spec.cutDepth, 1, 1, 1 - spec.baseMarginWidth, face));
                    target.accept(quad);
                }
                {
                    IMutablePolygon quad = template.claimCopy(4);
                    quad.setSurface(cutSurface);
                    quad.setTextureSalt(salt++);
                    setupCutSideQuad(quad, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0, 1 - spec.cutDepth,
                            spec.baseMarginWidth, 1, 1 - spec.baseMarginWidth, face));
                    target.accept(quad);
                }

                // splines
                for (int i = 0; i < spec.cutCount / 2; i++) {
                    float xLeft = spec.baseMarginWidth + (i * 2 + 1) * spec.cutWidth;
                    float xRight = Math.min(xLeft + spec.cutWidth, 0.5f);

                    {
                        IMutablePolygon quad = template.claimCopy(4);
                        quad.setSurface(INSTANCE_MAIN);
                        quad.setupFaceQuad(face, new FaceVertex(xLeft, 1 - xLeft, 0),
                                new FaceVertex(xRight, 1 - xRight, 0), new FaceVertex(xRight, 1, 0),
                                new FaceVertex(xLeft, 1, 0), side);
                        target.accept(quad);
                    }
                    {
                        // mirror on right side, reverse winding order
                        IMutablePolygon quad = template.claimCopy(4);
                        quad.setSurface(INSTANCE_MAIN);
                        quad.setupFaceQuad(face, new FaceVertex(1 - xRight, 1 - xRight, 0),
                                new FaceVertex(1 - xLeft, 1 - xLeft, 0), new FaceVertex(1 - xLeft, 1, 0),
                                new FaceVertex(1 - xRight, 1, 0), side);
                        target.accept(quad);
                    }

                    // cut sides
                    // with odd number of cuts, these overlap in middle, avoid with this check

                    if (xLeft < 0.4999) {
                        {
                            IMutablePolygon quad = template.claimCopy(4);
                            quad.setSurface(cutSurface);
                            quad.setTextureSalt(salt++);
                            setupCutSideQuad(quad, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0,
                                    1 - spec.cutDepth, xLeft, 1, xLeft, face));
                            target.accept(quad);
                        }
                        {
                            IMutablePolygon quad = template.claimCopy(4);
                            quad.setSurface(cutSurface);
                            quad.setTextureSalt(salt++);
                            setupCutSideQuad(quad, new SimpleQuadBounds(QuadHelper.rightOf(face, side), 1 - xLeft,
                                    1 - spec.cutDepth, 1, 1, xLeft, face));
                            target.accept(quad);
                        }
                    }
                    if (xRight < 0.4999) {
                        {
                            IMutablePolygon quad = template.claimCopy(4);
                            quad.setSurface(cutSurface);
                            quad.setTextureSalt(salt++);
                            setupCutSideQuad(quad, new SimpleQuadBounds(QuadHelper.rightOf(face, side), 1 - xRight,
                                    1 - spec.cutDepth, 1, 1, 1 - xRight, face));
                            target.accept(quad);
                        }
                        {
                            IMutablePolygon quad = template.claimCopy(4);
                            quad.setSurface(cutSurface);
                            quad.setTextureSalt(salt++);
                            setupCutSideQuad(quad, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0,
                                    1 - spec.cutDepth, xRight, 1, 1 - xRight, face));
                            target.accept(quad);
                        }
                    }
                }
            } else {
                // This side isn't joined, so don't connect cuts to other block on this side.

                {
                    // outer face
                    IMutablePolygon quad = template.claimCopy(4);
                    quad.setSurface(INSTANCE_MAIN);
                    quad.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(1, 1, 0), new FaceVertex(0, 1, 0), side);
                    target.accept(quad);
                }

                {
                    // outer cut sides
                    for (int i = 0; i < (spec.cutCount + 1) / 2; i++) {
                        float offset = spec.baseMarginWidth + (spec.cutWidth * 2 * i);

                        IMutablePolygon quad = template.claimCopy(4);
                        quad.setSurface(cutSurface);
                        quad.setTextureSalt(salt++);
                        setupCutSideQuad(quad, new SimpleQuadBounds(side.getOpposite(), offset, 1 - spec.cutDepth,
                                1 - offset, 1, 1 - offset, face));
                        target.accept(quad);

                    }
                }

                for (int i = 0; i < spec.cutCount / 2; i++) {
                    float offset = spec.baseMarginWidth + spec.cutWidth * (2 * i + 1);

                    {
                        // inner cut sides
                        IMutablePolygon quad = template.claimCopy(4);
                        quad.setSurface(cutSurface);
                        quad.setTextureSalt(salt++);
                        setupCutSideQuad(quad,
                                new SimpleQuadBounds(side, offset, 1 - spec.cutDepth, 1 - offset, 1, offset, face));
                        target.accept(quad);
                    }

                    {
                        // spline / center
                        IMutablePolygon quad = template.claimCopy(4);
                        quad.setSurface(INSTANCE_MAIN);
                        quad.setupFaceQuad(face,
                                new FaceVertex(Math.min(0.5f, offset + spec.cutWidth), 1 - offset - spec.cutWidth, 0),
                                new FaceVertex(Math.max(0.5f, 1 - offset - spec.cutWidth), 1 - offset - spec.cutWidth,
                                        0),
                                new FaceVertex(1 - offset, 1 - offset, 0), new FaceVertex(offset, 1 - offset, 0), side);
                        target.accept(quad);
                    }

                }
            }
        }
    }

    private void setupCutSideQuad(IMutablePolygon qi, SimpleQuadBounds qb) {
        final int glow = qi.getSurface().isLampGradient ? 128 : 0;

        qi.setupFaceQuad(qb.face, new FaceVertex.Colored(qb.x0, qb.y0, qb.depth, Color.WHITE, glow),
                new FaceVertex.Colored(qb.x1, qb.y0, qb.depth, Color.WHITE, glow),
                new FaceVertex.Colored(qb.x1, qb.y1, qb.depth, Color.WHITE, 0),
                new FaceVertex.Colored(qb.x0, qb.y1, qb.depth, Color.WHITE, 0), qb.topFace);
    }

    /**
     * If true, cuts in shape are on the block boundary. Reads value from static
     * shape bits in model state
     */
    public static boolean areCutsOnEdge(ISuperModelState modelState) {
        return STATE_ARE_CUTS_ON_EDGE.getValue(modelState.getStaticShapeBits());
    }

    /**
     * If true, cuts in shape are on the block boundary. Saves value in static shape
     * bits in model state
     */
    public static void setCutsOnEdge(boolean areCutsOnEdge, ISuperModelState modelState) {
        modelState.setStaticShapeBits(STATE_ARE_CUTS_ON_EDGE.setValue(areCutsOnEdge, modelState.getStaticShapeBits()));
    }

    /**
     * Number of cuts that appear on each face of model. Reads value from static
     * shape bits in model state
     */
    public static int getCutCount(ISuperModelState modelState) {
        return STATE_CUT_COUNT.getValue(modelState.getStaticShapeBits());
    }

    /**
     * Number of cuts that appear on each face of model. Saves value in static shape
     * bits in model state
     */
    public static void setCutCount(int cutCount, ISuperModelState modelState) {
        modelState.setStaticShapeBits(STATE_CUT_COUNT.setValue(cutCount, modelState.getStaticShapeBits()));
    }

    @Override
    public boolean hasLampSurface(ISuperModelState modelState) {
        return true;
    }

}
