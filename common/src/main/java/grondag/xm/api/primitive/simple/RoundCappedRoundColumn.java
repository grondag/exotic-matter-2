/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.api.primitive.simple;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;

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
import grondag.xm.api.primitive.surface.SurfaceLocation;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.orientation.api.OrientationType;

@Experimental
public class RoundCappedRoundColumn {
	private RoundCappedRoundColumn() { }

	private static final float INNER_DIAMETER = 0.75f;

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
		.add("ends", SurfaceTopology.CUBIC, SurfaceLocation.ENDS)
		.add("outer", SurfaceTopology.TILED, SurfaceLocation.OUTSIDE)
		.add("cut", SurfaceTopology.TILED, SurfaceLocation.CUT, XmSurface.FLAG_LAMP_GRADIENT)
		.add("inner", SurfaceTopology.TILED, SurfaceLocation.INSIDE, XmSurface.FLAG_LAMP)
		.build();

	@Deprecated public static final XmSurface SURFACE_ENDS = SURFACES.get(0);
	@Deprecated public static final XmSurface SURFACE_OUTER = SURFACES.get(1);
	@Deprecated public static final XmSurface SURFACE_CUT = SURFACES.get(2);
	@Deprecated public static final XmSurface SURFACE_INNER = SURFACES.get(3);

	private static Axis[] AXES = Axis.values();

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final PolyTransform pt = PolyTransform.get(modelState);

		final CsgMeshBuilder csg = CsgMeshBuilder.threadLocal();
		final SimpleJoinState state = modelState.simpleJoin();

		boolean hasCap = false;

		final Axis axis = AXES[modelState.orientationIndex()];

		if (!state.isJoined(Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE))) {
			emitOuterSection(csg.input().writer(), pt, 0.25f, axis == Axis.X ? 0 : 0.75f, SURFACE_ENDS, SURFACE_CUT);
			//emitCapSection(csg.input(), pt, axis != Axis.X);
			csg.union();
			hasCap = true;
		}

		if (!state.isJoined(Direction.fromAxisAndDirection(axis, AxisDirection.NEGATIVE))) {
			emitOuterSection(csg.input().writer(), pt, 0.25f, axis == Axis.X ? 0.75f : 0, SURFACE_CUT, SURFACE_ENDS);
			//			emitCapSection(csg.input(), pt, axis == Axis.X);
			csg.union();
			hasCap = true;
		}

		emitCenterSection(csg.input(), pt, hasCap);
		//		emitRoundSection(csg.input(), pt, hasCap);
		csg.union();

		return csg.build();
	};

	private static void emitCenterSection(WritableMesh mesh, PolyTransform pt, boolean incudeCaps) {
		final MutablePolygon writer = mesh.writer();
		final Consumer<MutablePolygon> transform = p -> {
			p.scaleFromBlockCenter(INNER_DIAMETER, 1, INNER_DIAMETER).apply(pt);
		};

		writer
			.colorAll(0, 0xFFFFFFFF)
			.surface(SURFACE_INNER)
			.lockUV(0, false)
			.rotation(0, TextureOrientation.IDENTITY)
			.sprite(0, "")
			.saveDefaults();

		final XmSurface surface = incudeCaps ? SURFACE_INNER : null;

		MeshHelper.unitCylinder(mesh.writer(), 16, transform, SURFACE_INNER, surface, surface, 2);
	}

	private static void emitOuterSection(MutablePolygon writer, PolyTransform pt, float height, float bottom, XmSurface topSurface, XmSurface bottomSurface) {
		writer
			.colorAll(0, 0xFFFFFFFF)
			.surface(SURFACE_CUT)
			.lockUV(0, false)
			.rotation(0, TextureOrientation.IDENTITY)
			.sprite(0, "")
			.saveDefaults();

		MeshHelper.unitCylinder(writer, 16, pt, SURFACE_OUTER, topSurface, bottom == 0, bottomSurface, bottom != 0, 3, bottom, bottom + height);
	}

	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
		.surfaceList(SURFACES)
		.polyFactory(POLY_FACTORY)
		.axisJoin(true)
		.orientationType(OrientationType.AXIS)
		.build(Xm.id("round_capped_round_column"));
}
