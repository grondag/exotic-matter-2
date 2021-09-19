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
package grondag.xm.terrain;

//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;

//import net.minecraft.util.math.Box;
//import net.minecraft.util.math.Direction;

//import grondag.fermion.sc.cache.LongSimpleCacheLoader;
//import grondag.fermion.sc.cache.LongSimpleLoadingCache;
//import grondag.xm.XmConfig;
//import grondag.xm.api.mesh.polygon.FaceVertex;
//import grondag.xm.api.mesh.polygon.MutablePolygon;
//import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.modelstate.base.BaseModelStateFactory;
//import grondag.xm.api.orientation.HorizontalEdge;
//import grondag.xm.api.orientation.HorizontalFace;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.terrain.TerrainModelState;
import grondag.xm.api.terrain.TerrainModelState.Mutable;
//import grondag.xm.mesh.vertex.Vec3fImpl;

/**
 * Mesh generator for flowing terrain. Currently used for lava and basalt. Makes
 * no effort to set useful UV values because all quads are expected to be UV
 * locked.
 */
@Internal
public abstract class TerrainSurface extends AbstractTerrainPrimitive {

	protected TerrainSurface(ResourceLocation id, int stateFlags, BaseModelStateFactory<TerrainModelState, Mutable> factory, Function<TerrainModelState, XmSurfaceList> surfaceFunc) {
		super(id, stateFlags | ModelStateFlags.POSITION, factory, surfaceFunc);
	}

