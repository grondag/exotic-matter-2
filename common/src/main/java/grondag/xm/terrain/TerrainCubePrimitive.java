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

package grondag.xm.terrain;

import static grondag.xm.api.modelstate.ModelStateFlags.NONE;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

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

	/** Never changes so may as well save it. */
	private final XmMesh cachedQuads;

	protected TerrainCubePrimitive(ResourceLocation id) {
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
