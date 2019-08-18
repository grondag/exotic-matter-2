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

package grondag.xm.api.primitive.simple;

import java.util.function.Consumer;

import grondag.fermion.bits.BitPacker32;
import grondag.fermion.color.Color;
import grondag.xm.Xm;
import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.state.CornerJoinFaceStates;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.mesh.FaceVertex;
import grondag.xm.api.mesh.QuadHelper;
import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.orientation.FaceEdge;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.model.state.AbstractPrimitiveModelState;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.painting.SurfaceTopology;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;

public class SquareColumn extends AbstractSimplePrimitive {
    private static final XmSurfaceList SURFACES_DARK = XmSurfaceList.builder()
            .add("main", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("cut", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("lamp", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

    private static final XmSurfaceList SURFACES_LIT = XmSurfaceList.builder()
            .add("main", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("cut", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP_GRADIENT)
            .add("lamp", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP).build();

    public static final int SURFACE_MAIN = 0;
    public static final int SURFACE_CUT = 1;
    public static final int SURFACE_LAMP = 2;

    public static final int MIN_CUTS = 1;
    public static final int MAX_CUTS = 3;
    

    private static final BitPacker32<SquareColumn> STATE_PACKER = new BitPacker32<SquareColumn>(null, null);
    private static final BitPacker32<SquareColumn>.BooleanElement STATE_ARE_CUTS_ON_EDGE = STATE_PACKER.createBooleanElement();
    private static final BitPacker32<SquareColumn>.IntElement STATE_CUT_COUNT = STATE_PACKER.createIntElement(MIN_CUTS, MAX_CUTS);

    static {
        assert STATE_PACKER.bitLength() <= AbstractPrimitiveModelState.PRIMITIVE_BIT_COUNT;
    }

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
    
    public static final SquareColumn INSTANCE = new SquareColumn(Xm.idString("column_square"));

    @Override
    protected void updateDefaultState(SimpleModelState.Mutable modelState) {
        setCutCount(3, modelState);
        setCutsOnEdge(true, modelState);
    }

    protected SquareColumn(String idString) {
        super(idString, ModelStateFlags.STATE_FLAG_NEEDS_CORNER_JOIN | ModelStateFlags.STATE_FLAG_HAS_AXIS, SimpleModelStateImpl.FACTORY,
                s -> isLit(s) ? SURFACES_LIT : SURFACES_DARK);
    }

    @Override
    public XmSurface lampSurface(SimpleModelState modelState) {
        return isLit(modelState) ? SURFACES_LIT.get(SURFACE_LAMP) : null;
    }

    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        FaceSpec spec = new FaceSpec(getCutCount(modelState), areCutsOnEdge(modelState));
        for (int i = 0; i < 6; i++) {
            this.makeFaceQuads(modelState, Direction.byId(i), spec, target);
        }
    }

    @Override
    public OrientationType orientationType(SimpleModelState modelState) {
        return OrientationType.AXIS;
    }
    
    private static final Axis[] AXIS = Direction.Axis.values();

    private void makeFaceQuads(SimpleModelState state, Direction face, FaceSpec spec, Consumer<Polygon> target) {
        if (face == null)
            return;

        // PERF: if have a consumer and doing this dynamically - should consumer simply
        // be a stream?
        // Why create a stream just to pipe it to the consumer? Or cache the result.

        CornerJoinState bjs = state.cornerJoin();
        Direction.Axis axis = AXIS[state.orientationIndex() % 3];
        WritablePolyStream stream = PolyStreams.claimWritable();

        stream.setVertexCount(4);
        // PERF set cull face for unconnected faces - doesn't work if connected
        stream.writer().lockUV(0, true); //.cullFace(face);
        stream.saveDefaults();

        XmSurfaceList surfaces = surfaces(state);

        if (face.getAxis() == axis) {
            makeCapFace(face, stream, bjs.faceState(face), spec, axis, surfaces);
        } else {
            positiveDirection(face, stream, bjs.faceState(face), spec, axis, surfaces);
        }

        if (stream.origin()) {
            Polygon reader = stream.reader();
            do {
                target.accept(reader);
            } while (stream.next());
        }
        stream.release();
    }

    private void positiveDirection(Direction face, WritablePolyStream stream, CornerJoinFaceState fjs, FaceSpec spec, Direction.Axis axis,
            XmSurfaceList surfaces) {
        if (fjs == CornerJoinFaceStates.NO_FACE)
            return;

        final MutablePolygon writer = stream.writer();

        Direction topFace = QuadHelper.positiveDirection(axis);
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
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, 0, 0, 1, bottomCapHeight, 0, topFace);
            stream.append();
        }

        // top
        if (!hasTopJoin) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, 0, 1 - topCapHeight, 1, 1, 0, topFace);
            stream.append();
        }

