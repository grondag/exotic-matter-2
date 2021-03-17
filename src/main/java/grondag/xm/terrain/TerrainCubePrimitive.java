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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.terrain.TerrainModelState;

@Internal
public class TerrainCubePrimitive extends AbstractTerrainPrimitive {
	public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("all", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS).build();

	public static final XmSurface SURFACE_ALL = SURFACES.get(0);

	public static final TerrainCubePrimitive INSTANCE = new TerrainCubePrimitive(Xm.id("terrain_cube"));

	/** never changes so may as well save it */
	private final XmMesh cachedQuads;

	protected TerrainCubePrimitive(Identifier id) {
		super(id, NONE, TerrainModelStateImpl.FACTORY, s -> SURFACES);
		cachedQuads = getCubeQuads();
	}

	@Override
	public void emitQuads(TerrainModelState modelState, Consumer<Polygon> target) {
		cachedQuads.forEach(target);
	}

	private XmMesh getCubeQuads() {
		final WritableMesh stream = XmMeshes.claimWritable();
		appendCubeFace(stream, Direction.DOWN);
		appendCubeFace(stream, Direction.UP);
		appendCubeFace(stream, Direction.EAST);
		appendCubeFace(stream, Direction.WEST);
		appendCubeFace(stream, Direction.NORTH);
		appendCubeFace(stream, Direction.SOUTH);

		final XmMesh result = stream.releaseToReader();

		result.reader().origin();
		assert result.reader().vertexCount() == 4;

		return result;
	}

	private void appendCubeFace(WritableMesh stream, Direction side) {
		final MutablePolygon q = stream.writer();

		q.lockUV(0, true);
		q.surface(SURFACE_ALL);
		q.nominalFace(side);

		switch (side) {
		case UP:
			q.vertex(0, 0, 1, 0, 0, 0, 0xFFFFFFFF);
			q.vertex(1, 0, 1, 1, 0, 1, 0xFFFFFFFF);
			q.vertex(2, 1, 1, 1, 1, 1, 0xFFFFFFFF);
			q.vertex(3, 1, 1, 0, 1, 0, 0xFFFFFFFF);
			break;

		case DOWN:
			q.vertex(0, 0, 0, 1, 1, 1, 0xFFFFFFFF);
			q.vertex(1, 0, 0, 0, 1, 0, 0xFFFFFFFF);
			q.vertex(2, 1, 0, 0, 0, 0, 0xFFFFFFFF);
			q.vertex(3, 1, 0, 1, 0, 1, 0xFFFFFFFF);
			break;

		case WEST:
			q.vertex(0, 0, 1, 0, 0, 0, 0xFFFFFFFF);
			q.vertex(1, 0, 0, 0, 0, 1, 0xFFFFFFFF);
			q.vertex(2, 0, 0, 1, 1, 1, 0xFFFFFFFF);
			q.vertex(3, 0, 1, 1, 1, 0, 0xFFFFFFFF);
			break;

		case EAST:
			q.vertex(0, 1, 1, 1, 0, 0, 0xFFFFFFFF);
			q.vertex(1, 1, 0, 1, 0, 1, 0xFFFFFFFF);
			q.vertex(2, 1, 0, 0, 1, 1, 0xFFFFFFFF);
			q.vertex(3, 1, 1, 0, 1, 0, 0xFFFFFFFF);
			break;

		case NORTH:
			q.vertex(0, 1, 1, 0, 0, 0, 0xFFFFFFFF);
			q.vertex(1, 1, 0, 0, 0, 1, 0xFFFFFFFF);
			q.vertex(2, 0, 0, 0, 1, 1, 0xFFFFFFFF);
			q.vertex(3, 0, 1, 0, 1, 0, 0xFFFFFFFF);
			break;

		case SOUTH:
			q.vertex(0, 0, 1, 1, 0, 0, 0xFFFFFFFF);
			q.vertex(1, 0, 0, 1, 0, 1, 0xFFFFFFFF);
			q.vertex(2, 1, 0, 1, 1, 1, 0xFFFFFFFF);
			q.vertex(3, 1, 1, 1, 1, 0, 0xFFFFFFFF);
			break;
		}

		q.append();
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
