/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm.terrain;

import grondag.xm.api.modelstate.PrimitiveModelState.ModelStateFactory;
import grondag.xm.api.terrain.TerrainModelState;
import grondag.xm.api.terrain.TerrainModelState.Mutable;

//TODO: restore
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Consumer;
//
//
//
//import com.google.common.collect.ImmutableList;
//
//import grondag.brocade.BrocadeConfig;
//import grondag.brocade.Brocade;
//import grondag.fermion.cache.LongSimpleCacheLoader;
//import grondag.fermion.cache.LongSimpleLoadingCache;
//import grondag.brocade.collision.CollisionBoxDispatcher;
//import grondag.brocade.legacy.block.ISuperBlock;
//import grondag.brocade.mesh.ShapeMeshGenerator;
//import grondag.brocade.painting.PaintLayer;
//import grondag.brocade.painting.Surface;
//import grondag.brocade.painting.SurfaceTopology;
//import grondag.brocade.primitives.FaceVertex;
//import grondag.brocade.primitives.polygon.IMutablePolygon;
//import grondag.brocade.primitives.polygon.IPolygon;
//import grondag.brocade.primitives.vertex.Vec3f;
//import grondag.brocade.world.HorizontalCorner;
//import grondag.brocade.world.HorizontalFace;
//import grondag.brocade.model.state.ISuperModelState;
//import grondag.brocade.model.state.ModelStateData;
//import grondag.brocade.model.state.StateFormat;
//import grondag.brocade.model.varia.SideShape;
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.math.Direction;
//import net.minecraft.util.math.BoundingBox;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//
///**
// * Mesh generator for flowing terrain. Currently used for lava and basalt. Makes
// * no effort to set useful UV values because all quads are expected to be UV
// * locked.
// */
public abstract class TerrainSurface extends AbstractTerrainPrimitive {
    
    protected TerrainSurface(String idString, int stateFlags, ModelStateFactory<TerrainModelState, Mutable> factory) {
        super(idString, stateFlags, factory);
        // TODO Auto-generated constructor stub
    }
    