        // left margin
        if (leftMarginWidth > 0) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, 0, bottomCapHeight, leftMarginWidth, 1 - topCapHeight, 0, topFace);
            stream.append();
        }

        // right margin
        if (rightMarginWidth > 0) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, 1 - rightMarginWidth, bottomCapHeight, 1, 1 - topCapHeight, 0, topFace);
            stream.append();
        }

        // splines
        for (int i = 0; i < actualCutCount - 1; i++) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth, bottomCapHeight, leftMarginWidth + spec.cutWidth * 2 * (i + 1),
                    1 - topCapHeight, 0, topFace);
            stream.append();
        }

        // top left corner
        if (fjs.needsCorner(topFace, leftFace, face)) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 1 - spec.baseMarginWidth, leftMarginWidth + spec.cutWidth, 1, 0, topFace);
            stream.append();
        }

        // bottom left corner
        if (fjs.needsCorner(bottomFace, leftFace, face)) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 0, leftMarginWidth + spec.cutWidth, spec.baseMarginWidth, 0, topFace);
            stream.append();
        }

        // top right corner
        if (fjs.needsCorner(topFace, rightFace, face)) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 1 - spec.baseMarginWidth, Math.min(1 - rightMarginWidth, 1), 1, 0, topFace);
            stream.append();
        }

        // bottom right corner
        if (fjs.needsCorner(bottomFace, rightFace, face)) {
            writer.surface(surfaces.get(SURFACE_MAIN));
            writer.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 0, Math.min(1 - rightMarginWidth, 1), spec.baseMarginWidth, 0, topFace);
            stream.append();
        }

        for (int i = 0; i < actualCutCount; i++) {
            float sx0 = Math.max(0, leftMarginWidth + spec.cutWidth * 2 * i);
            float sx1 = Math.min(1, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth);

            // left face
            if (sx0 > 0.0001f) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(rightFace, bottomCapHeight, 1 - spec.cutDepth, 1 - topCapHeight, 1, 1 - sx0, face), face);
                stream.append();
            }

            // right face
            if (sx1 < 0.9999) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(leftFace, topCapHeight, 1 - spec.cutDepth, 1 - bottomCapHeight, 1, sx1, face), face);
                stream.append();
            }

            // top face
            if (topCapHeight > 0) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, sx0, 1 - spec.cutDepth, sx1, 1, 1 - topCapHeight, face), face);
                stream.append();
            }

            // bottom face
            if (bottomCapHeight > 0) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(topFace, 1 - sx1, 1 - spec.cutDepth, 1 - sx0, 1, 1 - bottomCapHeight, face), face);
                stream.append();
            }

            // top left corner
            if (fjs.needsCorner(topFace, leftFace, face)) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, Math.max(leftMarginWidth, 0), 1 - spec.cutDepth, leftMarginWidth + spec.cutWidth, 1,
                        1 - spec.baseMarginWidth, face), face);
                stream.append();
            }

            // bottom left corner
            if (fjs.needsCorner(bottomFace, leftFace, face)) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(topFace, 1 - leftMarginWidth - spec.cutWidth, 1 - spec.cutDepth, Math.min(1 - leftMarginWidth, 1),
                        1, 1 - spec.baseMarginWidth, face), face);
                stream.append();

            }

            // top right corner
            if (fjs.needsCorner(topFace, rightFace, face)) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, 1 - rightMarginWidth - spec.cutWidth, 1 - spec.cutDepth,
                        Math.min(1 - rightMarginWidth, 1), 1, 1 - spec.baseMarginWidth, face), face);
                stream.append();
            }

            // bottom right corner
            if (fjs.needsCorner(bottomFace, rightFace, face)) {
                writer.surface(surfaces.get(SURFACE_CUT));
                writer.textureSalt(salt++);
                setupCutSideQuad(writer, new SimpleQuadBounds(topFace, Math.max(rightMarginWidth, 0), 1 - spec.cutDepth, rightMarginWidth + spec.cutWidth, 1,
                        1 - spec.baseMarginWidth, face), face);
                stream.append();
            }
        }

        // inner lamp surface can be a single poly
        {
            writer.surface(surfaces.get(SURFACE_LAMP));
            writer.setupFaceQuad(face, Math.max(0, leftMarginWidth), bottomCapHeight, Math.min(1, 1 - rightMarginWidth), 1 - topCapHeight, spec.cutDepth,
                    topFace);
            stream.append();
        }
    }

    private void makeCapFace(Direction face, WritablePolyStream stream, CornerJoinFaceState fjs, FaceSpec spec, Direction.Axis axis,
            XmSurfaceList surfaces) {
        if (fjs == CornerJoinFaceStates.NO_FACE)
            return;

        final MutablePolygon writer = stream.writer();
        
        /** used to randomize cut textures */
        int salt = 1;

        // lamp bottom can be a single poly
        {
            writer.surface(surfaces.get(SURFACE_LAMP));
            writer.setupFaceQuad(face, fjs.isJoined(FaceEdge.LEFT_EDGE) ? 0 : spec.baseMarginWidth,
                    fjs.isJoined(FaceEdge.BOTTOM_EDGE) ? 0 : spec.baseMarginWidth, fjs.isJoined(FaceEdge.RIGHT_EDGE) ? 1 : 1 - spec.baseMarginWidth,
                    fjs.isJoined(FaceEdge.TOP_EDGE) ? 1 : 1 - spec.baseMarginWidth, spec.cutDepth, FaceEdge.TOP_EDGE.toWorld(face));
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
                    writer.surface(surfaces.get(SURFACE_MAIN));
                    writer.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0), new FaceVertex(spec.baseMarginWidth, 1, 0),
                            new FaceVertex(0, 1, 0), side);
                    stream.append();
                }
                {
                    stream.setVertexCount(3);
                    writer.surface(surfaces.get(SURFACE_MAIN));
                    writer.setupFaceQuad(face, new FaceVertex(1 - spec.baseMarginWidth, 1, 0),
                            new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0), new FaceVertex(1, 1, 0), side);
                    stream.append();
                }

                // margin corner sides
                {
                    writer.surface(surfaces.get(SURFACE_CUT));
                    writer.textureSalt(salt++);
                    setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.rightOf(face, side), 1 - spec.baseMarginWidth, 1 - spec.cutDepth, 1, 1,
                            1 - spec.baseMarginWidth, face), face);
                    stream.append();
                }
                {
                    writer.surface(surfaces.get(SURFACE_CUT));
                    writer.textureSalt(salt++);
                    setupCutSideQuad(writer,
                            new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0, 1 - spec.cutDepth, spec.baseMarginWidth, 1, 1 - spec.baseMarginWidth, face), face);
                    stream.append();
                }

                // splines
                for (int i = 0; i < spec.cutCount / 2; i++) {
                    float xLeft = spec.baseMarginWidth + (i * 2 + 1) * spec.cutWidth;
                    float xRight = Math.min(xLeft + spec.cutWidth, 0.5f);

                    {
                        writer.surface(surfaces.get(SURFACE_MAIN));
                        writer.setupFaceQuad(face, new FaceVertex(xLeft, 1 - xLeft, 0), new FaceVertex(xRight, 1 - xRight, 0), new FaceVertex(xRight, 1, 0),
                                new FaceVertex(xLeft, 1, 0), side);
                        stream.append();
                    }
                    {
                        // mirror on right side, reverse winding order
                        writer.surface(surfaces.get(SURFACE_MAIN));
                        writer.setupFaceQuad(face, new FaceVertex(1 - xRight, 1 - xRight, 0), new FaceVertex(1 - xLeft, 1 - xLeft, 0),
                                new FaceVertex(1 - xLeft, 1, 0), new FaceVertex(1 - xRight, 1, 0), side);
                        stream.append();
                    }

                    // cut sides
                    // with odd number of cuts, these overlap in middle, avoid with this check

                    if (xLeft < 0.4999) {
                        {
                            writer.surface(surfaces.get(SURFACE_CUT));
                            writer.textureSalt(salt++);
                            setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0, 1 - spec.cutDepth, xLeft, 1, xLeft, face), face);
                            stream.append();
                        }
                        {
                            writer.surface(surfaces.get(SURFACE_CUT));
                            writer.textureSalt(salt++);
                            setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.rightOf(face, side), 1 - xLeft, 1 - spec.cutDepth, 1, 1, xLeft, face), face);
                            stream.append();
                        }
                    }
                    if (xRight < 0.4999) {
                        {
                            writer.surface(surfaces.get(SURFACE_CUT));
                            writer.textureSalt(salt++);
                            setupCutSideQuad(writer,
                                    new SimpleQuadBounds(QuadHelper.rightOf(face, side), 1 - xRight, 1 - spec.cutDepth, 1, 1, 1 - xRight, face), face);
                            stream.append();
                        }
                        {
                            writer.surface(surfaces.get(SURFACE_CUT));
                            writer.textureSalt(salt++);
                            setupCutSideQuad(writer, new SimpleQuadBounds(QuadHelper.leftOf(face, side), 0, 1 - spec.cutDepth, xRight, 1, 1 - xRight, face), face);
                            stream.append();
                        }
                    }
                }
            } else {
                // This side isn't joined, so don't connect cuts to other block on this side.

                {
                    // outer face
                    writer.surface(surfaces.get(SURFACE_MAIN));
                    writer.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
                            new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0), new FaceVertex(1, 1, 0), new FaceVertex(0, 1, 0), side);
                    stream.append();
                }

                {
                    // outer cut sides
                    for (int i = 0; i < (spec.cutCount + 1) / 2; i++) {
                        float offset = spec.baseMarginWidth + (spec.cutWidth * 2 * i);

                        writer.surface(surfaces.get(SURFACE_CUT));
                        writer.textureSalt(salt++);
                        setupCutSideQuad(writer, new SimpleQuadBounds(side.getOpposite(), offset, 1 - spec.cutDepth, 1 - offset, 1, 1 - offset, face), face);
                        stream.append();

                    }
                }

                for (int i = 0; i < spec.cutCount / 2; i++) {
                    float offset = spec.baseMarginWidth + spec.cutWidth * (2 * i + 1);

                    {
                        // inner cut sides
                        writer.surface(surfaces.get(SURFACE_CUT));
                        writer.textureSalt(salt++);
                        setupCutSideQuad(writer, new SimpleQuadBounds(side, offset, 1 - spec.cutDepth, 1 - offset, 1, offset, face), face);
                        stream.append();
                    }

                    {
                        // spline / center
                        writer.surface(surfaces.get(SURFACE_MAIN));
                        writer.setupFaceQuad(face, new FaceVertex(Math.min(0.5f, offset + spec.cutWidth), 1 - offset - spec.cutWidth, 0),
                                new FaceVertex(Math.max(0.5f, 1 - offset - spec.cutWidth), 1 - offset - spec.cutWidth, 0),
                                new FaceVertex(1 - offset, 1 - offset, 0), new FaceVertex(offset, 1 - offset, 0), side);
                        stream.append();
                    }

                }
            }
        }
    }

    private void setupCutSideQuad(MutablePolygon qi, SimpleQuadBounds qb, Direction face) {
        final int glow = qi.surface().isLampGradient() ? 255 : 0;
        
        qi.setupFaceQuad(qb.face, 
                new FaceVertex.Colored(qb.x0, qb.y0, qb.depth, Color.WHITE, glow),
                new FaceVertex.Colored(qb.x1, qb.y0, qb.depth, Color.WHITE, glow),
                new FaceVertex.Colored(qb.x1, qb.y1, qb.depth, Color.WHITE, glow / 3),
                new FaceVertex.Colored(qb.x0, qb.y1, qb.depth, Color.WHITE, glow / 3),
                qb.topFace);
        
        // force vertex normals out to prevent lighting anomalies
        final Vec3i vec = face.getVector();
        final float x = vec.getX();
        final float y = vec.getY();
        final float z = vec.getZ();
        for(int i = 0; i < 4; i++) {
            qi.normal(i, x, y, z);
        }
    }

    /**
     * If true, cuts in shape are on the block boundary. Reads value from static
     * shape bits in model state
     */
    public static boolean areCutsOnEdge(SimpleModelState modelState) {
        return STATE_ARE_CUTS_ON_EDGE.getValue(modelState.primitiveBits());
    }

    /**
     * If true, cuts in shape are on the block boundary. Saves value in static shape
     * bits in model state
     */
    public static void setCutsOnEdge(boolean areCutsOnEdge, SimpleModelState.Mutable modelState) {
        modelState.primitiveBits(STATE_ARE_CUTS_ON_EDGE.setValue(areCutsOnEdge, modelState.primitiveBits()));
    }

    /**
     * Number of cuts that appear on each face of model. Reads value from static
     * shape bits in model state
     */
    public static int getCutCount(SimpleModelState modelState) {
        return STATE_CUT_COUNT.getValue(modelState.primitiveBits());
    }

    /**
     * Number of cuts that appear on each face of model. Saves value in static shape
     * bits in model state
     */
    public static void setCutCount(int cutCount, SimpleModelState.Mutable modelState) {
        modelState.primitiveBits(STATE_CUT_COUNT.setValue(cutCount, modelState.primitiveBits()));
    }

    public static boolean isLit(SimpleModelState modelState) {
        return modelState.paint(SURFACE_LAMP).emissive(0);
    }

    @Override
    public SimpleModelState.Mutable geometricState(SimpleModelState fromState) {
        final SimpleModelState.Mutable result = this.newState();
        return result;
    }

    @Override
    public boolean doesShapeMatch(SimpleModelState from, SimpleModelState to) {
        
        return from.primitive() == to.primitive() 
                && from.orientationIndex() == to.orientationIndex()
                && from.cornerJoin() == to.cornerJoin()
                && from.primitiveBits() == to.primitiveBits();
    }
    
    /** a relic from simpler times */
    @Deprecated
    private static class SimpleQuadBounds {
        public Direction face;
        public float x0;
        public float y0;
        public float x1;
        public float y1;
        public float depth;
        public Direction topFace;

        public SimpleQuadBounds(Direction face, float x0, float y0, float x1, float y1, float depth, Direction topFace) {
            this.face = face;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.depth = depth;
            this.topFace = topFace;
        }
    }
}