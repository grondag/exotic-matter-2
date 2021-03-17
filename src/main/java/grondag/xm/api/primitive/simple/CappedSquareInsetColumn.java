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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3i;

import grondag.fermion.color.Color;
import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.Xm;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.CsgMeshBuilder;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.FaceVertex;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;

@Experimental
public class CappedSquareInsetColumn  {
	private CappedSquareInsetColumn() {}

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("ends", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("outer", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("cut", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP_GRADIENT)
			.add("inner", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP)
			.build();

	public static final XmSurface SURFACE_ENDS = SURFACES.get(0);
	public static final XmSurface SURFACE_OUTER = SURFACES.get(1);
	public static final XmSurface SURFACE_CUT = SURFACES.get(2);
	public static final XmSurface SURFACE_INNER = SURFACES.get(3);

	private static Axis[] AXES = Axis.values();

	static final float MIN = 2f/16f;
	static final float MAX = 1 - MIN;
	static final float DEPTH = 1f/8f;
	static final float IMIN = MIN + DEPTH;
	static final float IMAX = 1 - IMIN;

	private static XmMesh factory(PrimitiveState modelState) {
		final PolyTransform pt = PolyTransform.get(modelState);

		final CsgMeshBuilder csg = CsgMeshBuilder.threadLocal();
		final SimpleJoinState state = modelState.simpleJoin();

		boolean hasUpper = false;
		boolean hasLower = false;
		final Axis axis = AXES[modelState.orientationIndex()];
		final Direction up = Direction.from(axis, AxisDirection.POSITIVE);
		final Direction down = Direction.from(axis, AxisDirection.NEGATIVE);

		emitMiddleSection(csg, pt, modelState.primitive().lampSurface(modelState) != null);

		csg.push();

		if (!state.isJoined(up)) {
			emitCapSection(csg.input(), pt, axis != Axis.X);
			csg.union();
			hasUpper = true;
		}

		if (!state.isJoined(down)) {
			emitCapSection(csg.input(), pt, axis == Axis.X);
			csg.union();
			hasLower = true;
		}

		csg.pop();

		if(hasLower || hasUpper) {
			csg.union();
		}

		// Filter out quads that are occluded by connection
		final WritableMesh result = csg.buildMutable();

		if(!hasLower || !hasUpper) {
			final Polygon reader = result.reader();
			if(reader.origin()) {
				do {
					final Direction cullFace = reader.cullFace();
					if((!hasLower && cullFace == down) || (!hasUpper && cullFace == up)) {
						reader.delete();
					}
				} while (reader.next());
			}
		}

		return result.releaseToReader();
	}

	private static final void emitMiddleSection(CsgMeshBuilder csg, PolyTransform pt, boolean isLit) {
		final MutablePolygon writer = csg.input().writer();

		writer.colorAll(0, 0xFFFFFFFF)
		.surface(SURFACE_OUTER)
		.lockUV(0, false)
		.rotation(0, TextureOrientation.IDENTITY)
		.sprite(0, "")
		.saveDefaults();

		writer.setupFaceQuad(Direction.DOWN, MIN, MIN, MAX, MAX, 0, Direction.NORTH);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.UP, MIN, MIN, MAX, MAX, 0, Direction.NORTH);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.EAST, MIN, 0, MAX, 1, MIN, Direction.UP);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.WEST, MIN, 0, MAX, 1, MIN, Direction.UP);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.NORTH, MIN, 0, MAX, 1, MIN, Direction.UP);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.SOUTH, MIN, 0, MAX, 1, MIN, Direction.UP);
		pt.accept(writer);
		writer.append();

		csg.union();

		cutSide(Direction.EAST, csg, pt, isLit);
		cutSide(Direction.WEST, csg, pt, isLit);
		cutSide(Direction.NORTH, csg, pt, isLit);
		cutSide(Direction.SOUTH, csg, pt, isLit);
	}

	// UGLY: some copy pasta
	private static void cutSide(Direction face, CsgMeshBuilder csg, PolyTransform pt, boolean isLit) {
		final WritableMesh mesh = csg.input();
		final MutablePolygon writer = mesh.writer();

		writer.colorAll(0, 0xFFFFFFFF);
		writer.lockUV(0, true);
		writer.rotation(0, TextureOrientation.IDENTITY);
		writer.sprite(0, "");
		writer.saveDefaults();

		writer.surface(SURFACE_INNER);
		writer.setupFaceQuad(face.getOpposite(), IMIN, 0, IMAX, 1, 1 - IMIN, Direction.UP);
		pt.accept(writer);
		writer.append();

		writer.surface(SURFACE_OUTER);
		writer.setupFaceQuad(face, IMIN, 0, IMAX, 1, 0, Direction.UP);
		pt.accept(writer);
		writer.append();

		setupCutSideQuad(writer, pt, IMIN, 1 - IMIN, 1 - IMIN, 1, 0, Direction.DOWN, face, isLit);
		setupCutSideQuad(writer, pt, IMIN, 1 - IMIN, 1 - IMIN, 1, 0, Direction.UP, face, isLit);

		setupCutSideQuad(writer, pt, 0, 1 - IMIN, 1, 1, IMIN, PolyHelper.leftOf(face, Direction.UP), face, isLit);
		setupCutSideQuad(writer, pt, 0, 1 - IMIN, 1, 1, IMIN, PolyHelper.rightOf(face, Direction.UP), face, isLit);
		csg.difference();
	}

	// UGLY: copy pasta
	private static void setupCutSideQuad(MutablePolygon poly, PolyTransform pt, float x0, float y0, float x1, float y1, float depth, Direction face, Direction topFace, boolean isLit) {
		final int glow = isLit ? 255 : 0;

		poly.surface(SURFACE_CUT);

		poly.setupFaceQuad(face,
				new FaceVertex.Colored(x0, y0, depth, Color.WHITE, glow),
				new FaceVertex.Colored(x1, y0, depth, Color.WHITE, glow),
				new FaceVertex.Colored(x1, y1, depth, Color.WHITE, glow / 3),
				new FaceVertex.Colored(x0, y1, depth, Color.WHITE, glow / 3),
				topFace);

		// force vertex normals out to prevent lighting anomalies
		final Vec3i vec = face.getVector();
		final float x = vec.getX();
		final float y = vec.getY();
		final float z = vec.getZ();
		for(int i = 0; i < 4; i++) {
			poly.normal(i, x, y, z);
		}
		pt.accept(poly);
		poly.append();
	}

	private static final void emitCapSection(WritableMesh mesh, PolyTransform pt, boolean top) {
		final MutablePolygon writer = mesh.writer();

		writer.colorAll(0, 0xFFFFFFFF)
		.surface(SURFACE_ENDS)
		.lockUV(0, true)
		.rotation(0, TextureOrientation.IDENTITY)
		.sprite(0, "")
		.saveDefaults();


		writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, top ? 0.75f : 0, Direction.NORTH);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, top ? 0 : 0.75f, Direction.NORTH);
		pt.accept(writer);
		writer.append();

		final float min = top ? .75f : 0;
		final float max = min + 0.25f;

		writer.setupFaceQuad(Direction.EAST, 0, min, 1, max, 0, Direction.UP);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.WEST, 0, min, 1, max, 0, Direction.UP);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.NORTH, 0, min, 1, max, 0, Direction.UP);
		pt.accept(writer);
		writer.append();

		writer.setupFaceQuad(Direction.SOUTH, 0, min, 1, max, 0, Direction.UP);
		pt.accept(writer);
		writer.append();

	}
	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
			.surfaceList(SURFACES)
			.polyFactory( CappedSquareInsetColumn::factory)
			.axisJoin(true)
			.orientationType(OrientationType.AXIS)
			.build(Xm.id("capped_square_inset_column"));
}
