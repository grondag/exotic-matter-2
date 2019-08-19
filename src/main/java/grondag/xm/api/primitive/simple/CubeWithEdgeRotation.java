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

import grondag.fermion.spatial.Rotation;
import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import net.minecraft.util.math.Direction;

public class CubeWithEdgeRotation {
    private CubeWithEdgeRotation() {}
    
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
            .add("down", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("up", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("north", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("south", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("west", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("east", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .build();

    public static final XmSurface SURFACE_DOWN = SURFACES.get(0);
    public static final XmSurface SURFACE_UP = SURFACES.get(1);
    public static final XmSurface SURFACE_NORTH = SURFACES.get(2);
    public static final XmSurface SURFACE_SOUTH = SURFACES.get(3);
    public static final XmSurface SURFACE_WEST = SURFACES.get(4);
    public static final XmSurface SURFACE_EAST = SURFACES.get(5);
    
    public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
    public static final XmSurface SURFACE_TOP = SURFACES.get(1);
    public static final XmSurface SURFACE_FRONT = SURFACES.get(2);
    public static final XmSurface SURFACE_BACK = SURFACES.get(3);
    public static final XmSurface SURFACE_LEFT = SURFACES.get(4);
    public static final XmSurface SURFACE_RIGHT = SURFACES.get(5);

    static final Function<PolyTransform, XmMesh> POLY_FACTORY = transform -> {
        WritableMesh mesh = XmMeshes.claimWritable();
        MutablePolygon writer = mesh.writer();
        writer.colorAll(0, 0xFFFFFFFF);
        writer.lockUV(0, true);
        writer.rotation(0, Rotation.ROTATE_NONE);
        writer.sprite(0, "");
        mesh.saveDefaults();

        writer.surface(SURFACE_DOWN);
        writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        mesh.append();
        
        writer.surface(SURFACE_UP);
        writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        mesh.append();
        
        writer.surface(SURFACE_EAST);
        writer.setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        mesh.append();
        
        writer.surface(SURFACE_WEST);
        writer.setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        mesh.append();
        
        writer.surface(SURFACE_NORTH);
        writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        mesh.append();
        
        writer.surface(SURFACE_SOUTH);
        writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        mesh.append();

        return mesh.releaseToReader();
    };

    public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
            .surfaceList(SURFACES)
            .polyFactory(POLY_FACTORY)
            .orientationType(OrientationType.ROTATION)
            .build(Xm.idString("cube_with_rotation"));
}
