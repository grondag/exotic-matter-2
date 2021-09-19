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
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.Xm;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.CsgMeshBuilder;
import grondag.xm.api.mesh.MeshHelper;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;

@Experimental
public class CappedRoundColumn  {
	private CappedRoundColumn() {}

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("ends", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("sides", SurfaceTopology.TILED, XmSurface.FLAG_NONE)
			.build();

	public static final XmSurface SURFACE_ENDS = SURFACES.get(0);
	public static final XmSurface SURFACE_SIDES = SURFACES.get(1);

	private static Axis[] AXES = Axis.values();

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final PolyTransform pt = PolyTransform.get(modelState);

		final CsgMeshBuilder csg = CsgMeshBuilder.threadLocal();
		final SimpleJoinState state = modelState.simpleJoin();

		boolean hasCap = false;

		final Axis axis = AXES[modelState.orientationIndex()];
		if (!state.isJoined(Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE))) {
			emitCapSection(csg.input(), pt, axis != Axis.X);
			csg.union();
			hasCap = true;
		}

		if (!state.isJoined(Direction.fromAxisAndDirection(axis, AxisDirection.NEGATIVE))) {
			emitCapSection(csg.input(), pt, axis == Axis.X);
			csg.union();
			hasCap = true;
		}

		emitRoundSection(csg.input(), pt, hasCap);
		csg.union();

		return csg.build();
	};

	/**
	 *
	 * @param mesh
	 * @param pt
	 * @param incudeCaps  True if either cap is included so that CSG operation is supported. Otherwise cull.
	 */
	private static final void emitRoundSection(WritableMesh mesh, PolyTransform pt, boolean incudeCaps) {
		final MutablePolygon writer = mesh.writer();
		final Consumer<MutablePolygon> transform = p -> {
			p.scaleFromBlockCenter(0.75f, 1, 0.75f).apply(pt);
		};
		writer.colorAll(0, 0xFFFFFFFF)
		.surface(SURFACE_SIDES)
		.lockUV(0, false)
		.rotation(0, TextureOrientation.IDENTITY)
		.sprite(0, "")
		.saveDefaults();

		final XmSurface surface = incudeCaps ? SURFACE_ENDS : null;

		MeshHelper.unitCylinder(mesh.writer(), 16, transform, SURFACE_SIDES, surface, surface, 3);
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
			.polyFactory(POLY_FACTORY)
			.axisJoin(true)
			.orientationType(OrientationType.AXIS)
			.build(Xm.id("capped_round_column"));
}
