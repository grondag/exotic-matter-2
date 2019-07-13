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

package grondag.xm2.mesh;

import java.util.function.Consumer;

import grondag.fermion.color.Color;
import grondag.fermion.varia.BitPacker64;
import grondag.fermion.varia.DirectionHelper;
import grondag.xm2.connect.api.model.FaceEdge;
import grondag.xm2.connect.api.state.CornerJoinFaceState;
import grondag.xm2.connect.api.state.CornerJoinFaceStates;
import grondag.xm2.connect.api.state.CornerJoinState;
import grondag.xm2.dispatch.SimpleQuadBounds;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.primitives.FaceVertex;
import grondag.xm2.primitives.QuadHelper;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.IWritablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.ModelStateData;
import grondag.xm2.state.StateFormat;
import grondag.xm2.surface.api.XmSurface;
import grondag.xm2.surface.impl.XmSurfaceImpl;
import grondag.xm2.surface.impl.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Direction;

public class SquareColumnMeshFactory extends MeshFactory {
	public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
			.add("main", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("cut", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP_GRADIENT)
			.add("lamp", SurfaceTopology.CUBIC, 0)
			.build();
	
	public static final XmSurfaceImpl SURFACE_MAIN = SURFACES.get(0);
	public static final XmSurfaceImpl SURFACE_CUT = SURFACES.get(1);
	public static final XmSurfaceImpl SURFACE_LAMP = SURFACES.get(2);
	
    public static final int MIN_CUTS = 1;
    public static final int MAX_CUTS = 3;

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
        super(SURFACES, StateFormat.BLOCK, ModelStateData.STATE_FLAG_NEEDS_CORNER_JOIN | ModelStateData.STATE_FLAG_HAS_AXIS,
                STATE_CUT_COUNT.setValue(3, STATE_ARE_CUTS_ON_EDGE.setValue(true, 0)));
    }

