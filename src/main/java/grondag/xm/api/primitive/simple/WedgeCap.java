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

import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;
import net.minecraft.util.math.Direction;

public class WedgeCap  {
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
			.build(Xm.idString("wedge_cap"));

}
