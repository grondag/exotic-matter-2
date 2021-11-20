/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.primitive.simple;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

import io.vram.bitkit.BitPacker32;
import io.vram.frex.api.model.util.ColorUtil;

import grondag.fermion.orientation.api.FaceEdge;
import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.Xm;
import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.state.CornerJoinFaceStates;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.FaceVertex;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.modelstate.AbstractPrimitiveModelState;
import grondag.xm.modelstate.SimpleModelStateImpl;

@Experimental
public class SquareColumn extends AbstractSimplePrimitive {
	private static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("end", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("side", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("cut", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP_GRADIENT)
			.add("inlay", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP).build();

	public static final XmSurface SURFACE_END = SURFACES.get(0);
	public static final XmSurface SURFACE_SIDE = SURFACES.get(1);
	public static final XmSurface SURFACE_CUT = SURFACES.get(2);
	public static final XmSurface SURFACE_INLAY = SURFACES.get(3);

	public static final int MIN_CUTS = 1;
	public static final int MAX_CUTS = 3;

	private static final BitPacker32<SquareColumn> STATE_PACKER = new BitPacker32<>(null, null);
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

	public static final SquareColumn INSTANCE = new SquareColumn(Xm.id("column_square"));

	@Override
	protected void updateDefaultState(MutablePrimitiveState modelState) {
		setCutCount(3, modelState);
		setCutsOnEdge(true, modelState);
	}

	protected SquareColumn(ResourceLocation id) {
		super(id, ModelStateFlags.CORNER_JOIN, SimpleModelStateImpl.FACTORY, s -> SURFACES);
	}

	@Override
	public void emitQuads(PrimitiveState modelState, Consumer<Polygon> target) {
		final FaceSpec spec = new FaceSpec(getCutCount(modelState), areCutsOnEdge(modelState));

		for (int i = 0; i < 6; i++) {
			makeFaceQuads(modelState, Direction.from3DDataValue(i), spec, target);
		}
	}

	@Override
	public OrientationType orientationType(PrimitiveState modelState) {
		return OrientationType.AXIS;
	}

	private static final Axis[] AXIS = Direction.Axis.values();

	private void makeFaceQuads(PrimitiveState state, Direction face, FaceSpec spec, Consumer<Polygon> target) {
		if (face == null) return;

		// PERF: if have a consumer and doing this dynamically - should consumer simply
		// be a stream?
		// Why create a stream just to pipe it to the consumer? Or cache the result.

		final CornerJoinState bjs = state.cornerJoin();
		final Direction.Axis axis = AXIS[state.orientationIndex() % 3];
		final WritableMesh stream = XmMeshes.claimWritable();

		stream.writer()
			.vertexCount(4)
			// PERF set cull face for unconnected faces - doesn't work if connected
			.lockUV(0, true) //.cullFace(face);
			.saveDefaults();

		final XmSurfaceList surfaces = surfaces(state);

		final boolean isLit = INSTANCE.lampSurface(state) != null;

		if (face.getAxis() == axis) {
			makeCapFace(face, stream, bjs.faceState(face), spec, axis, surfaces, isLit);
		} else {
			makeSideFace(face, stream, bjs.faceState(face), spec, axis, surfaces, isLit);
		}

		final Polygon reader = stream.reader();

		if (reader.origin()) {
			do {
				target.accept(reader);
			} while (reader.next());
		}

		stream.release();
	}

	private void makeSideFace(Direction face, WritableMesh stream, CornerJoinFaceState fjs, FaceSpec spec, Direction.Axis axis, XmSurfaceList surfaces, boolean isLit) {
		if (fjs == CornerJoinFaceStates.NO_FACE) return;

		final MutablePolygon writer = stream.writer();

		final Direction topFace = PolyHelper.positiveDirection(axis);
		final Direction bottomFace = topFace.getOpposite();
		final Direction leftFace = PolyHelper.leftOf(face, topFace);
		final Direction rightFace = PolyHelper.rightOf(face, topFace);

		int actualCutCount = spec.cutCount;

		/** used to randomize cut textures */
		int salt = 1;

		final boolean hasLeftJoin = fjs.isJoined(leftFace, face);
		final boolean hasRightJoin = fjs.isJoined(rightFace, face);
		final boolean hasTopJoin = fjs.isJoined(topFace, face);
		final boolean hasBottomJoin = fjs.isJoined(bottomFace, face);

		if (hasLeftJoin) {
			actualCutCount++;
		}

		if (hasRightJoin) {
			actualCutCount++;
		}

		final float leftMarginWidth = hasLeftJoin ? spec.marginOffset * spec.cutWidth : spec.baseMarginWidth;
		final float rightMarginWidth = hasRightJoin ? spec.marginOffset * spec.cutWidth : spec.baseMarginWidth;
		final float topCapHeight = hasTopJoin ? 0 : spec.baseMarginWidth;
		final float bottomCapHeight = hasBottomJoin ? 0 : spec.baseMarginWidth;

		// bottom
		if (!hasBottomJoin) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, 0, 0, 1, bottomCapHeight, 0, topFace);
			writer.append();
		}

		// top
		if (!hasTopJoin) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, 0, 1 - topCapHeight, 1, 1, 0, topFace);
			writer.append();
		}

		// left margin
		if (leftMarginWidth > 0) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, 0, bottomCapHeight, leftMarginWidth, 1 - topCapHeight, 0, topFace);
			writer.append();
		}

		// right margin
		if (rightMarginWidth > 0) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, 1 - rightMarginWidth, bottomCapHeight, 1, 1 - topCapHeight, 0, topFace);
			writer.append();
		}

		// splines
		for (int i = 0; i < actualCutCount - 1; i++) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth, bottomCapHeight, leftMarginWidth + spec.cutWidth * 2 * (i + 1),
					1 - topCapHeight, 0, topFace);
			writer.append();
		}

		// top left corner
		if (fjs.needsCorner(topFace, leftFace, face)) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 1 - spec.baseMarginWidth, leftMarginWidth + spec.cutWidth, 1, 0, topFace);
			writer.append();
		}

		// bottom left corner
		if (fjs.needsCorner(bottomFace, leftFace, face)) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, Math.max(leftMarginWidth, 0), 0, leftMarginWidth + spec.cutWidth, spec.baseMarginWidth, 0, topFace);
			writer.append();
		}

		// top right corner
		if (fjs.needsCorner(topFace, rightFace, face)) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 1 - spec.baseMarginWidth, Math.min(1 - rightMarginWidth, 1), 1, 0, topFace);
			writer.append();
		}

		// bottom right corner
		if (fjs.needsCorner(bottomFace, rightFace, face)) {
			writer.surface(SURFACE_SIDE);
			writer.setupFaceQuad(face, 1 - rightMarginWidth - spec.cutWidth, 0, Math.min(1 - rightMarginWidth, 1), spec.baseMarginWidth, 0, topFace);
			writer.append();
		}

		for (int i = 0; i < actualCutCount; i++) {
			final float sx0 = Math.max(0, leftMarginWidth + spec.cutWidth * 2 * i);
			final float sx1 = Math.min(1, leftMarginWidth + spec.cutWidth * 2 * i + spec.cutWidth);

			// left face
			if (sx0 > 0.0001f) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(rightFace, bottomCapHeight, 1 - spec.cutDepth, 1 - topCapHeight, 1, 1 - sx0, face), face, isLit);
				writer.append();
			}

			// right face
			if (sx1 < 0.9999) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(leftFace, topCapHeight, 1 - spec.cutDepth, 1 - bottomCapHeight, 1, sx1, face), face, isLit);
				writer.append();
			}

			// top face
			if (topCapHeight > 0) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, sx0, 1 - spec.cutDepth, sx1, 1, 1 - topCapHeight, face), face, isLit);
				writer.append();
			}

			// bottom face
			if (bottomCapHeight > 0) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(topFace, 1 - sx1, 1 - spec.cutDepth, 1 - sx0, 1, 1 - bottomCapHeight, face), face, isLit);
				writer.append();
			}

			// top left corner
			if (fjs.needsCorner(topFace, leftFace, face)) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, Math.max(leftMarginWidth, 0), 1 - spec.cutDepth, leftMarginWidth + spec.cutWidth, 1,
						1 - spec.baseMarginWidth, face), face, isLit);
				writer.append();
			}

			// bottom left corner
			if (fjs.needsCorner(bottomFace, leftFace, face)) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(topFace, 1 - leftMarginWidth - spec.cutWidth, 1 - spec.cutDepth, Math.min(1 - leftMarginWidth, 1),
						1, 1 - spec.baseMarginWidth, face), face, isLit);
				writer.append();
			}

			// top right corner
			if (fjs.needsCorner(topFace, rightFace, face)) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(bottomFace, 1 - rightMarginWidth - spec.cutWidth, 1 - spec.cutDepth,
						Math.min(1 - rightMarginWidth, 1), 1, 1 - spec.baseMarginWidth, face), face, isLit);
				writer.append();
			}

			// bottom right corner
			if (fjs.needsCorner(bottomFace, rightFace, face)) {
				writer.surface(SURFACE_CUT);
				writer.textureSalt(salt++);
				setupCutSideQuad(writer, new SimpleQuadBounds(topFace, Math.max(rightMarginWidth, 0), 1 - spec.cutDepth, rightMarginWidth + spec.cutWidth, 1,
						1 - spec.baseMarginWidth, face), face, isLit);
				writer.append();
			}
		}

		// inner lamp surface can be a single poly
		{
			writer.surface(SURFACE_INLAY);
			writer.setupFaceQuad(face, Math.max(0, leftMarginWidth), bottomCapHeight, Math.min(1, 1 - rightMarginWidth), 1 - topCapHeight, spec.cutDepth,
					topFace);
			writer.append();
		}
	}

	private void makeCapFace(Direction face, WritableMesh stream, CornerJoinFaceState fjs, FaceSpec spec, Direction.Axis axis, XmSurfaceList surfaces, boolean isLit) {
		if (fjs == CornerJoinFaceStates.NO_FACE) return;

		final MutablePolygon writer = stream.writer();

		/** used to randomize cut textures */
		int salt = 1;

		// lamp bottom can be a single poly
		{
			writer.surface(SURFACE_INLAY);
			writer.setupFaceQuad(face, fjs.isJoined(FaceEdge.LEFT_EDGE) ? 0 : spec.baseMarginWidth,
					fjs.isJoined(FaceEdge.BOTTOM_EDGE) ? 0 : spec.baseMarginWidth, fjs.isJoined(FaceEdge.RIGHT_EDGE) ? 1 : 1 - spec.baseMarginWidth,
							fjs.isJoined(FaceEdge.TOP_EDGE) ? 1 : 1 - spec.baseMarginWidth, spec.cutDepth, FaceEdge.TOP_EDGE.toWorld(face));
			writer.append();
		}

		// build quarter slice of cap for each side separately
		// specifications below are oriented with the side at top of cap face

		for (int f = 0; f < FaceEdge.COUNT; f++) {
			final FaceEdge joinSide = FaceEdge.fromOrdinal(f);
			final Direction side = joinSide.toWorld(face);

			if (fjs.isJoined(joinSide)) {
				// This side is joined, so connect cuts to other block on this side.

				// margin corner faces
				{
					writer.vertexCount(3);
					writer.surface(SURFACE_END);
					writer.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0), new FaceVertex(spec.baseMarginWidth, 1, 0),
							new FaceVertex(0, 1, 0), side);
					writer.append();
				}

				{
					writer.vertexCount(3);
					writer.surface(SURFACE_END);
					writer.setupFaceQuad(face, new FaceVertex(1 - spec.baseMarginWidth, 1, 0),
							new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0), new FaceVertex(1, 1, 0), side);
					writer.append();
				}

				// margin corner sides
				{
					writer.surface(SURFACE_CUT);
					writer.textureSalt(salt++);
					setupCutSideQuad(writer, new SimpleQuadBounds(PolyHelper.rightOf(face, side), 1 - spec.baseMarginWidth, 1 - spec.cutDepth, 1, 1,
							1 - spec.baseMarginWidth, face), face, isLit);
					writer.append();
				}

				{
					writer.surface(SURFACE_CUT);
					writer.textureSalt(salt++);
					setupCutSideQuad(writer,
							new SimpleQuadBounds(PolyHelper.leftOf(face, side), 0, 1 - spec.cutDepth, spec.baseMarginWidth, 1, 1 - spec.baseMarginWidth, face), face, isLit);
					writer.append();
				}

				// splines
				for (int i = 0; i < spec.cutCount / 2; i++) {
					final float xLeft = spec.baseMarginWidth + (i * 2 + 1) * spec.cutWidth;
					final float xRight = Math.min(xLeft + spec.cutWidth, 0.5f);

					{
						writer.surface(SURFACE_END);
						writer.setupFaceQuad(face, new FaceVertex(xLeft, 1 - xLeft, 0), new FaceVertex(xRight, 1 - xRight, 0), new FaceVertex(xRight, 1, 0),
								new FaceVertex(xLeft, 1, 0), side);
						writer.append();
					}

					{
						// mirror on right side, reverse winding order
						writer.surface(SURFACE_END);
						writer.setupFaceQuad(face, new FaceVertex(1 - xRight, 1 - xRight, 0), new FaceVertex(1 - xLeft, 1 - xLeft, 0),
								new FaceVertex(1 - xLeft, 1, 0), new FaceVertex(1 - xRight, 1, 0), side);
						writer.append();
					}

					// cut sides
					// with odd number of cuts, these overlap in middle, avoid with this check

					if (xLeft < 0.4999) {
						{
							writer.surface(SURFACE_CUT);
							writer.textureSalt(salt++);
							setupCutSideQuad(writer, new SimpleQuadBounds(PolyHelper.leftOf(face, side), 0, 1 - spec.cutDepth, xLeft, 1, xLeft, face), face, isLit);
							writer.append();
						}

						{
							writer.surface(SURFACE_CUT);
							writer.textureSalt(salt++);
							setupCutSideQuad(writer, new SimpleQuadBounds(PolyHelper.rightOf(face, side), 1 - xLeft, 1 - spec.cutDepth, 1, 1, xLeft, face), face, isLit);
							writer.append();
						}
					}

					if (xRight < 0.4999) {
						{
							writer.surface(SURFACE_CUT);
							writer.textureSalt(salt++);
							setupCutSideQuad(writer,
									new SimpleQuadBounds(PolyHelper.rightOf(face, side), 1 - xRight, 1 - spec.cutDepth, 1, 1, 1 - xRight, face), face, isLit);
							writer.append();
						}

						{
							writer.surface(SURFACE_CUT);
							writer.textureSalt(salt++);
							setupCutSideQuad(writer, new SimpleQuadBounds(PolyHelper.leftOf(face, side), 0, 1 - spec.cutDepth, xRight, 1, 1 - xRight, face), face, isLit);
							writer.append();
						}
					}
				}
			} else {
				// This side isn't joined, so don't connect cuts to other block on this side.

				{
					// outer face
					writer.surface(SURFACE_END);
					writer.setupFaceQuad(face, new FaceVertex(spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0),
							new FaceVertex(1 - spec.baseMarginWidth, 1 - spec.baseMarginWidth, 0), new FaceVertex(1, 1, 0), new FaceVertex(0, 1, 0), side);
					writer.append();
				}

				{
					// outer cut sides
					for (int i = 0; i < (spec.cutCount + 1) / 2; i++) {
						final float offset = spec.baseMarginWidth + (spec.cutWidth * 2 * i);

						writer.surface(SURFACE_CUT);
						writer.textureSalt(salt++);
						setupCutSideQuad(writer, new SimpleQuadBounds(side.getOpposite(), offset, 1 - spec.cutDepth, 1 - offset, 1, 1 - offset, face), face, isLit);
						writer.append();
					}
				}

				for (int i = 0; i < spec.cutCount / 2; i++) {
					final float offset = spec.baseMarginWidth + spec.cutWidth * (2 * i + 1);

					{
						// inner cut sides
						writer.surface(SURFACE_CUT);
						writer.textureSalt(salt++);
						setupCutSideQuad(writer, new SimpleQuadBounds(side, offset, 1 - spec.cutDepth, 1 - offset, 1, offset, face), face, isLit);
						writer.append();
					}

					{
						// spline / center
						writer.surface(SURFACE_END);
						writer.setupFaceQuad(face, new FaceVertex(Math.min(0.5f, offset + spec.cutWidth), 1 - offset - spec.cutWidth, 0),
								new FaceVertex(Math.max(0.5f, 1 - offset - spec.cutWidth), 1 - offset - spec.cutWidth, 0),
								new FaceVertex(1 - offset, 1 - offset, 0), new FaceVertex(offset, 1 - offset, 0), side);
						writer.append();
					}
				}
			}
		}
	}

	private void setupCutSideQuad(MutablePolygon qi, SimpleQuadBounds qb, Direction face, boolean isLit) {
		final int glow = isLit ? 255 : 0;

		qi.setupFaceQuad(
			qb.face,
			new FaceVertex.Colored(qb.x0, qb.y0, qb.depth, ColorUtil.WHITE, glow),
			new FaceVertex.Colored(qb.x1, qb.y0, qb.depth, ColorUtil.WHITE, glow),
			new FaceVertex.Colored(qb.x1, qb.y1, qb.depth, ColorUtil.WHITE, glow / 3),
			new FaceVertex.Colored(qb.x0, qb.y1, qb.depth, ColorUtil.WHITE, glow / 3),
			qb.topFace
		);

		// force vertex normals out to prevent lighting anomalies
		final Vec3i vec = face.getNormal();
		final float x = vec.getX();
		final float y = vec.getY();
		final float z = vec.getZ();

		for (int i = 0; i < 4; i++) {
			qi.normal(i, x, y, z);
		}
	}

	/**
	 * If true, cuts in shape are on the block boundary. Reads value from static
	 * shape bits in model state
	 */
	public static boolean areCutsOnEdge(PrimitiveState modelState) {
		return STATE_ARE_CUTS_ON_EDGE.getValue(modelState.primitiveBits());
	}

	/**
	 * If true, cuts in shape are on the block boundary. Saves value in static shape
	 * bits in model state
	 */
	public static void setCutsOnEdge(boolean areCutsOnEdge, MutablePrimitiveState modelState) {
		modelState.primitiveBits(STATE_ARE_CUTS_ON_EDGE.setValue(areCutsOnEdge, modelState.primitiveBits()));
	}

	/**
	 * Number of cuts that appear on each face of model. Reads value from static
	 * shape bits in model state
	 */
	public static int getCutCount(PrimitiveState modelState) {
		return STATE_CUT_COUNT.getValue(modelState.primitiveBits());
	}

	/**
	 * Number of cuts that appear on each face of model. Saves value in static shape
	 * bits in model state
	 */
	public static void setCutCount(int cutCount, MutablePrimitiveState modelState) {
		modelState.primitiveBits(STATE_CUT_COUNT.setValue(cutCount, modelState.primitiveBits()));
	}

	@Override
	public MutablePrimitiveState geometricState(PrimitiveState fromState) {
		final MutablePrimitiveState result = newState();
		return result;
	}

	@Override
	public boolean doesShapeMatch(PrimitiveState from, PrimitiveState to) {
		return from.primitive() == to.primitive()
				&& from.orientationIndex() == to.orientationIndex()
				&& from.cornerJoin() == to.cornerJoin()
				&& from.primitiveBits() == to.primitiveBits();
	}

	/** A relic from simpler times. */
	@Deprecated
	private static class SimpleQuadBounds {
		public Direction face;
		public float x0;
		public float y0;
		public float x1;
		public float y1;
		public float depth;
		public Direction topFace;

		SimpleQuadBounds(Direction face, float x0, float y0, float x1, float y1, float depth, Direction topFace) {
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