	public static final TerrainSurface FILLER = null; // ModelShapes.create("terrain_filler",
	public static final TerrainSurface HEIGHT = null; // ModelShapes.create("terrain_height",

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE | XmSurface.FLAG_IGNORE_DEPTH_FOR_RADOMIZATION)
			.add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

	public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
	public static final XmSurface SURFACE_TOP = SURFACES.get(1);
	public static final XmSurface SURFACE_SIDES = SURFACES.get(1);

	//	private static final Box[] COLLISION_BOUNDS = { new Box(0, 0, 0, 1, 1, 1),
	//			new Box(0, 0, 0, 1, 11F / 12F, 1), new Box(0, 0, 0, 1, 10F / 12F, 1),
	//			new Box(0, 0, 0, 1, 9F / 12F, 1),
	//
	//			new Box(0, 0, 0, 1, 8F / 12F, 1), new Box(0, 0, 0, 1, 7F / 12F, 1),
	//			new Box(0, 0, 0, 1, 6F / 12F, 1), new Box(0, 0, 0, 1, 5F / 12F, 1),
	//
	//			new Box(0, 0, 0, 1, 4F / 12F, 1), new Box(0, 0, 0, 1, 3F / 12F, 1),
	//			new Box(0, 0, 0, 1, 2F / 12F, 1), new Box(0, 0, 0, 1, 1F / 12F, 1),
	//
	//			// These aren't actually valid meta values, but prevent NPE if we get one
	//			// somehow
	//			new Box(0, 0, 0, 1, 1, 1), new Box(0, 0, 0, 1, 1, 1),
	//			new Box(0, 0, 0, 1, 1, 1), new Box(0, 0, 0, 1, 1, 1) };
	//
	//
	//	private static final LongSimpleLoadingCache<List<Polygon>> modelCache = new LongSimpleLoadingCache<>(
	//			new TerrainCacheLoader(), 0xFFFF);
	//
	//	private static class TerrainCacheLoader implements LongSimpleCacheLoader<List<Polygon>> {
	//		@Override
	//		public List<Polygon> load(long key) {
	//			// cacheMisses.incrementAndGet();
	//			return createShapeQuads(new TerrainState(key < 0 ? -key : key, 0), key < 0);
	//		}
	//	}
	//
	//	// PERF: restore pre-cut optimizations
	//	//	private static final CSGNode.Root[] terrainNodesSimple = new CSGNode.Root[5];
	//	//	private static final CSGNode.Root[] terrainNodesHybrid = new CSGNode.Root[5];
	//	//	// private final CSGNode.Root[] terrainNodesComplex = new CSGNode.Root[5];
	//	//
	//	//	private static final CSGNode.Root cubeNodeSimple;
	//	//	private static final CSGNode.Root cubeNodeHybrid;
	//	// private final CSGNode.Root cubeNodeComplex;
	//	//
	//	//	static {
	//	//		template = PolyFactory.COMMON_POOL.newPaintable(4);
	//	//
	//	//		template.lockUV(0, true);
	//	//		template.surface(SURFACE_TOP);
	//	//		// default - need to change for sides and bottom
	//	//		template.nominalFace(Direction.UP);
	//	//		// templateBuilder.setTag("template");
	//	//
	//	//		final MutablePolygon qBottom = template.claimCopy(4);
	//	//		for (int i = 0; i < 5; i++) {
	//	//			// Bottom faces are pre-built
	//	//
	//	//			qBottom.surface(SURFACE_SIDES);
	//	//			qBottom.nominalFace(Direction.DOWN);
	//	//			qBottom.setupFaceQuad(0, 0, 1, 1, getBottomY(i - 2), Direction.NORTH);
	//	//			// qBottom.setTag("bottom-" + i);
	//	//
	//	//			// qBottom = Poly.claimCopyOf(qBottom).setTag("bottom-simple-" + i);
	//	//
	//	//			terrainNodesSimple[i] = CSGNode.create(ImmutableList.of(qBottom.toPainted()));
	//	//
	//	//			// qBottom = Poly.claimCopyOf(qBottom).setTag("bottom-hybrid-" + i);
	//	//
	//	//			terrainNodesHybrid[i] = CSGNode.create(ImmutableList.of(qBottom.toPainted()), false);
	//	//
	//	//			// terrainNodesComplex[i] = CSGNode.create(ImmutableList.of(qBottom), true);
	//	//
	//	//		}
	//	//		qBottom.release();
	//	//
	//	//		List<Polygon> cubeQuads = cubeQuads();
	//	//
	//	//		// cubeQuads.forEach(q -> q.setTag("cube-simple-" +
	//	//		// q.getNominalFace().toString()));
	//	//		cubeNodeSimple = CSGNode.create(cubeQuads);
	//	//
	//	//		cubeQuads = cubeQuads();
	//	//		// cubeQuads.forEach(q -> q.setTag("cube-hybrid" +
	//	//		// q.getNominalFace().toString()));
	//	//		cubeNodeHybrid = CSGNode.create(cubeQuads, false);
	//	//
	//	//		// this.cubeNodeComplex = CSGNode.create(cubeQuads, true);
	//	//	}
	//
	//	// private final AtomicInteger cacheAttempts = new AtomicInteger(0);
	//	// private final AtomicInteger cacheMisses = new AtomicInteger(0);
	//	//
	//	// public void reportCacheHits()
	//	// {
	//	// final int attempts = cacheAttempts.get();
	//	// final int hits = attempts - cacheMisses.get();
	//	// System.out.println(String.format("Terrain cache hits: %d / %d (%f percent)",
	//	// hits, attempts, 100f * hits / attempts ));
	//	// System.out.println(String.format("Cache capacity = %d, maxfill = %d",
	//	// modelCache.capacity, modelCache.maxFill));
	//	// cacheMisses.set(0);
	//	// cacheAttempts.set(0);
	//	// }
	//
	//	private static int getIndexForState(TerrainState state) {
	//		return state.getYOffset() + 2;
	//	}
	//
	//	/**
	//	 * Used for Y coord of bottom face and as lower Y coord of side faces Expects
	//	 * values of -2 through +2 from {@link TerrainState#getYOffset()}
	//	 */
	//	private static int getBottomY(int yOffset) {
	//		return -2 - yOffset;
	//	}
	//
	//	// private static LongOpenHashSet hitMap = new LongOpenHashSet();
	//	// private static AtomicInteger tryCount = new AtomicInteger();
	//	// private static AtomicInteger hitCount = new AtomicInteger();
	//	//
	//	// public static void reportAndClearHitCount()
	//	// {
	//	// synchronized(hitMap)
	//	// {
	//	// Brocade.INSTANCE.info("Terrain geometry potential cache hit rate = %d
	//	// percent", (hitCount.get() * 100) / tryCount.get());
	//	// Brocade.INSTANCE.info("Terrain geometry max cached states = %d",
	//	// hitMap.size());
	//	// tryCount.set(0);
	//	// hitCount.set(0);
	//	// hitMap.clear();
	//	// }
	//	// }
	//
	//	private static class WorkVars {
	//		/**
	//		 * Quads on left (west) side of the top face.<br>
	//		 * Needed for model and to computer center normal.
	//		 */
	//		final MutablePolygon quadInputsCenterLeft[] = new MutablePolygon[4];
	//
	//		/**
	//		 * Quads on right (east) side of the top face.<br>
	//		 * Needed for model and to compute center normal.
	//		 */
	//		final MutablePolygon quadInputsCenterRight[] = new MutablePolygon[4];
	//
	//		/**
	//		 * Quads adjacent to each side midpoint vertex. Needed to compute normals. Will
	//		 * always contains quads for this block but only contains quads for adjacent
	//		 * space if it has a terrain height.
	//		 */
	//		@SuppressWarnings("unchecked")
	//		final ObjectArrayList<MutablePolygon>[] quadInputsSide = new ObjectArrayList[4];
	//
	//		/**
	//		 * Quads adjacent to each corner vertex. Needed to compute normals. Will always
	//		 * contains quads for this block but only contains quads for adjacent spaces if
	//		 * the space has a terrain height.
	//		 */
	//		@SuppressWarnings("unchecked")
	//		final ObjectArrayList<MutablePolygon>[] quadInputsCorner = new ObjectArrayList[4];
	//
	//		final Vec3fImpl.Mutable vecOut = new Vec3fImpl.Mutable(0, 0, 0);
	//		final Vec3fImpl.Mutable vecIn = new Vec3fImpl.Mutable(0, 0, 0);
	//
	//		/**
	//		 * Top face vertex positions for corners of this block. Could be above or below
	//		 * the box bounding box. CSG operations will trim shape to block box later.
	//		 * Initialized to a height of one and changed based on world state.
	//		 */
	//		final FaceVertex fvMidCorner[] = new FaceVertex[HorizontalFace.COUNT];
	//
	//		/**
	//		 * Top face vertex positions for centers of the block at the four corners.
	//		 * Initialized to a height of one and changed based on world state. Used to
	//		 * generate tris needed to compute vertex normals
	//		 */
	//		final FaceVertex fvFarCorner[] = new FaceVertex[HorizontalFace.COUNT];
	//
	//		// Coordinates assume quad will be set up with North=top orientation
	//		// Depth will be set separately.
	//		final FaceVertex fvMidSide[] = new FaceVertex[HorizontalFace.COUNT];
	//
	//		final FaceVertex fvFarSide[] = new FaceVertex[HorizontalFace.COUNT];
	//
	//		final Vec3fImpl normCorner[] = new Vec3fImpl[4];
	//		final Vec3fImpl normSide[] = new Vec3fImpl[4];
	//
	//		private WorkVars() {
	//			for (int i = 0; i < 4; i++) {
	//				quadInputsSide[i] = new ObjectArrayList<>();
	//				quadInputsCorner[i] = new ObjectArrayList<>();
	//			}
	//		}
	//
	//		WorkVars prepare() {
	//			for (int i = 0; i < 4; i++) {
	//				quadInputsSide[i].clear();
	//				quadInputsCorner[i].clear();
	//			}
	//
	//			fvMidCorner[HorizontalEdge.NORTH_EAST.ordinal()] = new FaceVertex(1, 1, 1);
	//			fvMidCorner[HorizontalEdge.NORTH_WEST.ordinal()] = new FaceVertex(0, 1, 1);
	//			fvMidCorner[HorizontalEdge.SOUTH_EAST.ordinal()] = new FaceVertex(1, 0, 1);
	//			fvMidCorner[HorizontalEdge.SOUTH_WEST.ordinal()] = new FaceVertex(0, 0, 1);
	//
	//			fvFarCorner[HorizontalEdge.NORTH_EAST.ordinal()] = new FaceVertex(1.5f, 1.5f, 1);
	//			fvFarCorner[HorizontalEdge.NORTH_WEST.ordinal()] = new FaceVertex(-0.5f, 1.5f, 1);
	//			fvFarCorner[HorizontalEdge.SOUTH_EAST.ordinal()] = new FaceVertex(1.5f, -0.5f, 1);
	//			fvFarCorner[HorizontalEdge.SOUTH_WEST.ordinal()] = new FaceVertex(-0.5f, -0.5f, 1);
	//
	//			fvMidSide[HorizontalFace.NORTH.ordinal()] = new FaceVertex(0.5f, 1f, 1.0f);
	//			fvMidSide[HorizontalFace.SOUTH.ordinal()] = new FaceVertex(0.5f, 0f, 1.0f);
	//			fvMidSide[HorizontalFace.EAST.ordinal()] = new FaceVertex(1.0f, 0.5f, 1.0f);
	//			fvMidSide[HorizontalFace.WEST.ordinal()] = new FaceVertex(0f, 0.5f, 1.0f);
	//
	//			fvFarSide[HorizontalFace.NORTH.ordinal()] = new FaceVertex(0.5f, 1.5f, 1.0f);
	//			fvFarSide[HorizontalFace.SOUTH.ordinal()] = new FaceVertex(0.5f, -0.5f, 1.0f);
	//			fvFarSide[HorizontalFace.EAST.ordinal()] = new FaceVertex(1.5f, 0.5f, 1.0f);
	//			fvFarSide[HorizontalFace.WEST.ordinal()] = new FaceVertex(-0.5f, 0.5f, 1.0f);
	//			return this;
	//		}
	//	}
	//
	//	private static final ThreadLocal<WorkVars> workVars = new ThreadLocal<WorkVars>() {
	//		@Override
	//		protected WorkVars initialValue() {
	//			return new WorkVars();
	//		}
	//	};
	//
	//	private static void addTerrainQuads(TerrainState flowState, CSGNode.Root terrainQuads, boolean needsSubdivision) {
	//		// tryCount.incrementAndGet();
	//		// synchronized(hitMap)
	//		// {
	//		// if(!hitMap.add(flowState.getStateKey()))
	//		// {
	//		// hitCount.incrementAndGet();
	//		// }
	//		// }
	//
	//		final WorkVars w = workVars.get().prepare();
	//		final MutablePolygon quadInputsCenterLeft[] = w.quadInputsCenterLeft;
	//		final MutablePolygon quadInputsCenterRight[] = w.quadInputsCenterRight;
	//		final ObjectArrayList<MutablePolygon>[] quadInputsSide = w.quadInputsSide;
	//		final ObjectArrayList<MutablePolygon>[] quadInputsCorner = w.quadInputsCorner;
	//		final Vec3fImpl.Mutable vecOut = w.vecOut;
	//		final Vec3fImpl.Mutable vecIn = w.vecIn;
	//		final FaceVertex fvMidCorner[] = w.fvMidCorner;
	//		final FaceVertex fvFarCorner[] = w.fvFarCorner;
	//		final FaceVertex fvMidSide[] = w.fvMidSide;
	//		final FaceVertex fvFarSide[] = w.fvFarSide;
	//		final Vec3fImpl normCorner[] = w.normCorner;
	//		final Vec3fImpl normSide[] = w.normSide;
	//
	//		// center vertex setup
	//		final FaceVertex fvCenter = new FaceVertex(0.5f, 0.5f,
	//				1.0f - flowState.getCenterVertexHeight() + flowState.getYOffset());
	//
	//		///////////////////////////////////////////////
	//		// set up corner heights and face vertices
	//		///////////////////////////////////////////////
	//
	//		// Coordinates assume quad will be set up with North=top orientation
	//		// Depth will be set separately.
	//
	//		for (int i = 0; i < HorizontalEdge.COUNT; i++) {
	//			final HorizontalEdge corner = HorizontalEdge.fromOrdinal(i);
	//			fvMidCorner[i] = fvMidCorner[i]
	//					.withDepth(1 - flowState.getMidCornerVertexHeight(corner) + flowState.getYOffset());
	//			fvFarCorner[i] = fvFarCorner[i]
	//					.withDepth(1 - flowState.getFarCornerVertexHeight(corner) + flowState.getYOffset());
	//		}
	//
	//		for (int i = 0; i < HorizontalFace.COUNT; i++) {
	//			final HorizontalFace side = HorizontalFace.fromOrdinal(i);
	//
	//			fvMidSide[i] = fvMidSide[i].withDepth(1 - flowState.getMidSideVertexHeight(side) + flowState.getYOffset());
	//			fvFarSide[i] = fvFarSide[i].withDepth(1 - flowState.getFarSideVertexHeight(side) + flowState.getYOffset());
	//
	//			// build quads on the top of this block that that border this side (left and
	//			// right)
	//			// these are always included in the vertex normal calculations for the side
	//			// midpoint and corner vertices
	//
	//			MutablePolygon qiWork = template.claimCopy(3);
	//			qiWork.setupFaceQuad(fvMidSide[i], fvMidCorner[HorizontalEdge.find(side, side.left()).ordinal()],
	//					fvCenter, Direction.NORTH);
	//			quadInputsCenterLeft[i] = qiWork;
	//			quadInputsSide[i].add(qiWork);
	//			quadInputsCorner[HorizontalEdge.find(side, side.left()).ordinal()].add(qiWork);
	//
	//			qiWork = template.claimCopy(3);
	//			qiWork.setupFaceQuad(fvMidCorner[HorizontalEdge.find(side, side.right()).ordinal()], fvMidSide[i],
	//					fvCenter, Direction.NORTH);
	//			quadInputsCenterRight[i] = qiWork;
	//			quadInputsSide[i].add(qiWork);
	//			quadInputsCorner[HorizontalEdge.find(side, side.right()).ordinal()].add(qiWork);
	//
	//			final boolean isSidePresent = flowState.height(side) != TerrainState.NO_BLOCK;
	//
	//			// add side block tri that borders this block if it is there
	//			if (isSidePresent) {
	//				qiWork = template.claimCopy(3);
	//				qiWork.setupFaceQuad(fvFarSide[i], fvMidCorner[HorizontalEdge.find(side, side.left()).ordinal()],
	//						fvMidSide[i], Direction.NORTH);
	//				quadInputsSide[i].add(qiWork);
	//				quadInputsCorner[HorizontalEdge.find(side, side.left()).ordinal()].add(qiWork);
	//
	//				qiWork = template.claimCopy(3);
	//				qiWork.setupFaceQuad(fvMidCorner[HorizontalEdge.find(side, side.right()).ordinal()], fvFarSide[i],
	//						fvMidSide[i], Direction.NORTH);
	//				quadInputsSide[i].add(qiWork);
	//				quadInputsCorner[HorizontalEdge.find(side, side.right()).ordinal()].add(qiWork);
	//			}
	//
	//			// add side block tris that connect to corner but do not border side
	//			// if both the side and corner block are present, this will be a tri that
	//			// spans both the side and corner block (and will affect normals proportional to
	//			// area)
	//
	//			// if only the side is present, will be the half on the side block and if
	//			// the side block is missing but the corner block is present will be the part
	//			// that is on the corner block.
	//
	//			// in the cases where either the side or corner block is missing, terrain state
	//			// will compute the height of midpoint between them to be the 1/2 block less
	//			// than
	//			// the height of center of the block that is present (see
	//			// TerrainState.calcMidSideVertexHeight)
	//
	//			final HorizontalEdge leftCorner = HorizontalEdge.find(side, side.left());
	//			final boolean isLeftCornerPresent = flowState.height(leftCorner) != TerrainState.NO_BLOCK;
	//
	//			final HorizontalEdge rightCorner = HorizontalEdge.find(side, side.right());
	//			final boolean isRightCornerPresent = flowState.height(rightCorner) != TerrainState.NO_BLOCK;
	//
	//			if (isSidePresent) {
	//				qiWork = template.claimCopy(3);
	//
	//				final FaceVertex leftFarCorner = isLeftCornerPresent
	//
	//						// have both the corner and side so do one big tri for both
	//						? fvFarCorner[leftCorner.ordinal()]
	//
	//								// only have tri on side block, vertex for side of missing corner will
	//								// be half a block lower than the side's center height
	//								: midPoint(fvFarSide[i], fvFarCorner[leftCorner.ordinal()])
	//								.withDepth(fvFarSide[i].depth + 0.5f);
	//
	//						qiWork.setupFaceQuad(fvMidCorner[leftCorner.ordinal()], fvFarSide[i], leftFarCorner, Direction.NORTH);
	//						quadInputsCorner[leftCorner.ordinal()].add(qiWork);
	//
	//						qiWork = template.claimCopy(3);
	//
	//						final FaceVertex rightFarCorner = isRightCornerPresent
	//
	//								// have both the corner and side so do one big tri for both
	//								? fvFarCorner[rightCorner.ordinal()]
	//
	//										// only have tri on side block, vertex for side of missing corner will
	//										// be half a block lower than the side's center height
	//										: midPoint(fvFarSide[i], fvFarCorner[rightCorner.ordinal()])
	//										.withDepth(fvFarSide[i].depth + 0.5f);
	//
	//								qiWork.setupFaceQuad(fvMidCorner[rightCorner.ordinal()], rightFarCorner, fvFarSide[i],
	//										Direction.NORTH);
	//								quadInputsCorner[rightCorner.ordinal()].add(qiWork);
	//			} else {
	//				if (isLeftCornerPresent) {
	//					// only have the corner
	//					qiWork = template.claimCopy(3);
	//					qiWork.setupFaceQuad(fvMidCorner[leftCorner.ordinal()],
	//							midPoint(fvFarSide[i], fvFarCorner[leftCorner.ordinal()])
	//							.withDepth(fvFarCorner[leftCorner.ordinal()].depth + 0.5f),
	//							fvFarCorner[leftCorner.ordinal()], Direction.NORTH);
	//					quadInputsCorner[leftCorner.ordinal()].add(qiWork);
	//				}
	//
	//				if (isRightCornerPresent) {
	//					// only have the corner
	//					qiWork = template.claimCopy(3);
	//					qiWork.setupFaceQuad(fvMidCorner[rightCorner.ordinal()], fvFarCorner[rightCorner.ordinal()],
	//							midPoint(fvFarSide[i], fvFarCorner[rightCorner.ordinal()])
	//							.withDepth(fvFarCorner[rightCorner.ordinal()].depth + 0.5f),
	//							Direction.NORTH);
	//					quadInputsCorner[rightCorner.ordinal()].add(qiWork);
	//				}
	//			}
	//		}
	//
	//		final int bottom = getBottomY(flowState.getYOffset());
	//
	//		vecOut.load((Vec3fImpl) quadInputsCenterLeft[0].faceNormal()).scale(quadInputsCenterLeft[0].area());
	//		vecOut.add(vecIn.load((Vec3fImpl) quadInputsCenterLeft[1].faceNormal()).scale(quadInputsCenterLeft[1].area()));
	//		vecOut.add(vecIn.load((Vec3fImpl) quadInputsCenterLeft[2].faceNormal()).scale(quadInputsCenterLeft[2].area()));
	//		vecOut.add(vecIn.load((Vec3fImpl) quadInputsCenterLeft[3].faceNormal()).scale(quadInputsCenterLeft[3].area()));
	//		vecOut.add(vecIn.load((Vec3fImpl) quadInputsCenterRight[0].faceNormal()).scale(quadInputsCenterRight[0].area()));
	//		vecOut.add(vecIn.load((Vec3fImpl) quadInputsCenterRight[1].faceNormal()).scale(quadInputsCenterRight[1].area()));
	//		vecOut.add(vecIn.load((Vec3fImpl) quadInputsCenterRight[2].faceNormal()).scale(quadInputsCenterRight[2].area()));
	//		vecOut.add(vecIn.load((Vec3fImpl) quadInputsCenterRight[3].faceNormal()).scale(quadInputsCenterRight[3].area()));
	//		shadowEnhance(vecOut);
	//		final Vec3fImpl normCenter = vecOut.toImmutable();
	//
	//		// compute weighted normals for side and corner vertices
	//		// four of each, so reusing the same 0-3 loop for both
	//		for (int i = 0; i < 4; i++) {
	//			vecOut.load(0, 0, 0);
	//			ObjectArrayList<MutablePolygon> list = quadInputsSide[i];
	//			int limit = list.size();
	//			for (int j = 0; j < limit; j++) {
	//				final MutablePolygon qi = list.get(j);
	//				vecOut.add(vecIn.load((Vec3fImpl) qi.faceNormal()).scale(qi.area()));
	//			}
	//			shadowEnhance(vecOut);
	//			normSide[i] = vecOut.toImmutable();
	//
	//			vecOut.load(0, 0, 0);
	//			list = quadInputsCorner[i];
	//			limit = list.size();
	//			for (int j = 0; j < limit; j++) {
	//				final MutablePolygon qi = list.get(j);
	//				vecOut.add(vecIn.load((Vec3fImpl) qi.faceNormal()).scale(qi.area()));
	//			}
	//			shadowEnhance(vecOut);
	//			normCorner[i] = vecOut.toImmutable();
	//		}
	//
	//		final boolean isTopSimple = XmConfig.simplifyTerrainBlockGeometry && flowState.isTopSimple()
	//				&& !needsSubdivision;
	//
	//		// note that outputting sides first seems to work best for CSG intersect
	//		// performance
	//		// with convex polyhedra tend to get an unbalanced BSP tree - not much can do
	//		// about it without creating unwanted splits
	//		for (int i = 0; i < HorizontalFace.COUNT; i++) {
	//			final HorizontalFace side = HorizontalFace.fromOrdinal(i);
	//
	//			// don't use middle vertex if it is close to being in line with corners
	//			if (XmConfig.simplifyTerrainBlockGeometry && flowState.isSideSimple(side) && !needsSubdivision) {
	//
	//				// side
	//				final MutablePolygon qSide = template.claimCopy();
	//				// qSide.setTag("side-simple-" + side.toString());
	//
	//				final HorizontalEdge cornerLeft = HorizontalEdge.find(side, side.left());
	//				final HorizontalEdge cornerRight = HorizontalEdge.find(side, side.right());
	//
	//				qSide.surface(SURFACE_SIDES);
	//				qSide.nominalFace(side.face);
	//
	//				qSide.setupFaceQuad(new FaceVertex(0, bottom, 0), new FaceVertex(1, bottom, 0),
	//						new FaceVertex(1, flowState.getMidCornerVertexHeight(cornerLeft) - flowState.getYOffset(), 0),
	//						new FaceVertex(0, flowState.getMidCornerVertexHeight(cornerRight) - flowState.getYOffset(), 0),
	//						Direction.UP);
	//				terrainQuads.addPolygon(qSide);
	//
	//				// if side is simple top *may* be not necessarily so - build top if not simple
	//				if (!isTopSimple) {
	//					final MutablePolygon qi = template.claimCopy(3);
	//
	//					// qi.setTag("top-simpleside-" + side.toString());
	//
	//					qi.setupFaceQuad(fvMidCorner[cornerLeft.ordinal()], fvCenter, fvMidCorner[cornerRight.ordinal()],
	//							Direction.NORTH);
	//					qi.normal(0, normCorner[cornerLeft.ordinal()]);
	//					qi.normal(1, normCenter);
	//					qi.normal(2, normCorner[cornerRight.ordinal()]);
	//					terrainQuads.addPolygon(qi);
	//				}
	//
	//			} else // side and top not simple
	//			{
	//
	//				// Sides
	//				MutablePolygon qSide = template.claimCopy();
	//				// qSide.setTag("side-complex-1-" + side.toString());
	//				qSide.surface(SURFACE_SIDES);
	//				qSide.nominalFace(side.face);
	//
	//				qSide.setupFaceQuad(new FaceVertex(0, bottom, 0), new FaceVertex(0.5f, bottom, 0),
	//						new FaceVertex(0.5f, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
	//						new FaceVertex(0,
	//								flowState.getMidCornerVertexHeight(HorizontalEdge.find(side, side.right()))
	//								- flowState.getYOffset(),
	//								0),
	//						Direction.UP);
	//				terrainQuads.addPolygon(qSide);
	//
	//				qSide = qSide.claimCopy();
	//				// qSide.setTag("side-complex-2-" + side.toString());
	//				qSide.surface(SURFACE_SIDES);
	//				qSide.nominalFace(side.face);
	//				qSide.setupFaceQuad(new FaceVertex(0.5f, bottom, 0), new FaceVertex(1, bottom, 0),
	//						new FaceVertex(1,
	//								flowState.getMidCornerVertexHeight(HorizontalEdge.find(side, side.left()))
	//								- flowState.getYOffset(),
	//								0),
	//						new FaceVertex(0.5f, flowState.getMidSideVertexHeight(side) - flowState.getYOffset(), 0),
	//						Direction.UP);
	//				terrainQuads.addPolygon(qSide);
	//
	//				// Side is not simple so have to output tops.
	//				// If this quadrant touches an empty block (side or corner)
	//				// then subdivide into four tris to allow for smoother vertex colors
	//				// along terrain edges (especially lava).
	//
	//				// left
	//				{
	//					final HorizontalEdge corner = HorizontalEdge.find(side, side.left());
	//					final MutablePolygon qi = quadInputsCenterLeft[i];
	//					qi.normal(0, normSide[i]);
	//					qi.normal(1, normCorner[corner.ordinal()]);
	//					qi.normal(2, normCenter);
	//
	//					if (needsSubdivision) {
	//						final MutablePolygon qA = qi.claimCopy();
	//						// find vertex at midpoint of corner and center
	//						qA.copyInterpolatedVertexFrom(2, qi, 1, qi, 2, .5f);
	//						terrainQuads.addPolygon(qA);
	//
	//						final MutablePolygon qB = qi.claimCopy();
	//						qB.copyVertexFrom(1, qA, 2);
	//						terrainQuads.addPolygon(qB);
	//
	//						qi.release();
	//					} else {
	//						terrainQuads.addPolygon(qi);
	//					}
	//				}
	//
	//				// right
	//				{
	//					final HorizontalEdge corner = HorizontalEdge.find(side, side.right());
	//					final MutablePolygon qi = quadInputsCenterRight[i];
	//					qi.normal(0, normCorner[corner.ordinal()]);
	//					qi.normal(1, normSide[i]);
	//					qi.normal(2, normCenter);
	//
	//					if (needsSubdivision) {
	//						final MutablePolygon qA = qi.claimCopy();
	//						// find vertex at midpoint of corner and center
	//						qA.copyInterpolatedVertexFrom(2, qi, 0, qi, 2, .5f);
	//						terrainQuads.addPolygon(qA);
	//
	//						final MutablePolygon qB = qi.claimCopy();
	//						qB.copyVertexFrom(0, qA, 2);
	//						terrainQuads.addPolygon(qB);
	//
	//						qi.release();
	//
	//					} else {
	//						terrainQuads.addPolygon(qi);
	//					}
	//				}
	//			}
	//		}
	//
	//		// simple top face if it is relatively flat and all sides can be drawn without a
	//		// mid vertex
	//		if (isTopSimple) {
	//			final MutablePolygon qi = template.claimCopy(4);
	//
	//			// qi.setTag("top-simple");
	//
	//			qi.setupFaceQuad(fvMidCorner[HorizontalEdge.SOUTH_WEST.ordinal()],
	//					fvMidCorner[HorizontalEdge.SOUTH_EAST.ordinal()],
	//					fvMidCorner[HorizontalEdge.NORTH_EAST.ordinal()],
	//					fvMidCorner[HorizontalEdge.NORTH_WEST.ordinal()], Direction.NORTH);
	//			qi.normal(0, normCorner[HorizontalEdge.SOUTH_WEST.ordinal()]);
	//			qi.normal(1, normCorner[HorizontalEdge.SOUTH_EAST.ordinal()]);
	//			qi.normal(2, normCorner[HorizontalEdge.NORTH_EAST.ordinal()]);
	//			qi.normal(3, normCorner[HorizontalEdge.NORTH_WEST.ordinal()]);
	//
	//			// break into tris unless it is truly coplanar
	//			if (qi.isOnSinglePlane()) {
	//				terrainQuads.addPolygon(qi);
	//			} else {
	//				if (qi.toPaintableTris(terrainQuads)) {
	//					qi.release();
	//				} else {
	//					assert false : "Non-co-planaer poly didn't split into tris";
	//				}
	//			}
	//		}
	//
	//		// Bottom face is pre-added to CSGNode templates
	//		// IPaintablePoly qBottom = Poly.mutable(template);
	//		// qBottom.surfaceInstance(SURFACE_SIDES);
	//		// qBottom.nominalFace(Direction.DOWN);
	//		// qBottom.setupFaceQuad(0, 0, 1, 1, bottom, Direction.NORTH);
	//		// terrainQuads.add(qBottom);
	//		// terrainQuads.add(getBottomForState(flowState));
	//	}
	//
	//	private static List<Polygon> cubeQuads() {
	//		final ArrayList<Polygon> cubeQuads = new ArrayList<>();
	//		final MutablePolygon work = template.claimCopy();
	//
	//		// note the order here is significant - testing shows this order gives fewest
	//		// splits in CSG intersect
	//		// most important thing seems to be that sides come first
	//		cubeQuads.add(work.surface(SURFACE_SIDES).setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP)
	//				.toPainted());
	//		cubeQuads.add(
	//				work.surface(SURFACE_SIDES).setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP).toPainted());
	//		cubeQuads.add(work.surface(SURFACE_SIDES).setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP)
	//				.toPainted());
	//		cubeQuads.add(
	//				work.surface(SURFACE_SIDES).setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP).toPainted());
	//		cubeQuads.add(work.surface(SURFACE_SIDES).setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH)
	//				.toPainted());
	//		cubeQuads.add(work.surface(SURFACE_SIDES).setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH)
	//				.toPainted());
	//
	//		work.release();
	//		return cubeQuads;
	//	}
	//
	//	/**
	//	 * Returns a face vertex at the average of the coordinates of the inputs. Does
	//	 * not use any other properties of the inputs.
	//	 */
	//	private static FaceVertex midPoint(FaceVertex first, FaceVertex second) {
	//		return new FaceVertex((first.x + second.x) / 2, (first.y + second.y) / 2, (first.depth + second.depth) / 2);
	//	}
	//
	//	/**
	//	 * Flowing terrain tends to appear washed out due to simplistic lighting model.
	//	 * Scale down the vertical component of vertex normals to make the shadows a
	//	 * little deeper.
	//	 */
	//	private static void shadowEnhance(Vec3fImpl.Mutable vec) {
	//		vec.normalize();
	//		final float y = vec.y();
	//		vec.load(vec.x(), y * y, vec.z());
	//		vec.normalize();
	//	}
	//
	//	// private static ISuperModelState[] modelStates = new ISuperModelState[120000];
	//	// private static int index = 0;
	//	private static List<Polygon> createShapeQuads(TerrainState flowState, boolean needsSubdivision) {
	//		// shapeTimer.start();
	//		// Collection<Polygon> result = innerShapeQuads(modelState);
	//		// shapeTimer.stop();
	//		// synchronized(modelStates)
	//		// {
	//		// modelStates[index++] = modelState;
	//		// if(index == modelStates.length)
	//		// {
	//		// try
	//		// {
	//		// ByteBuffer bytes = ByteBuffer.allocate(modelStates.length * 4 * Long.BYTES);
	//		// for(ISuperModelState mstate : modelStates)
	//		// {
	//		// assert mstate.getShape() == ModShapes.TERRAIN_FILLER || mstate.getShape() ==
	//		// ModShapes.TERRAIN_HEIGHT;
	//		// assert mstate.getTerrainState() != null;
	//		// bytes.putLong(mstate.getBits0());
	//		// bytes.putLong(mstate.getBits1());
	//		// bytes.putLong(mstate.getBits2());
	//		// bytes.putLong(mstate.getBits3());
	//		// }
	//		// bytes.flip();
	//		// FileOutputStream fos = new FileOutputStream("terrainState.data");
	//		// fos.getChannel().write(bytes);
	//		// fos.close();
	//		//
	//		//
	//		// FileInputStream fis = new FileInputStream("terrainState.data");
	//		// ByteBuffer testBytes = ByteBuffer.allocate(modelStates.length * 4 *
	//		// Long.BYTES);
	//		// fis.getChannel().read(testBytes);
	//		// fis.close();
	//		// testBytes.flip();
	//		// for(int i = 0; i < modelStates.length; i++)
	//		// {
	//		// ModelState testModelState = new ModelState(testBytes.getLong(),
	//		// testBytes.getLong(), testBytes.getLong(), testBytes.getLong());
	//		// ISuperModelState originalModelState = modelStates[i];
	//		// assert testModelState.equalsIncludeStatic(originalModelState);
	//		// assert testModelState.getShape() == ModShapes.TERRAIN_FILLER ||
	//		// testModelState.getShape() == ModShapes.TERRAIN_HEIGHT;
	//		// assert testModelState.getTerrainState() != null;
	//		// }
	//		// }
	//		// catch (Exception e)
	//		// {
	//		// e.printStackTrace();
	//		// }
	//		// index = 0;
	//		// }
	//		// }
	//		// if(shapeTimer.stop())
	//		// {
	//
	//		// }
	//		// return result;
	//		// }
	//		// private static MicroTimer shapeTimer = new MicroTimer("terrainGetShapeQuads",
	//		// 400000);
	//		//
	//		//
	//		// private Collection<Polygon> innerShapeQuads(ISuperModelState
	//		// modelState)
	//		// {
	//
	//		// synchronized(stateList)
	//		// {
	//		// stateList.add(modelState.getShape() ==ModShapes.TERRAIN_FILLER ?
	//		// -flowState.getStateKey() : flowState.getStateKey());
	//		// }
	//
	//		CSGNode.Root terrainNode;
	//		CSGNode.Root cubeNode;
	//
	//		if (flowState.isTopSimple()) {
	//			terrainNode = terrainNodesSimple[getIndexForState(flowState)].clone();
	//			cubeNode = cubeNodeSimple.clone();
	//		} else // if(flowState.areMostSidesSimple())
	//		{
	//			terrainNode = terrainNodesHybrid[getIndexForState(flowState)].clone();
	//			cubeNode = cubeNodeHybrid.clone();
	//		}
	//		// else
	//		// {
	//		// terrainNode = terrainNodesComplex[getIndexForState(flowState)].clone();
	//		// cubeNode = this.cubeNodeComplex.clone();
	//		// }
	//
	//		addTerrainQuads(flowState, terrainNode, needsSubdivision);
	//
	//		// order here is important
	//		// terrain has to come first in order to preserve normals when
	//		// top terrain face quads are co-planar with cube quads
	//		final Collection<MutablePolygon> mutableResult = CSGMesh.intersect(terrainNode, cubeNode);
	//
	//		assert mutableResult != null : "Got null terrain mesh - not expected.";
	//
	//		return MutablePolygon.paintAndRelease(mutableResult);
	//	}
	//
	//
	//	@Override
	//	public void emitQuads(TerrainModelState modelState, Consumer<Polygon> target) {
	//		// TODO: restore this feature, but generalize to all meshes and put in block
	//		// dispatcher
	//		//        final Consumer<MutablePolygon> wrapped = BrocadeConfig.BLOCKS.enableTerrainQuadDebugRender
	//		//                ? QuadHelper.makeRecoloring(target) : target;
	//
	//		// Hot terrain blocks that border non-hot blocks need a subdivided mesh
	//		// for smooth vertex shading. So that we can still use a long key and avoid
	//		// instantiating a terrain state for cached meshes, we use the sign bit on
	//		// the key to indicate that subdivision is needed.
	//		// Subdivision isn't always strictly necessary for all quadrant of an edge block
	//		// but subdividing all enables potentially more cache hits at the cost of a few
	//		// extra polygons
	//		final int hotness = modelState.getTerrainHotness();
	//		final boolean needsSubdivision = !(hotness == 0 || hotness == TerrainState.ALL_HOT);
	//		final long key = needsSubdivision ? -modelState.getTerrainStateKey() : modelState.getTerrainStateKey();
	//
	//		if (key == TerrainState.EMPTY_BLOCK_STATE_KEY) {
	//			return;
	//		}
	//
	//		// NB: was checking flowState.isFullCube() and returning cubeQuads() in
	//		// that case but can produce incorrect normals in rare cases that will cause
	//		// shading on top face to be visibly mismatched to neighbors.
	//		// cacheAttempts.incrementAndGet();
	//
	//		// FIXME: prevent NPE in release due to strange and rare bug somewhere -
	//		// possibly a concurrency issue in cache
	//		final List<Polygon> c = modelCache.get(key);
	//		if (c == null) {
	//			assert false : "Got null result from terrain model cache";
	//		} else {
	//			final int limit = c.size();
	//			for (int i = 0; i < limit; i++) {
	//				target.accept(c.get(i));
	//			}
	//		}
	//	}
}
