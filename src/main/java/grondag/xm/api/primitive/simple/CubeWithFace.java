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

import net.minecraft.util.math.Direction;

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

public class CubeWithFace  {
	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.build();

	public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
	public static final XmSurface SURFACE_SIDES = SURFACES.get(1);
	public static final XmSurface SURFACE_TOP = SURFACES.get(2);

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final PolyTransform transform = PolyTransform.get(modelState);

		final WritableMesh mesh = XmMeshes.claimWritable();
		final MutablePolygon writer = mesh.writer();
		writer.colorAll(0, 0xFFFFFFFF);
		writer.lockUV(0, true);
		writer.rotation(0, TextureOrientation.IDENTITY);
		writer.sprite(0, "");
		writer.saveDefaults();

		writer.surface(SURFACE_BOTTOM);
		writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_TOP);
		writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		return mesh.releaseToReader();
	};


	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
			.surfaceList(SURFACES)
			.polyFactory(POLY_FACTORY)
			.orientationType(OrientationType.FACE)
			.build(Xm.id("cube_face"));

}