    public static final TerrainSurface FILLER = null; // ModelShapes.create("terrain_filler",
    public static final TerrainSurface HEIGHT = null; // ModelShapes.create("terrain_height",
//    private static final Surface SURFACE_TOP = Surface.builder(SurfaceTopology.CUBIC)
//            .withIgnoreDepthForRandomization(true)
//            .withEnabledLayers(PaintLayer.BASE, PaintLayer.LAMP, PaintLayer.MIDDLE).build();
//
//    private static final Surface SURFACE_SIDE = Surface.builder(SurfaceTopology.CUBIC)
//            .withEnabledLayers(PaintLayer.CUT, PaintLayer.LAMP, PaintLayer.OUTER).withAllowBorders(false).build();
//
//    private static final BoundingBox[] COLLISION_BOUNDS = { new BoundingBox(0, 0, 0, 1, 1, 1),
//            new BoundingBox(0, 0, 0, 1, 11F / 12F, 1), new BoundingBox(0, 0, 0, 1, 10F / 12F, 1),
//            new BoundingBox(0, 0, 0, 1, 9F / 12F, 1),
//
//            new BoundingBox(0, 0, 0, 1, 8F / 12F, 1), new BoundingBox(0, 0, 0, 1, 7F / 12F, 1),
//            new BoundingBox(0, 0, 0, 1, 6F / 12F, 1), new BoundingBox(0, 0, 0, 1, 5F / 12F, 1),
//
//            new BoundingBox(0, 0, 0, 1, 4F / 12F, 1), new BoundingBox(0, 0, 0, 1, 3F / 12F, 1),
//            new BoundingBox(0, 0, 0, 1, 2F / 12F, 1), new BoundingBox(0, 0, 0, 1, 1F / 12F, 1),
//
//            // These aren't actually valid meta values, but prevent NPE if we get one
//            // somehow
//            new BoundingBox(0, 0, 0, 1, 1, 1), new BoundingBox(0, 0, 0, 1, 1, 1),
//            new BoundingBox(0, 0, 0, 1, 1, 1), new BoundingBox(0, 0, 0, 1, 1, 1) };
//
//    private static final IMutablePolygon template;
//
//    private static final CSGNode.Root[] terrainNodesSimple = new CSGNode.Root[5];
//    private static final CSGNode.Root[] terrainNodesHybrid = new CSGNode.Root[5];
//    // private final CSGNode.Root[] terrainNodesComplex = new CSGNode.Root[5];
//
//    private static final CSGNode.Root cubeNodeSimple;
//    private static final CSGNode.Root cubeNodeHybrid;
//    // private final CSGNode.Root cubeNodeComplex;
//
//    private static final LongSimpleLoadingCache<List<IPolygon>> modelCache = new LongSimpleLoadingCache<List<IPolygon>>(
//            new TerrainCacheLoader(), 0xFFFF);
//
//    private static class TerrainCacheLoader implements LongSimpleCacheLoader<List<IPolygon>> {
//        @Override
//        public List<IPolygon> load(long key) {
//            // cacheMisses.incrementAndGet();
//            return createShapeQuads(new TerrainState(key < 0 ? -key : key, 0), key < 0);
//        }
//    }
//
//    static {
//        template = PolyFactory.COMMON_POOL.newPaintable(4);
//
//        template.setLockUV(0, true);
//        template.setSurface(SURFACE_TOP);
//        // default - need to change for sides and bottom
//        template.setNominalFace(Direction.UP);
//        // templateBuilder.setTag("template");
//
//        final IMutablePolygon qBottom = template.claimCopy(4);
//        for (int i = 0; i < 5; i++) {
//            // Bottom faces are pre-built
//
//            qBottom.setSurface(SURFACE_SIDE);
//            qBottom.setNominalFace(Direction.DOWN);
//            qBottom.setupFaceQuad(0, 0, 1, 1, getBottomY(i - 2), Direction.NORTH);
//            // qBottom.setTag("bottom-" + i);
//
//            // qBottom = Poly.claimCopyOf(qBottom).setTag("bottom-simple-" + i);
//
//            terrainNodesSimple[i] = CSGNode.create(ImmutableList.of(qBottom.toPainted()));
//
//            // qBottom = Poly.claimCopyOf(qBottom).setTag("bottom-hybrid-" + i);
//
//            terrainNodesHybrid[i] = CSGNode.create(ImmutableList.of(qBottom.toPainted()), false);
//
//            // terrainNodesComplex[i] = CSGNode.create(ImmutableList.of(qBottom), true);
//
//        }
//        qBottom.release();
//
//        List<IPolygon> cubeQuads = cubeQuads();
//
//        // cubeQuads.forEach(q -> q.setTag("cube-simple-" +
//        // q.getNominalFace().toString()));
//        cubeNodeSimple = CSGNode.create(cubeQuads);
//
//        cubeQuads = cubeQuads();
//        // cubeQuads.forEach(q -> q.setTag("cube-hybrid" +
//        // q.getNominalFace().toString()));
//        cubeNodeHybrid = CSGNode.create(cubeQuads, false);
//
//        // this.cubeNodeComplex = CSGNode.create(cubeQuads, true);
//    }
//
//    // private final AtomicInteger cacheAttempts = new AtomicInteger(0);
//    // private final AtomicInteger cacheMisses = new AtomicInteger(0);
//    //
//    // public void reportCacheHits()
//    // {
//    // final int attempts = cacheAttempts.get();
//    // final int hits = attempts - cacheMisses.get();
//    // System.out.println(String.format("Terrain cache hits: %d / %d (%f percent)",
//    // hits, attempts, 100f * hits / attempts ));
//    // System.out.println(String.format("Cache capacity = %d, maxfill = %d",
//    // modelCache.capacity, modelCache.maxFill));
//    // cacheMisses.set(0);
//    // cacheAttempts.set(0);
//    // }
//
//    private static int getIndexForState(TerrainState state) {
//        return state.getYOffset() + 2;
//    }
//
//    /**
//     * Used for Y coord of bottom face and as lower Y coord of side faces Expects
//     * values of -2 through +2 from {@link TerrainState#getYOffset()}
//     */
//    private static int getBottomY(int yOffset) {
//        return -2 - yOffset;
//    }
//
//    // private static LongOpenHashSet hitMap = new LongOpenHashSet();
//    // private static AtomicInteger tryCount = new AtomicInteger();
//    // private static AtomicInteger hitCount = new AtomicInteger();
//    //
//    // public static void reportAndClearHitCount()
//    // {
//    // synchronized(hitMap)
//    // {
//    // Brocade.INSTANCE.info("Terrain geometry potential cache hit rate = %d
//    // percent", (hitCount.get() * 100) / tryCount.get());
//    // Brocade.INSTANCE.info("Terrain geometry max cached states = %d",
//    // hitMap.size());
//    // tryCount.set(0);
//    // hitCount.set(0);
//    // hitMap.clear();
//    // }
//    // }
//
//    private static class WorkVars {
//        /**
//         * Quads on left (west) side of the top face.<br>
//         * Needed for model and to computer center normal.
//         */
//        final IMutablePolygon quadInputsCenterLeft[] = new IMutablePolygon[4];
//
//        /**
//         * Quads on right (east) side of the top face.<br>
//         * Needed for model and to compute center normal.
//         */
//        final IMutablePolygon quadInputsCenterRight[] = new IMutablePolygon[4];
//
//        /**
//         * Quads adjacent to each side midpoint vertex. Needed to compute normals. Will
//         * always contains quads for this block but only contains quads for adjacent
//         * space if it has a terrain height.
//         */
//        @SuppressWarnings("unchecked")
//        final ObjectArrayList<IMutablePolygon>[] quadInputsSide = new ObjectArrayList[4];
//
//        /**
//         * Quads adjacent to each corner vertex. Needed to compute normals. Will always
//         * contains quads for this block but only contains quads for adjacent spaces if
//         * the space has a terrain height.
//         */
//        @SuppressWarnings("unchecked")
//        final ObjectArrayList<IMutablePolygon>[] quadInputsCorner = new ObjectArrayList[4];
//
//        final Vec3f.Mutable vecOut = new Vec3f.Mutable(0, 0, 0);
//        final Vec3f.Mutable vecIn = new Vec3f.Mutable(0, 0, 0);
//
//        /**
//         * Top face vertex positions for corners of this block. Could be above or below
//         * the box bounding box. CSG operations will trim shape to block box later.
//         * Initialized to a height of one and changed based on world state.
//         */
//        final FaceVertex fvMidCorner[] = new FaceVertex[HorizontalFace.COUNT];
//
//        /**
//         * Top face vertex positions for centers of the block at the four corners.
//         * Initialized to a height of one and changed based on world state. Used to
//         * generate tris needed to compute vertex normals
//         */
//        final FaceVertex fvFarCorner[] = new FaceVertex[HorizontalFace.COUNT];
//
//        // Coordinates assume quad will be set up with North=top orientation
//        // Depth will be set separately.
//        final FaceVertex fvMidSide[] = new FaceVertex[HorizontalFace.COUNT];
//
//        final FaceVertex fvFarSide[] = new FaceVertex[HorizontalFace.COUNT];
//
//        final Vec3f normCorner[] = new Vec3f[4];
//        final Vec3f normSide[] = new Vec3f[4];
//
//        private WorkVars() {
//            for (int i = 0; i < 4; i++) {
//                quadInputsSide[i] = new ObjectArrayList<IMutablePolygon>();
//                quadInputsCorner[i] = new ObjectArrayList<IMutablePolygon>();
//            }
//        }
//
//        WorkVars prepare() {
//            for (int i = 0; i < 4; i++) {
//                quadInputsSide[i].clear();
//                quadInputsCorner[i].clear();
//            }
//
//            fvMidCorner[HorizontalCorner.NORTH_EAST.ordinal()] = new FaceVertex(1, 1, 1);
//            fvMidCorner[HorizontalCorner.NORTH_WEST.ordinal()] = new FaceVertex(0, 1, 1);
//            fvMidCorner[HorizontalCorner.SOUTH_EAST.ordinal()] = new FaceVertex(1, 0, 1);
//            fvMidCorner[HorizontalCorner.SOUTH_WEST.ordinal()] = new FaceVertex(0, 0, 1);
//
//            fvFarCorner[HorizontalCorner.NORTH_EAST.ordinal()] = new FaceVertex(1.5f, 1.5f, 1);
//            fvFarCorner[HorizontalCorner.NORTH_WEST.ordinal()] = new FaceVertex(-0.5f, 1.5f, 1);
//            fvFarCorner[HorizontalCorner.SOUTH_EAST.ordinal()] = new FaceVertex(1.5f, -0.5f, 1);
//            fvFarCorner[HorizontalCorner.SOUTH_WEST.ordinal()] = new FaceVertex(-0.5f, -0.5f, 1);
//
//            fvMidSide[HorizontalFace.NORTH.ordinal()] = new FaceVertex(0.5f, 1f, 1.0f);
//            fvMidSide[HorizontalFace.SOUTH.ordinal()] = new FaceVertex(0.5f, 0f, 1.0f);
//            fvMidSide[HorizontalFace.EAST.ordinal()] = new FaceVertex(1.0f, 0.5f, 1.0f);
//            fvMidSide[HorizontalFace.WEST.ordinal()] = new FaceVertex(0f, 0.5f, 1.0f);
//
//            fvFarSide[HorizontalFace.NORTH.ordinal()] = new FaceVertex(0.5f, 1.5f, 1.0f);
//            fvFarSide[HorizontalFace.SOUTH.ordinal()] = new FaceVertex(0.5f, -0.5f, 1.0f);
//            fvFarSide[HorizontalFace.EAST.ordinal()] = new FaceVertex(1.5f, 0.5f, 1.0f);
//            fvFarSide[HorizontalFace.WEST.ordinal()] = new FaceVertex(-0.5f, 0.5f, 1.0f);
//            return this;
//        }
//    }
//
//    private static final ThreadLocal<WorkVars> workVars = new ThreadLocal<WorkVars>() {
//        @Override
//        protected WorkVars initialValue() {
//            return new WorkVars();
//        }
//    };
//
//    private static void addTerrainQuads(TerrainState flowState, CSGNode.Root terrainQuads, boolean needsSubdivision) {
//        // tryCount.incrementAndGet();
//        // synchronized(hitMap)
//        // {
//        // if(!hitMap.add(flowState.getStateKey()))
//        // {
//        // hitCount.incrementAndGet();
//        // }
//        // }
//
//        final WorkVars w = workVars.get().prepare();
//        final IMutablePolygon quadInputsCenterLeft[] = w.quadInputsCenterLeft;
//        final IMutablePolygon quadInputsCenterRight[] = w.quadInputsCenterRight;
//        final ObjectArrayList<IMutablePolygon>[] quadInputsSide = w.quadInputsSide;
//        final ObjectArrayList<IMutablePolygon>[] quadInputsCorner = w.quadInputsCorner;
//        final Vec3f.Mutable vecOut = w.vecOut;
//        final Vec3f.Mutable vecIn = w.vecIn;
//        final FaceVertex fvMidCorner[] = w.fvMidCorner;
//        final FaceVertex fvFarCorner[] = w.fvFarCorner;
//        final FaceVertex fvMidSide[] = w.fvMidSide;
//        final FaceVertex fvFarSide[] = w.fvFarSide;
//        final Vec3f normCorner[] = w.normCorner;
//        final Vec3f normSide[] = w.normSide;
//
//        // center vertex setup
//        FaceVertex fvCenter = new FaceVertex(0.5f, 0.5f,
//                1.0f - flowState.getCenterVertexHeight() + flowState.getYOffset());
//
//        ///////////////////////////////////////////////
//        // set up corner heights and face vertices
//        ///////////////////////////////////////////////
//
//        // Coordinates assume quad will be set up with North=top orientation
//        // Depth will be set separately.
//
//        for (int i = 0; i < HorizontalCorner.COUNT; i++) {
//            HorizontalCorner corner = HorizontalCorner.VALUES[i];
//            fvMidCorner[i] = fvMidCorner[i]
//                    .withDepth(1 - flowState.getMidCornerVertexHeight(corner) + flowState.getYOffset());
//            fvFarCorner[i] = fvFarCorner[i]
//                    .withDepth(1 - flowState.getFarCornerVertexHeight(corner) + flowState.getYOffset());
//        }
//
//        for (int i = 0; i < HorizontalFace.COUNT; i++) {
//            final HorizontalFace side = HorizontalFace.VALUES[i];
//
//            fvMidSide[i] = fvMidSide[i].withDepth(1 - flowState.getMidSideVertexHeight(side) + flowState.getYOffset());
//            fvFarSide[i] = fvFarSide[i].withDepth(1 - flowState.getFarSideVertexHeight(side) + flowState.getYOffset());
//
//            // build quads on the top of this block that that border this side (left and
//            // right)
//            // these are always included in the vertex normal calculations for the side
//            // midpoint and corner vertices
//
//            IMutablePolygon qiWork = template.claimCopy(3);
//            qiWork.setupFaceQuad(fvMidSide[i], fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
//                    fvCenter, Direction.NORTH);
//            quadInputsCenterLeft[i] = qiWork;
//            quadInputsSide[i].add(qiWork);
//            quadInputsCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()].add(qiWork);
//
//            qiWork = template.claimCopy(3);
//            qiWork.setupFaceQuad(fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()], fvMidSide[i],
//                    fvCenter, Direction.NORTH);
//            quadInputsCenterRight[i] = qiWork;
//            quadInputsSide[i].add(qiWork);
//            quadInputsCorner[HorizontalCorner.find(side, side.getRight()).ordinal()].add(qiWork);
//
//            final boolean isSidePresent = flowState.height(side) != TerrainState.NO_BLOCK;
//
//            // add side block tri that borders this block if it is there
//            if (isSidePresent) {
//                qiWork = template.claimCopy(3);
//                qiWork.setupFaceQuad(fvFarSide[i], fvMidCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()],
//                        fvMidSide[i], Direction.NORTH);
//                quadInputsSide[i].add(qiWork);
//                quadInputsCorner[HorizontalCorner.find(side, side.getLeft()).ordinal()].add(qiWork);
//
//                qiWork = template.claimCopy(3);
//                qiWork.setupFaceQuad(fvMidCorner[HorizontalCorner.find(side, side.getRight()).ordinal()], fvFarSide[i],
//                        fvMidSide[i], Direction.NORTH);
//                quadInputsSide[i].add(qiWork);
//                quadInputsCorner[HorizontalCorner.find(side, side.getRight()).ordinal()].add(qiWork);
//            }
//
//            // add side block tris that connect to corner but do not border side
//            // if both the side and corner block are present, this will be a tri that
//            // spans both the side and corner block (and will affect normals proportional to
//            // area)
//
//            // if only the side is present, will be the half on the side block and if
//            // the side block is missing but the corner block is present will be the part
//            // that is on the corner block.
//
//            // in the cases where either the side or corner block is missing, terrain state
//            // will compute the height of midpoint between them to be the 1/2 block less
//            // than
//            // the height of center of the block that is present (see
//            // TerrainState.calcMidSideVertexHeight)
//
//            final HorizontalCorner leftCorner = HorizontalCorner.find(side, side.getLeft());
//            final boolean isLeftCornerPresent = flowState.height(leftCorner) != TerrainState.NO_BLOCK;
//
//            final HorizontalCorner rightCorner = HorizontalCorner.find(side, side.getRight());
//            final boolean isRightCornerPresent = flowState.height(rightCorner) != TerrainState.NO_BLOCK;
//
//            if (isSidePresent) {
//                qiWork = template.claimCopy(3);
//
//                final FaceVertex leftFarCorner = isLeftCornerPresent
//
//                        // have both the corner and side so do one big tri for both
//                        ? fvFarCorner[leftCorner.ordinal()]
//
//                        // only have tri on side block, vertex for side of missing corner will
//                        // be half a block lower than the side's center height
//                        : midPoint(fvFarSide[i], fvFarCorner[leftCorner.ordinal()])
//                                .withDepth(fvFarSide[i].depth + 0.5f);
//
//                qiWork.setupFaceQuad(fvMidCorner[leftCorner.ordinal()], fvFarSide[i], leftFarCorner, Direction.NORTH);
//                quadInputsCorner[leftCorner.ordinal()].add(qiWork);
//
//                qiWork = template.claimCopy(3);
//
//                final FaceVertex rightFarCorner = isRightCornerPresent
//
//                        // have both the corner and side so do one big tri for both
//                        ? fvFarCorner[rightCorner.ordinal()]
//
//                        // only have tri on side block, vertex for side of missing corner will
//                        // be half a block lower than the side's center height
//                        : midPoint(fvFarSide[i], fvFarCorner[rightCorner.ordinal()])
//                                .withDepth(fvFarSide[i].depth + 0.5f);
//
//                qiWork.setupFaceQuad(fvMidCorner[rightCorner.ordinal()], rightFarCorner, fvFarSide[i],
//                        Direction.NORTH);
//                quadInputsCorner[rightCorner.ordinal()].add(qiWork);
//            } else {
//                if (isLeftCornerPresent) {
//                    // only have the corner
//                    qiWork = template.claimCopy(3);
//                    qiWork.setupFaceQuad(fvMidCorner[leftCorner.ordinal()],
//                            midPoint(fvFarSide[i], fvFarCorner[leftCorner.ordinal()])
//                                    .withDepth(fvFarCorner[leftCorner.ordinal()].depth + 0.5f),
//                            fvFarCorner[leftCorner.ordinal()], Direction.NORTH);
//                    quadInputsCorner[leftCorner.ordinal()].add(qiWork);
//                }
//
//                if (isRightCornerPresent) {
//                    // only have the corner
//                    qiWork = template.claimCopy(3);
//                    qiWork.setupFaceQuad(fvMidCorner[rightCorner.ordinal()], fvFarCorner[rightCorner.ordinal()],
//                            midPoint(fvFarSide[i], fvFarCorner[rightCorner.ordinal()])
//                                    .withDepth(fvFarCorner[rightCorner.ordinal()].depth + 0.5f),
//                            Direction.NORTH);
//                    quadInputsCorner[rightCorner.ordinal()].add(qiWork);
//                }
//            }
//        }
//
//        int bottom = getBottomY(flowState.getYOffset());
//
//        vecOut.load(quadInputsCenterLeft[0].getFaceNormal()).scale(quadInputsCenterLeft[0].getArea());
//        vecOut.add(vecIn.load(quadInputsCenterLeft[1].getFaceNormal()).scale(quadInputsCenterLeft[1].getArea()));
//        vecOut.add(vecIn.load(quadInputsCenterLeft[2].getFaceNormal()).scale(quadInputsCenterLeft[2].getArea()));
//        vecOut.add(vecIn.load(quadInputsCenterLeft[3].getFaceNormal()).scale(quadInputsCenterLeft[3].getArea()));
//        vecOut.add(vecIn.load(quadInputsCenterRight[0].getFaceNormal()).scale(quadInputsCenterRight[0].getArea()));
//        vecOut.add(vecIn.load(quadInputsCenterRight[1].getFaceNormal()).scale(quadInputsCenterRight[1].getArea()));
//        vecOut.add(vecIn.load(quadInputsCenterRight[2].getFaceNormal()).scale(quadInputsCenterRight[2].getArea()));
//        vecOut.add(vecIn.load(quadInputsCenterRight[3].getFaceNormal()).scale(quadInputsCenterRight[3].getArea()));
//        shadowEnhance(vecOut);
//        final Vec3f normCenter = vecOut.toImmutable();
//
//        // compute weighted normals for side and corner vertices
//        // four of each, so reusing the same 0-3 loop for both
//        for (int i = 0; i < 4; i++) {
//            vecOut.load(0, 0, 0);
//            ObjectArrayList<IMutablePolygon> list = quadInputsSide[i];
//            int limit = list.size();
//            for (int j = 0; j < limit; j++) {
//                IMutablePolygon qi = list.get(j);
//                vecOut.add(vecIn.load(qi.getFaceNormal()).scale(qi.getArea()));
//            }
//            shadowEnhance(vecOut);
//            normSide[i] = vecOut.toImmutable();
//
//            vecOut.load(0, 0, 0);
//            list = quadInputsCorner[i];
//            limit = list.size();
//            for (int j = 0; j < limit; j++) {
//                IMutablePolygon qi = list.get(j);
//                vecOut.add(vecIn.load(qi.getFaceNormal()).scale(qi.getArea()));
//            }
//            shadowEnhance(vecOut);
//            normCorner[i] = vecOut.toImmutable();
//        }
//
//        final boolean isTopSimple = BrocadeConfig.BLOCKS.simplifyTerrainBlockGeometry && flowState.isTopSimple()
//                && !needsSubdivision;
//
//        // note that outputting sides first seems to work best for CSG intersect
//        // performance
//        // with convex polyhedra tend to get an unbalanced BSP tree - not much can do
//        // about it without creating unwanted splits
//        for (int i = 0; i < HorizontalFace.COUNT; i++) {
//            final HorizontalFace side = HorizontalFace.VALUES[i];
//
//            // don't use middle vertex if it is close to being in line with corners
//            if (BrocadeConfig.BLOCKS.simplifyTerrainBlockGeometry && flowState.isSideSimple(side) && !needsSubdivision) {
//
//                // side
//                IMutablePolygon qSide = template.claimCopy();
//                // qSide.setTag("side-simple-" + side.toString());
//
//                final HorizontalCorner cornerLeft = HorizontalCorner.find(side, side.getLeft());
//                final HorizontalCorner cornerRight = HorizontalCorner.find(side, side.getRight());
//
//                qSide.setSurface(SURFACE_SIDE);
//                qSide.setNominalFace(side.face);
//
//                qSide.setupFaceQuad(new FaceVertex(0, bottom, 0), new FaceVertex(1, bottom, 0),
//                        new FaceVertex(1, flowState.getMidCornerVertexHeight(cornerLeft) - flowState.getYOffset(), 0),
//                        new FaceVertex(0, flowState.getMidCornerVertexHeight(cornerRight) - flowState.getYOffset(), 0),
//                        Direction.UP);
//                terrainQuads.addPolygon(qSide);
//
//                // if side is simple top *may* be not necessarily so - build top if not simple
//                if (!isTopSimple) {
//                    IMutablePolygon qi = template.claimCopy(3);
//
//                    // qi.setTag("top-simpleside-" + side.toString());
//
//                    qi.setupFaceQuad(fvMidCorner[cornerLeft.ordinal()], fvCenter, fvMidCorner[cornerRight.ordinal()],
//                            Direction.NORTH);
//                    qi.setVertexNormal(0, normCorner[cornerLeft.ordinal()]);
//                    qi.setVertexNormal(1, normCenter);
//                    qi.setVertexNormal(2, normCorner[cornerRight.ordinal()]);
//                    terrainQuads.addPolygon(qi);
//                }
//
//            } else // side and top not simple
//            {
//
//                // Sides
//                IMutablePolygon qSide = template.claimCopy();
//                // qSide.setTag("side-complex-1-" + side.toString());
//                qSide.setSurface(SURFACE_SIDE);
//                qSide.setNominalFace(side.face);
//
//                qSide.setupFaceQuad(new FaceVertex(0, bottom, 0), new FaceVertex(0.5f, bottom, 0),
//                        new FaceVertex(0.5f, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
//                        new FaceVertex(0,
//                                flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getRight()))
//                                        - flowState.getYOffset(),
//                                0),
//                        Direction.UP);
//                terrainQuads.addPolygon(qSide);
//
//                qSide = qSide.claimCopy();
//                // qSide.setTag("side-complex-2-" + side.toString());
//                qSide.setSurface(SURFACE_SIDE);
//                qSide.setNominalFace(side.face);
//                qSide.setupFaceQuad(new FaceVertex(0.5f, bottom, 0), new FaceVertex(1, bottom, 0),
//                        new FaceVertex(1,
//                                flowState.getMidCornerVertexHeight(HorizontalCorner.find(side, side.getLeft()))
//                                        - flowState.getYOffset(),
//                                0),
//                        new FaceVertex(0.5f, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
//                        Direction.UP);
//                terrainQuads.addPolygon(qSide);
//
//                // Side is not simple so have to output tops.
//                // If this quadrant touches an empty block (side or corner)
//                // then subdivide into four tris to allow for smoother vertex colors
//                // along terrain edges (especially lava).
//
//                // left
//                {
//                    final HorizontalCorner corner = HorizontalCorner.find(side, side.getLeft());
//                    IMutablePolygon qi = quadInputsCenterLeft[i];
//                    qi.setVertexNormal(0, normSide[i]);
//                    qi.setVertexNormal(1, normCorner[corner.ordinal()]);
//                    qi.setVertexNormal(2, normCenter);
//
//                    if (needsSubdivision) {
//                        IMutablePolygon qA = qi.claimCopy();
//                        // find vertex at midpoint of corner and center
//                        qA.copyInterpolatedVertexFrom(2, qi, 1, qi, 2, .5f);
//                        terrainQuads.addPolygon(qA);
//
//                        IMutablePolygon qB = qi.claimCopy();
//                        qB.copyVertexFrom(1, qA, 2);
//                        terrainQuads.addPolygon(qB);
//
//                        qi.release();
//                    } else {
//                        terrainQuads.addPolygon(qi);
//                    }
//                }
//
//                // right
//                {
//                    final HorizontalCorner corner = HorizontalCorner.find(side, side.getRight());
//                    IMutablePolygon qi = quadInputsCenterRight[i];
//                    qi.setVertexNormal(0, normCorner[corner.ordinal()]);
//                    qi.setVertexNormal(1, normSide[i]);
//                    qi.setVertexNormal(2, normCenter);
//
//                    if (needsSubdivision) {
//                        IMutablePolygon qA = qi.claimCopy();
//                        // find vertex at midpoint of corner and center
//                        qA.copyInterpolatedVertexFrom(2, qi, 0, qi, 2, .5f);
//                        terrainQuads.addPolygon(qA);
//
//                        IMutablePolygon qB = qi.claimCopy();
//                        qB.copyVertexFrom(0, qA, 2);
//                        terrainQuads.addPolygon(qB);
//
//                        qi.release();
//
//                    } else {
//                        terrainQuads.addPolygon(qi);
//                    }
//                }
//            }
//        }
//
//        // simple top face if it is relatively flat and all sides can be drawn without a
//        // mid vertex
//        if (isTopSimple) {
//            IMutablePolygon qi = template.claimCopy(4);
//
//            // qi.setTag("top-simple");
//
//            qi.setupFaceQuad(fvMidCorner[HorizontalCorner.SOUTH_WEST.ordinal()],
//                    fvMidCorner[HorizontalCorner.SOUTH_EAST.ordinal()],
//                    fvMidCorner[HorizontalCorner.NORTH_EAST.ordinal()],
//                    fvMidCorner[HorizontalCorner.NORTH_WEST.ordinal()], Direction.NORTH);
//            qi.setVertexNormal(0, normCorner[HorizontalCorner.SOUTH_WEST.ordinal()]);
//            qi.setVertexNormal(1, normCorner[HorizontalCorner.SOUTH_EAST.ordinal()]);
//            qi.setVertexNormal(2, normCorner[HorizontalCorner.NORTH_EAST.ordinal()]);
//            qi.setVertexNormal(3, normCorner[HorizontalCorner.NORTH_WEST.ordinal()]);
//
//            // break into tris unless it is truly coplanar
//            if (qi.isOnSinglePlane()) {
//                terrainQuads.addPolygon(qi);
//            } else {
//                if (qi.toPaintableTris(terrainQuads))
//                    qi.release();
//                else
//                    assert false : "Non-co-planaer poly didn't split into tris";
//            }
//        }
//
//        // Bottom face is pre-added to CSGNode templates
//        // IPaintablePoly qBottom = Poly.mutable(template);
//        // qBottom.setSurfaceInstance(SURFACE_SIDE);
//        // qBottom.setNominalFace(Direction.DOWN);
//        // qBottom.setupFaceQuad(0, 0, 1, 1, bottom, Direction.NORTH);
//        // terrainQuads.add(qBottom);
//        // terrainQuads.add(getBottomForState(flowState));
//    }
//
//    private static List<IPolygon> cubeQuads() {
//        final ArrayList<IPolygon> cubeQuads = new ArrayList<>();
//        final IMutablePolygon work = template.claimCopy();
//
//        // note the order here is significant - testing shows this order gives fewest
//        // splits in CSG intersect
//        // most important thing seems to be that sides come first
//        cubeQuads.add(work.setSurface(SURFACE_SIDE).setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP)
//                .toPainted());
//        cubeQuads.add(
//                work.setSurface(SURFACE_SIDE).setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP).toPainted());
//        cubeQuads.add(work.setSurface(SURFACE_SIDE).setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP)
//                .toPainted());
//        cubeQuads.add(
//                work.setSurface(SURFACE_SIDE).setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP).toPainted());
//        cubeQuads.add(work.setSurface(SURFACE_SIDE).setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH)
//                .toPainted());
//        cubeQuads.add(work.setSurface(SURFACE_SIDE).setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH)
//                .toPainted());
//
//        work.release();
//        return cubeQuads;
//    }
//
//    /**
//     * Returns a face vertex at the average of the coordinates of the inputs. Does
//     * not use any other properties of the inputs.
//     */
//    private static FaceVertex midPoint(FaceVertex first, FaceVertex second) {
//        return new FaceVertex((first.x + second.x) / 2, (first.y + second.y) / 2, (first.depth + second.depth) / 2);
//    }
//
//    public TerrainMeshFactory() {
//        super(StateFormat.FLOW, ModelStateData.STATE_FLAG_NEEDS_POS);
//
//    }
//
//    /**
//     * Flowing terrain tends to appear washed out due to simplistic lighting model.
//     * Scale down the vertical component of vertex normals to make the shadows a
//     * little deeper.
//     */
//    private static void shadowEnhance(Vec3f.Mutable vec) {
//        vec.normalize();
//        float y = vec.y();
//        vec.load(vec.x(), y * y, vec.z());
//        vec.normalize();
//    }
//
//    // private static ISuperModelState[] modelStates = new ISuperModelState[120000];
//    // private static int index = 0;
//    private static List<IPolygon> createShapeQuads(TerrainState flowState, boolean needsSubdivision) {
//        // shapeTimer.start();
//        // Collection<IPolygon> result = innerShapeQuads(modelState);
//        // shapeTimer.stop();
//        // synchronized(modelStates)
//        // {
//        // modelStates[index++] = modelState;
//        // if(index == modelStates.length)
//        // {
//        // try
//        // {
//        // ByteBuffer bytes = ByteBuffer.allocate(modelStates.length * 4 * Long.BYTES);
//        // for(ISuperModelState mstate : modelStates)
//        // {
//        // assert mstate.getShape() == ModShapes.TERRAIN_FILLER || mstate.getShape() ==
//        // ModShapes.TERRAIN_HEIGHT;
//        // assert mstate.getTerrainState() != null;
//        // bytes.putLong(mstate.getBits0());
//        // bytes.putLong(mstate.getBits1());
//        // bytes.putLong(mstate.getBits2());
//        // bytes.putLong(mstate.getBits3());
//        // }
//        // bytes.flip();
//        // FileOutputStream fos = new FileOutputStream("terrainState.data");
//        // fos.getChannel().write(bytes);
//        // fos.close();
//        //
//        //
//        // FileInputStream fis = new FileInputStream("terrainState.data");
//        // ByteBuffer testBytes = ByteBuffer.allocate(modelStates.length * 4 *
//        // Long.BYTES);
//        // fis.getChannel().read(testBytes);
//        // fis.close();
//        // testBytes.flip();
//        // for(int i = 0; i < modelStates.length; i++)
//        // {
//        // ModelState testModelState = new ModelState(testBytes.getLong(),
//        // testBytes.getLong(), testBytes.getLong(), testBytes.getLong());
//        // ISuperModelState originalModelState = modelStates[i];
//        // assert testModelState.equalsIncludeStatic(originalModelState);
//        // assert testModelState.getShape() == ModShapes.TERRAIN_FILLER ||
//        // testModelState.getShape() == ModShapes.TERRAIN_HEIGHT;
//        // assert testModelState.getTerrainState() != null;
//        // }
//        // }
//        // catch (Exception e)
//        // {
//        // e.printStackTrace();
//        // }
//        // index = 0;
//        // }
//        // }
//        // if(shapeTimer.stop())
//        // {
//
//        // }
//        // return result;
//        // }
//        // private static MicroTimer shapeTimer = new MicroTimer("terrainGetShapeQuads",
//        // 400000);
//        //
//        //
//        // private Collection<IPolygon> innerShapeQuads(ISuperModelState
//        // modelState)
//        // {
//
//        // synchronized(stateList)
//        // {
//        // stateList.add(modelState.getShape() ==ModShapes.TERRAIN_FILLER ?
//        // -flowState.getStateKey() : flowState.getStateKey());
//        // }
//
//        CSGNode.Root terrainNode;
//        CSGNode.Root cubeNode;
//
//        if (flowState.isTopSimple()) {
//            terrainNode = terrainNodesSimple[getIndexForState(flowState)].clone();
//            cubeNode = cubeNodeSimple.clone();
//        } else // if(flowState.areMostSidesSimple())
//        {
//            terrainNode = terrainNodesHybrid[getIndexForState(flowState)].clone();
//            cubeNode = cubeNodeHybrid.clone();
//        }
//        // else
//        // {
//        // terrainNode = terrainNodesComplex[getIndexForState(flowState)].clone();
//        // cubeNode = this.cubeNodeComplex.clone();
//        // }
//
//        addTerrainQuads(flowState, terrainNode, needsSubdivision);
//
//        // order here is important
//        // terrain has to come first in order to preserve normals when
//        // top terrain face quads are co-planar with cube quads
//        final Collection<IMutablePolygon> mutableResult = CSGMesh.intersect(terrainNode, cubeNode);
//
//        assert mutableResult != null : "Got null terrain mesh - not expected.";
//
//        return IMutablePolygon.paintAndRelease(mutableResult);
//    }
//
//    @Override
//    public int geometricSkyOcclusion(ISuperModelState modelState) {
//        return modelState.getTerrainState().verticalOcclusion();
//    }
//
//    @Override
//    public void produceShapeQuads(ISuperModelState modelState, final Consumer<IPolygon> target) {
//        // TODO: restore this feature, but generalize to all meshes and put in block
//        // dispatcher
////        final Consumer<IMutablePolygon> wrapped = BrocadeConfig.BLOCKS.enableTerrainQuadDebugRender
////                ? QuadHelper.makeRecoloring(target) : target;
//
//        // Hot terrain blocks that border non-hot blocks need a subdivided mesh
//        // for smooth vertex shading. So that we can still use a long key and avoid
//        // instantiating a terrain state for cached meshes, we use the sign bit on
//        // the key to indicate that subdivision is needed.
//        // Subdivision isn't always strictly necessary for all quadrant of an edge block
//        // but subdividing all enables potentially more cache hits at the cost of a few
//        // extra polygons
//        final int hotness = modelState.getTerrainHotness();
//        final boolean needsSubdivision = !(hotness == 0 || hotness == TerrainState.ALL_HOT);
//        final long key = needsSubdivision ? -modelState.getTerrainStateKey() : modelState.getTerrainStateKey();
//
//        if (key == TerrainState.EMPTY_BLOCK_STATE_KEY)
//            return;
//
//        // NB: was checking flowState.isFullCube() and returning cubeQuads() in
//        // that case but can produce incorrect normals in rare cases that will cause
//        // shading on top face to be visibly mismatched to neighbors.
//        // cacheAttempts.incrementAndGet();
//
//        // FIXME: prevent NPE in release due to strange and rare bug somewhere -
//        // possibly a concurrency issue in cache
//        final List<IPolygon> c = modelCache.get(key);
//        if (c == null)
//            assert false : "Got null result from terrain model cache";
//        else {
//            final int limit = c.size();
//            for (int i = 0; i < limit; i++)
//                target.accept(c.get(i));
//        }
//    }
//
//    @Override
//    public boolean isCube(ISuperModelState modelState) {
//        return false;
//        // doing it this way caused lighting problems: massive dark areas
//        // return modelState.getTerrainState().isFullCube();
//    }
//
//    @Override
//    public boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis, ISuperBlock block,
//            ISuperModelState modelState) {
//        return false;
//    }
//
//    @Override
//    public SideShape sideShape(ISuperModelState modelState, Direction side) {
//        return modelState.getTerrainState().isFullCube() ? SideShape.SOLID : SideShape.MISSING;
//    }
//
//    @Override
//    public int getMetaData(ISuperModelState modelState) {
//        return modelState.getTerrainState().centerHeight();
//    }
//
//    @Override
//    public boolean hasLampSurface(ISuperModelState modelState) {
//        return false;
//    }
}
