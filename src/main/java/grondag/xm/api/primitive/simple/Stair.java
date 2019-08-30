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

import grondag.fermion.spatial.Rotation;
import grondag.xm.Xm;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.primitive.base.AbstractWedge;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import net.minecraft.util.math.Direction;

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
    protected ReadOnlyMesh buildPolyStream(int edgeIndex, boolean isCorner, boolean isInside) {
        // Default geometry bottom/back against down/south faces. Corner is on right.

        // Sides are split into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.
        
        final WritableMesh stream = XmMeshes.claimWritable();
        final MutablePolygon quad = stream.writer();
        final PolyTransform transform = PolyTransform.forEdgeRotation(edgeIndex);
        
        quad.rotation(0, Rotation.ROTATE_NONE);
        quad.lockUV(0, true);
        quad.saveDefaults();

        // bottom is always the same
        quad.surface(SURFACE_BOTTOM);
        quad.nominalFace(Direction.DOWN);
        quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
        transform.accept(quad);
        quad.append();

        
        // back is full except for outside corners
        if(isCorner && !isInside) {
            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        } else {
            quad.surface(SURFACE_BACK);
            quad.nominalFace(Direction.SOUTH);
            quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
            transform.accept(quad);
            quad.append();
        }

        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();
                
                // Extra, inset top quadrant on inside corner
                
                // make cuts appear different from top/front face
                quad.textureSalt(1); 
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();
                
            } else {
                // Left side top quadrant is inset on an outside corner
                quad.textureSalt(1); 
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();
            }

        } else {
            quad.surface(SURFACE_LEFT);
            quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        }
        
        quad.surface(SURFACE_LEFT);
        quad.setupFaceQuad(Direction.EAST, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
        transform.accept(quad);
        quad.append();

        quad.surface(SURFACE_LEFT);
        quad.setupFaceQuad(Direction.EAST, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
        transform.accept(quad);
        quad.append();

        
        // right side is a full face on an inside corner
        if(isCorner && isInside) {
            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        } else {
            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
        }

        // front 
        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();
                
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();
                
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();
                
                quad.textureSalt(1); 
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();
            } else {
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
                transform.accept(quad);
                quad.append();
                
                quad.textureSalt(1); 
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
                transform.accept(quad);
                quad.append();
            }

        } else {
            quad.surface(SURFACE_FRONT);
            quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
            transform.accept(quad);
            quad.append();
            
            quad.textureSalt(1);
            quad.surface(SURFACE_FRONT);
            quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
            transform.accept(quad);
            quad.append();
        }

        // top 
        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
                
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
                
                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
            } else {
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();

                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
                
                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
                
                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.5f, 0.0f, 1.0f, 0.5f, 0.5f, Direction.SOUTH);
                transform.accept(quad);
                quad.append();
            }
        } else {
            quad.surface(SURFACE_TOP);
            quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
            transform.accept(quad);
            quad.append();

            quad.surface(SURFACE_TOP);
            quad.textureSalt(1);
            quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f, Direction.SOUTH);
            transform.accept(quad);
            quad.append();
        }
        
        return stream.releaseToReader();
    }
}