    @Override
    public void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target) {
        FaceSpec spec = new FaceSpec(getCutCount(modelState), areCutsOnEdge(modelState));
        for (int i = 0; i < 6; i++) {
            this.makeFaceQuads(modelState, DirectionHelper.fromOrdinal(i), spec, target);
        }
    }

    @Override
    public boolean isCube(ModelState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState) {
        return 255;
    }

    @Override
    public BlockOrientationType orientationType(ModelState modelState) {
        return BlockOrientationType.AXIS;
    }

    private void makeFaceQuads(ModelState state, Direction face, FaceSpec spec, Consumer<IPolygon> target) {
        if (face == null)
            return;

        // PERF: if have a consumer and doing this dynamically - should consumer simply be a stream?
        // Why create a stream just to pipe it to the consumer?  Or cache the result.
        
        CornerJoinState bjs = state.getCornerJoin();
        Direction.Axis axis = state.getAxis();
        IWritablePolyStream stream = PolyStreams.claimWritable();
        
        stream.setVertexCount(4);
        stream.writer().setLockUV(0, true);
        stream.saveDefaults();
        
        //FIXME: find a way to handle lamp gradient
        //Surface cutSurface = state.isEmissive(PaintLayer.LAMP) ? INSTANCE_CUT_LAMP : INSTANCE_CUT;
        XmSurfaceImpl cutSurface = SURFACE_CUT;
        
        if (face.getAxis() == axis) {
            makeCapFace(face, stream, bjs.faceState(face), spec, axis, cutSurface);
        } else {
            makeSideFace(face, stream, bjs.faceState(face), spec, axis, cutSurface);
        }
        
        if (stream.origin()) {
            IPolygon reader = stream.reader();

            do
                target.accept(reader);
            while (stream.next());
        }
        stream.release();
    }

    private void makeSideFace(Direction face, IWritablePolyStream stream, CornerJoinFaceState fjs, FaceSpec spec,
            Direction.Axis axis, XmSurfaceImpl cutSurface) {
        if (fjs == CornerJoinFaceStates.NO_FACE)
            return;

        final IMutablePolygon writer = stream.writer();
        
        Direction topFace = QuadHelper.getAxisTop(axis);
        Direction bottomFace = topFace.getOpposite();
        Direction leftFace = QuadHelper.leftOf(face, topFace);
        Direction rightFace = QuadHelper.rightOf(face, topFace);

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
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, 0, 0, 1, bottomCapHeight, 0, topFace);
            stream.append();
        }

        // top
        if (!hasTopJoin) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, 0, 1 - topCapHeight, 1, 1, 0, topFace);
            stream.append();
        }

        // left margin
        if (leftMarginWidth > 0) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, 0, bottomCapHeight, leftMarginWidth, 1 - topCapHeight, 0, topFace);
            stream.append();
        }

        // right margin
        if (rightMarginWidth > 0) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, 1 - rightMarginWidth, bottomCapHeight, 1, 1 - topCapHeight, 0, topFace);
            stream.append();
        }

        // splines
        for (int i = 0; i < actualCutCount - 1; i++) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth, bottomCapHeight,
                    leftMarginWidth + spec.cutWidth * 2 * (i + 1), 1 - topCapHeight, 0, topFace);
            stream.append();
        }

        // top left corner
        if (fjs.needsCorner(topFace, leftFace, face)) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 1 - spec.baseMarginWidth,
                    leftMarginWidth + spec.cutWidth, 1, 0, topFace);
            stream.append();
        }

        // bottom left corner
        if (fjs.needsCorner(bottomFace, leftFace, face)) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 0, leftMarginWidth + spec.cutWidth,
                    spec.baseMarginWidth, 0, topFace);
            stream.append();
        }

        // top right corner
        if (fjs.needsCorner(topFace, rightFace, face)) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 1 - spec.baseMarginWidth,
                    Math.min(1 - rightMarginWidth, 1), 1, 0, topFace);
            stream.append();
        }

        // bottom right corner
        if (fjs.needsCorner(bottomFace, rightFace, face)) {
            writer.surface(SURFACE_MAIN);
            writer.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 0, Math.min(1 - rightMarginWidth, 1),
                    spec.baseMarginWidth, 0, topFace);
            stream.append();
        }

        for (int i = 0; i < actualCutCount; i++) {
            float sx0 = Math.max(0, leftMarginWidth + spec.cutWidth * 2 * i);
            float sx1 = Math.min(1, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth);

            // left face
            if (sx0 > 0.0001f) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(rightFace, bottomCapHeight, 1 - spec.cutDepth,
                        1 - topCapHeight, 1, 1 - sx0, face));
                stream.append();
            }

            // right face
            if (sx1 < 0.9999) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(leftFace, topCapHeight, 1 - spec.cutDepth,
                        1 - bottomCapHeight, 1, sx1, face));
                stream.append();
            }

            // top face
            if (topCapHeight > 0) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer,
                        new SimpleQuadBounds(bottomFace, sx0, 1 - spec.cutDepth, sx1, 1, 1 - topCapHeight, face));
                stream.append();
            }

            // bottom face
            if (bottomCapHeight > 0) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(topFace, 1 - sx1, 1 - spec.cutDepth, 1 - sx0, 1,
                        1 - bottomCapHeight, face));
                stream.append();
            }

            // top left corner
            if (fjs.needsCorner(topFace, leftFace, face)) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, Math.max(leftMarginWidth, 0), 1 - spec.cutDepth,
                        leftMarginWidth + spec.cutWidth, 1, 1 - spec.baseMarginWidth, face));
                stream.append();
            }

            // bottom left corner
            if (fjs.needsCorner(bottomFace, leftFace, face)) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(topFace, 1 - leftMarginWidth - spec.cutWidth,
                        1 - spec.cutDepth, Math.min(1 - leftMarginWidth, 1), 1, 1 - spec.baseMarginWidth, face));
                stream.append();

            }

            // top right corner
            if (fjs.needsCorner(topFace, rightFace, face)) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, 1 - rightMarginWidth - spec.cutWidth,
                        1 - spec.cutDepth, Math.min(1 - rightMarginWidth, 1), 1, 1 - spec.baseMarginWidth, face));
                stream.append();
            }

            // bottom right corner
            if (fjs.needsCorner(bottomFace, rightFace, face)) {
                writer.surface(cutSurface);
                writer.setTextureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(topFace, Math.max(rightMarginWidth, 0), 1 - spec.cutDepth,
                        rightMarginWidth + spec.cutWidth, 1, 1 - spec.baseMarginWidth, face));
                stream.append();
            }
        }

        // inner lamp surface can be a single poly
        {
            writer.surface(SURFACE_LAMP);
            writer.setupFaceQuad(face, Math.max(0, leftMarginWidth), bottomCapHeight, Math.min(1, 1 - rightMarginWidth),
                    1 - topCapHeight, spec.cutDepth, topFace);
            stream.append();
        }
    }

    private void makeCapFace(Direction face, IWritablePolyStream stream, CornerJoinFaceState fjs, FaceSpec spec,
            Direction.Axis axis, XmSurfaceImpl cutSurface) {
        if (fjs == CornerJoinFaceStates.NO_FACE)
            return;

        final IMutablePolygon writer = stream.writer();
        
        /** used to randomize cut textures */
        int salt = 1;

        // lamp bottom can be a single poly
        {
            writer.surface(SURFACE_LAMP);
            writer.setupFaceQuad(face, fjs.isJoined(FaceEdge.LEFT_EDGE) ? 0 : spec.baseMarginWidth,
                    fjs.isJoined(FaceEdge.BOTTOM_EDGE) ? 0 : spec.baseMarginWidth,
                    fjs.isJoined(FaceEdge.RIGHT_EDGE) ? 1 : 1 - spec.baseMarginWidth,
                    fjs.isJoined(FaceEdge.TOP_EDGE) ? 1 : 1 - spec.baseMarginWidth, spec.cutDepth,
                            FaceEdge.TOP_EDGE.toWorld(face));
            stream.append();
        }

        // build quarter slice of cap for each side separately
        // specifications below are oriented with the side at top of cap face

        for (int f = 0; f < FaceEdge.COUNT; f++) {
            final FaceEdge joinSide = FaceEdge.fromOrdinal(f);
            Direction side = joinSide.toWorld(face);

            if (fjs.isJoined(joinSide)) {
                // This side is joined, so connect cuts to other block on this side.

                // margin corner faces
                {
                    stream.setVertexCount(3);
                    writer.surface(SURFACE_MAIN);
                    writer.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(spec.baseMarginWidth, 1, 0), new FaceVertex(0, 1, 0), side);
                    stream.append();
                }
                {
                    stream.setVertexCount(3);
                    writer.surface(SURFACE_MAIN);
                    writer.setupFaceQuad(face, new FaceVertex(1 - spec.baseMarginWidth, 1, 0),
                            new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(1, 1, 0), side);
                    stream.append();
                }

                // margin corner sides
                {
                    writer.surface(cutSurface);
                    writer.setTextureSalt(salt++);
                    setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.rightOf(face, side),
                            1 - spec.baseMarginWidth, 1 - spec.cutDepth, 1, 1, 1 - spec.baseMarginWidth, face));
                    stream.append();
                }
                {
                    writer.surface(cutSurface);
                    writer.setTextureSalt(salt++);
                    setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0, 1 - spec.cutDepth,
                            spec.baseMarginWidth, 1, 1 - spec.baseMarginWidth, face));
                    stream.append();
                }

                // splines
                for (int i = 0; i < spec.cutCount / 2; i++) {
                    float xLeft = spec.baseMarginWidth + (i * 2 + 1) * spec.cutWidth;
                    float xRight = Math.min(xLeft + spec.cutWidth, 0.5f);

                    {
                        writer.surface(SURFACE_MAIN);
                        writer.setupFaceQuad(face, new FaceVertex(xLeft, 1 - xLeft, 0),
                                new FaceVertex(xRight, 1 - xRight, 0), new FaceVertex(xRight, 1, 0),
                                new FaceVertex(xLeft, 1, 0), side);
                        stream.append();
                    }
                    {
                        // mirror on right side, reverse winding order
                        writer.surface(SURFACE_MAIN);
                        writer.setupFaceQuad(face, new FaceVertex(1 - xRight, 1 - xRight, 0),
                                new FaceVertex(1 - xLeft, 1 - xLeft, 0), new FaceVertex(1 - xLeft, 1, 0),
                                new FaceVertex(1 - xRight, 1, 0), side);
                        stream.append();
                    }

                    // cut sides
                    // with odd number of cuts, these overlap in middle, avoid with this check

                    if (xLeft < 0.4999) {
                        {
                            writer.surface(cutSurface);
                            writer.setTextureSalt(salt++);
                            setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0,
                                    1 - spec.cutDepth, xLeft, 1, xLeft, face));
                            stream.append();
                        }
                        {
                            writer.surface(cutSurface);
                            writer.setTextureSalt(salt++);
                            setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.rightOf(face, side), 1 - xLeft,
                                    1 - spec.cutDepth, 1, 1, xLeft, face));
                            stream.append();
                        }
                    }
                    if (xRight < 0.4999) {
                        {
                            writer.surface(cutSurface);
                            writer.setTextureSalt(salt++);
                            setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.rightOf(face, side), 1 - xRight,
                                    1 - spec.cutDepth, 1, 1, 1 - xRight, face));
                            stream.append();
                        }
                        {
                            writer.surface(cutSurface);
                            writer.setTextureSalt(salt++);
                            setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0,
                                    1 - spec.cutDepth, xRight, 1, 1 - xRight, face));
                            stream.append();
                        }
                    }
                }
            } else {
                // This side isn't joined, so don't connect cuts to other block on this side.

                {
                    // outer face
                    writer.surface(SURFACE_MAIN);
                    writer.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(1, 1, 0), new FaceVertex(0, 1, 0), side);
                    stream.append();
                }

                {
                    // outer cut sides
                    for (int i = 0; i < (spec.cutCount + 1) / 2; i++) {
                        float offset = spec.baseMarginWidth + (spec.cutWidth * 2 * i);

                        writer.surface(cutSurface);
                        writer.setTextureSalt(salt++);
                        setupCutSideQuad(writer, new SimpleQuadBounds(side.getOpposite(), offset, 1 - spec.cutDepth,
                                1 - offset, 1, 1 - offset, face));
                        stream.append();

                    }
                }

                for (int i = 0; i < spec.cutCount / 2; i++) {
                    float offset = spec.baseMarginWidth + spec.cutWidth * (2 * i + 1);

                    {
                        // inner cut sides
                        writer.surface(cutSurface);
                        writer.setTextureSalt(salt++);
                        setupCutSideQuad(writer,
                                new SimpleQuadBounds(side, offset, 1 - spec.cutDepth, 1 - offset, 1, offset, face));
                        stream.append();
                    }

                    {
                        // spline / center
                        writer.surface(SURFACE_MAIN);
                        writer.setupFaceQuad(face,
                                new FaceVertex(Math.min(0.5f, offset + spec.cutWidth), 1 - offset - spec.cutWidth, 0),
                                new FaceVertex(Math.max(0.5f, 1 - offset - spec.cutWidth), 1 - offset - spec.cutWidth,
                                        0),
                                new FaceVertex(1 - offset, 1 - offset, 0), new FaceVertex(offset, 1 - offset, 0), side);
                        stream.append();
                    }

                }
            }
        }
    }

    private void setupCutSideQuad(IMutablePolygon qi, SimpleQuadBounds qb) {
        final int glow = qi.surface().isLampGradient() ? 128 : 0;

        qi.setupFaceQuad(qb.face, new FaceVertex.Colored(qb.x0, qb.y0, qb.depth, Color.WHITE, glow),
                new FaceVertex.Colored(qb.x1, qb.y0, qb.depth, Color.WHITE, glow),
                new FaceVertex.Colored(qb.x1, qb.y1, qb.depth, Color.WHITE, 0),
                new FaceVertex.Colored(qb.x0, qb.y1, qb.depth, Color.WHITE, 0), qb.topFace);
    }

    /**
     * If true, cuts in shape are on the block boundary. Reads value from static
     * shape bits in model state
     */
    public static boolean areCutsOnEdge(ModelState modelState) {
        return STATE_ARE_CUTS_ON_EDGE.getValue(modelState.getStaticShapeBits());
    }

    /**
     * If true, cuts in shape are on the block boundary. Saves value in static shape
     * bits in model state
     */
    public static void setCutsOnEdge(boolean areCutsOnEdge, ModelState modelState) {
        modelState.setStaticShapeBits(STATE_ARE_CUTS_ON_EDGE.setValue(areCutsOnEdge, modelState.getStaticShapeBits()));
    }

    /**
     * Number of cuts that appear on each face of model. Reads value from static
     * shape bits in model state
     */
    public static int getCutCount(ModelState modelState) {
        return STATE_CUT_COUNT.getValue(modelState.getStaticShapeBits());
    }

    /**
     * Number of cuts that appear on each face of model. Saves value in static shape
     * bits in model state
     */
    public static void setCutCount(int cutCount, ModelState modelState) {
        modelState.setStaticShapeBits(STATE_CUT_COUNT.setValue(cutCount, modelState.getStaticShapeBits()));
    }

    @Override
    public boolean hasLampSurface(ModelState modelState) {
        return true;
    }

}
