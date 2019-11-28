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
package grondag.xm.terrain;

import static grondag.xm.api.modelstate.ModelStateFlags.NONE;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import net.minecraft.util.math.Direction;

import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.terrain.TerrainModelState;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.relics.CubeInputs;

@API(status = INTERNAL)
public class TerrainCubePrimitive extends AbstractTerrainPrimitive {
	public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("all", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS).build();

	public static final XmSurface SURFACE_ALL = SURFACES.get(0);

	public static final TerrainCubePrimitive INSTANCE = new TerrainCubePrimitive(Xm.idString("terrain_cube"));

	/** never changes so may as well save it */
	private final XmMesh cachedQuads;

	protected TerrainCubePrimitive(String idString) {
		super(idString, NONE, TerrainModelStateImpl.FACTORY, s -> SURFACES);
		cachedQuads = getCubeQuads();
	}

	@Override
	public void emitQuads(TerrainModelState modelState, Consumer<Polygon> target) {
		cachedQuads.forEach(target);
	}

	private XmMesh getCubeQuads() {
		final CubeInputs cube = new CubeInputs();
		cube.color = 0xFFFFFFFF;
		cube.textureRotation = TextureOrientation.IDENTITY;
		cube.isFullBrightness = false;
		cube.u0 = 0;
		cube.v0 = 0;
		cube.u1 = 1;
		cube.v1 = 1;
		cube.isOverlay = false;
		cube.surface = SURFACE_ALL;

		final WritableMesh stream = XmMeshes.claimWritable();
		cube.appendFace(stream, Direction.DOWN);
		cube.appendFace(stream, Direction.UP);
		cube.appendFace(stream, Direction.EAST);
		cube.appendFace(stream, Direction.WEST);
		cube.appendFace(stream, Direction.NORTH);
		cube.appendFace(stream, Direction.SOUTH);

		final XmMesh result = stream.releaseToReader();

		result.reader().origin();
		assert result.reader().vertexCount() == 4;

		return result;
	}

	@Override
	public TerrainModelState.Mutable geometricState(TerrainModelState fromState) {
		return defaultState().mutableCopy();
	}

	@Override
	public boolean doesShapeMatch(TerrainModelState from, TerrainModelState to) {
		return from.primitive() == to.primitive();
	}
}
