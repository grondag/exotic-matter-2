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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.generator.StairMesh;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.primitive.base.AbstractWedge;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@Experimental
public class Stair extends AbstractWedge {
	public static final XmSurfaceList SURFACES = CubeWithRotation.SURFACES;

	public static final XmSurface SURFACE_DOWN = CubeWithRotation.SURFACE_DOWN;
	public static final XmSurface SURFACE_UP = CubeWithRotation.SURFACE_UP;
	public static final XmSurface SURFACE_NORTH = CubeWithRotation.SURFACE_NORTH;
	public static final XmSurface SURFACE_SOUTH = CubeWithRotation.SURFACE_SOUTH;
	public static final XmSurface SURFACE_WEST = CubeWithRotation.SURFACE_WEST;
	public static final XmSurface SURFACE_EAST = CubeWithRotation.SURFACE_EAST;

	public static final XmSurface SURFACE_BOTTOM = CubeWithRotation.SURFACE_BOTTOM;
	public static final XmSurface SURFACE_TOP = CubeWithRotation.SURFACE_TOP;
	public static final XmSurface SURFACE_BACK = CubeWithRotation.SURFACE_BACK;
	public static final XmSurface SURFACE_FRONT = CubeWithRotation.SURFACE_FRONT;
	public static final XmSurface SURFACE_LEFT = CubeWithRotation.SURFACE_LEFT;
	public static final XmSurface SURFACE_RIGHT = CubeWithRotation.SURFACE_RIGHT;

	public static final Stair INSTANCE = new Stair(Xm.id("stair"));

	protected Stair(ResourceLocation id) {
		super(id, s -> SURFACES);
	}

	@Override
	protected ReadOnlyMesh buildMesh(PolyTransform transform, boolean isCorner, boolean isInside) {
		final WritableMesh mesh = XmMeshes.claimWritable();
		StairMesh.build(mesh, transform, isCorner, isInside, SURFACE_BOTTOM, SURFACE_TOP, SURFACE_FRONT, SURFACE_BACK, SURFACE_LEFT, SURFACE_RIGHT);
		return mesh.releaseToReader();
	}
}
