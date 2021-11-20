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

import java.util.function.Function;

import net.minecraft.core.Direction;

import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;

public class WedgeCap {
	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.build();

	public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
	public static final XmSurface SURFACE_TOP = SURFACES.get(1);

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final PolyTransform transform = PolyTransform.get(modelState);

		final WritableMesh mesh = XmMeshes.claimWritable();
		final MutablePolygon writer = mesh.writer()
				.colorAll(0, 0xFFFFFFFF)
				.lockUV(0, true)
				.rotation(0, TextureOrientation.IDENTITY)
				.sprite(0, "")
				.saveDefaults();

		writer
			.surface(SURFACE_BOTTOM)
			.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH)
			.apply(transform)
			.append();

		writer
			.surface(SURFACE_TOP)
			.vertexCount(3)
			.pos(0, 0, 0, 0).color(0, 0, 0xFFFFFFFF)
			.pos(1, 0.5f, 0.5f, 0.5f).color(1, 0, 0xFFFFFFFF)
			.pos(2, 1, 0, 0).color(2, 0, 0xFFFFFFFF)
			.nominalFace(Direction.UP)
			.apply(transform)
			.append();

		writer
			.surface(SURFACE_TOP)
			.vertexCount(3)
			.pos(0, 1, 0, 0).color(0, 0, 0xFFFFFFFF)
			.pos(1, 0.5f, 0.5f, 0.5f).color(1, 0, 0xFFFFFFFF)
			.pos(2, 1, 0, 1).color(2, 0, 0xFFFFFFFF)
			.nominalFace(Direction.UP)
			.apply(transform)
			.append();

		writer
			.surface(SURFACE_TOP)
			.vertexCount(3)
			.pos(0, 1, 0, 1).color(0, 0, 0xFFFFFFFF)
			.pos(1, 0.5f, 0.5f, 0.5f).color(1, 0, 0xFFFFFFFF)
			.pos(2, 0, 0, 1).color(2, 0, 0xFFFFFFFF)
			.nominalFace(Direction.UP)
			.apply(transform)
			.append();

		writer
			.surface(SURFACE_TOP)
			.vertexCount(3)
			.pos(0, 0, 0, 1).color(0, 0, 0xFFFFFFFF)
			.pos(1, 0.5f, 0.5f, 0.5f).color(1, 0, 0xFFFFFFFF)
			.pos(2, 0, 0, 0).color(2, 0, 0xFFFFFFFF)
			.nominalFace(Direction.UP)
			.apply(transform)
			.append();

		return mesh.releaseToReader();
	};

	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
		.surfaceList(SURFACES)
		.polyFactory(POLY_FACTORY)
		.orientationType(OrientationType.FACE)
		.build(Xm.id("wedge_cap"));
}
