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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.Xm;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.generator.StairMesh;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.primitive.base.AbstractWedge;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@API(status = EXPERIMENTAL)
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

    public static final Stair INSTANCE = new Stair(Xm.idString("stair"));

    protected Stair(String idString) {
        super(idString, s -> SURFACES);
    }

    @Override
    protected ReadOnlyMesh buildMesh(PolyTransform transform, boolean isCorner, boolean isInside) {
        final WritableMesh mesh = XmMeshes.claimWritable();
        StairMesh.build(mesh, transform, isCorner, isInside, SURFACE_BOTTOM, SURFACE_TOP, SURFACE_FRONT, SURFACE_BACK, SURFACE_LEFT, SURFACE_RIGHT);
        return mesh.releaseToReader();
    }
}
