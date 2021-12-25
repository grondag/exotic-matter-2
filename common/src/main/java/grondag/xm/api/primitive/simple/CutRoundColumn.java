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
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.orientation.api.OrientationType;

@Experimental
public class CutRoundColumn {
	private CutRoundColumn() { }

	private static final float INNER_DIAMETER = 0.75f;
	private static final float INNER_RADIUS = INNER_DIAMETER / 2f;
	private static final float INNER_RADIUS_SQUARED = INNER_RADIUS * INNER_RADIUS;

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
		.add("ends", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
		.add("outer", SurfaceTopology.TILED, XmSurface.FLAG_NONE)
		.add("cut", SurfaceTopology.TILED, XmSurface.FLAG_LAMP_GRADIENT)
		.add("inner", SurfaceTopology.TILED, XmSurface.FLAG_LAMP)
		.build();

	public static final XmSurface SURFACE_ENDS = SURFACES.get(0);
	public static final XmSurface SURFACE_OUTER = SURFACES.get(1);
	public static final XmSurface SURFACE_CUT = SURFACES.get(2);
	public static final XmSurface SURFACE_INNER = SURFACES.get(3);

	private static Axis[] AXES = Axis.values();

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final PolyTransform pt = PolyTransform.get(modelState);

		final CsgMeshBuilder csg = CsgMeshBuilder.threadLocal();
		final SimpleJoinState state = modelState.simpleJoin();
		final boolean isLit = modelState.primitive().lampSurface(modelState) != null;
		final Axis axis = AXES[modelState.orientationIndex()];
		final Direction up = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		final Direction down = Direction.fromAxisAndDirection(axis, AxisDirection.NEGATIVE);

		// FIXME: not a big problem (yet) but should not cull ends when producing collision model
		final boolean cullUpper = state.isJoined(up);
		final boolean cullLower = state.isJoined(down);

		final MutablePolygon writer = csg.input().writer();
		emitOuterSection(writer, pt, 0.25f, 0, SURFACE_CUT, SURFACE_ENDS);
		emitOuterSection(writer, pt, 0.25f, 0.75f, SURFACE_ENDS, SURFACE_CUT);
		csg.union();

		emitCenterSection(csg.input(), pt);
		csg.union();

		final MutableMesh mesh = csg.buildMutable();

		if (isLit || cullUpper || cullLower) {
			final MutablePolygon editor = mesh.editor();

			if (editor.origin()) {
				do {
					final XmSurface surface = editor.surface();

					if (surface == SURFACE_CUT) {
						//apply glow

						// we want inner vertices to have glow
						// this is one way to find them...
						editor.assignLockedUVCoordinates(0);

						for (int i = 0; i < 4; i++) {
							final float u = editor.u(i, 0) - 0.5f; // move to origin
							final float v = editor.v(i, 0) - 0.5f;
							final int glow = u * u + v * v > (INNER_RADIUS_SQUARED + PolyHelper.EPSILON) ? 255 / 3 : 255;
							editor.glow(i, glow);
						}
					} else if (surface == SURFACE_ENDS) {
						final Direction cullFace = editor.computeCullFace();

						// Remove occluded end faces
						if ((cullUpper && cullFace == up) || (cullLower && cullFace == down)) {
							editor.delete();
						}
					}
				} while (editor.next());
			}
		}

		return mesh.releaseToReader();
	};

	private static void emitCenterSection(WritableMesh mesh, PolyTransform pt) {
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

		MeshHelper.unitCylinder(mesh.writer(), 16, transform, SURFACE_INNER, SURFACE_INNER, SURFACE_INNER, 2);
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
		.build(Xm.id("cut_round_column"));
}
