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

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.math.Direction;

import grondag.fermion.color.Color;
import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.Xm;
import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.CsgMeshBuilder;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.FaceVertex;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.base.ConnectedShapeHelper;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;

@Experimental
public class FlatPanel {
	private FlatPanel() {}

	private static final float DEPTH = 1f / 16f;
	private static final float INV_DEPTH = 1 - DEPTH;

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("outer", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("inner", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS | XmSurface.FLAG_LAMP)
			.build();

	public static final XmSurface SURFACE_OUTER = SURFACES.get(0);
	public static final XmSurface SURFACE_INNER = SURFACES.get(1);

	private static final float[][] SPECS = ConnectedShapeHelper.panelspec(1f / 8f);

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final CornerJoinState joins = modelState.cornerJoin();

		if(joins.simpleJoin() == SimpleJoinState.ALL_JOINS)
			return XmMesh.EMPTY;

		final CsgMeshBuilder csg = CsgMeshBuilder.threadLocal();

		final boolean isLit = modelState.primitive().lampSurface(modelState) != null;

		for (int i = 0; i < 6; i++) {
			final Direction face = Direction.byId(i);
			cutSide(face, csg, joins.faceState(face), isLit);
		}

		emitOuter(csg.input(), joins);
		csg.union();

		return csg.build();
	};

	private static void cutSide(Direction face, CsgMeshBuilder csg, CornerJoinFaceState faceJoin, boolean isLit) {

		final float[] spec = SPECS[faceJoin.ordinal()];

		if(spec == null) return;

		final Direction top = PolyHelper.defaultTopOf(face);
		final Direction opposite = face.getOpposite();

		final int limit = spec.length / 4;
		for(int i = 0; i < limit; i++) {
			final WritableMesh mesh = csg.input();
			final MutablePolygon writer = mesh.writer();

			writer.colorAll(0, 0xFFFFFFFF);
			writer.lockUV(0, true);
			writer.rotation(0, TextureOrientation.IDENTITY);
			writer.sprite(0, "");
			writer.saveDefaults();

			final int index = i * 4;
			final float x0 = spec[index];
			final float y0 = spec[index + 1];
			final float x1 = spec[index + 2];
			final float y1 = spec[index + 3];

			writer.surface(SURFACE_INNER);
			writer.setupFaceQuad(opposite, 1 - x1, y0, 1 - x0, y1, INV_DEPTH, top);
			writer.append();

			writer.surface(SURFACE_INNER);
			writer.setupFaceQuad(face, x0, y0, x1, y1, 0, top);
			writer.append();

			setupCutSideQuad(writer, x0, INV_DEPTH, x1, 1, y0, PolyHelper.bottomOf(face, top), face, isLit);
			setupCutSideQuad(writer, 1 - x1, INV_DEPTH, 1 - x0, 1, 1 - y1, top, face, isLit);

			setupCutSideQuad(writer, 1 - y1, INV_DEPTH, 1 - y0, 1, x0, PolyHelper.leftOf(face, top), face, isLit);
			setupCutSideQuad(writer, y0, INV_DEPTH, y1, 1, 1 - x1, PolyHelper.rightOf(face, top), face, isLit);
			csg.union();
		}
	}

	private static void setupCutSideQuad(MutablePolygon poly, float x0, float y0, float x1, float y1, float depth, Direction face, Direction topFace, boolean isLit) {

		poly.surface(SURFACE_INNER);

		poly.setupFaceQuad(face,
				new FaceVertex.Colored(x0, y0, depth, Color.WHITE, 0),
				new FaceVertex.Colored(x1, y0, depth, Color.WHITE, 0),
				new FaceVertex.Colored(x1, y1, depth, Color.WHITE, 0),
				new FaceVertex.Colored(x0, y1, depth, Color.WHITE, 0),
				topFace);

		poly.append();
	}

	private static final void emitOuter(WritableMesh mesh, CornerJoinState joins) {
		final MutablePolygon writer = mesh.writer();

		writer.colorAll(0, 0xFFFFFFFF);
		writer.lockUV(0, true);
		writer.rotation(0, TextureOrientation.IDENTITY);
		writer.sprite(0, "");
		writer.saveDefaults();

		final SimpleJoinState j = joins.simpleJoin();

		if (!j.isJoined(Direction.DOWN)) {
			writer.surface(SURFACE_OUTER);
			writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
			writer.append();
		}

		if (!j.isJoined(Direction.UP)) {
			writer.surface(SURFACE_OUTER);
			writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH);
			writer.append();
		}

		if (!j.isJoined(Direction.EAST)) {
			writer.surface(SURFACE_OUTER);
			writer.setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP);
			writer.append();
		}

		if (!j.isJoined(Direction.WEST)) {
			writer.surface(SURFACE_OUTER);
			writer.setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP);
			writer.append();
		}

		if (!j.isJoined(Direction.NORTH)) {
			writer.surface(SURFACE_OUTER);
			writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP);
			writer.append();
		}

		if (!j.isJoined(Direction.SOUTH)) {
			writer.surface(SURFACE_OUTER);
			writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP);
			writer.append();
		}
	}

	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
			.surfaceList(SURFACES)
			.cornerJoin(true)
			.polyFactory(POLY_FACTORY)
			.orientationType(OrientationType.NONE)
			.build(Xm.id("flat_panel"));
}