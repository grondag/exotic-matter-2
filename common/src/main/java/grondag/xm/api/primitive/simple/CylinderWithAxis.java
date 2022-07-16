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

import grondag.xm.Xm;
import grondag.xm.api.mesh.MeshHelper;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
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
public class CylinderWithAxis {
	private CylinderWithAxis() { }

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("ends", SurfaceTopology.CUBIC, SurfaceLocation.ENDS)
			.add("sides", SurfaceTopology.TILED, SurfaceLocation.SIDES)
			.build();

	@Deprecated public static final XmSurface SURFACE_ENDS = SURFACES.get(0);
	@Deprecated public static final XmSurface SURFACE_SIDES = SURFACES.get(1);

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final PolyTransform pt = PolyTransform.get(modelState);

		final WritableMesh mesh = XmMeshes.claimWritable();
		final MutablePolygon writer = mesh.writer();
		writer.colorAll(0, 0xFFFFFFFF);
		writer.lockUV(0, false);
		writer.rotation(0, TextureOrientation.IDENTITY);
		writer.sprite(0, "");
		writer.saveDefaults();

		final Consumer<MutablePolygon> transform = p -> {
			p.nominalFace(p.lightFace()).apply(pt);
		};

		MeshHelper.unitCylinder(mesh.writer(), 16, transform, SURFACE_SIDES, SURFACE_ENDS, SURFACE_ENDS, 3);

		return mesh.releaseToReader();
	};

	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
		.surfaceList(SURFACES)
		.polyFactory(POLY_FACTORY)
		.orientationType(OrientationType.AXIS)
		.build(Xm.id("cylinder_axis"));
}
