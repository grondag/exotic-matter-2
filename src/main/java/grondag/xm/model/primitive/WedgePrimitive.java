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

package grondag.xm.model.primitive;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.helper.FaceVertex;
import grondag.xm.mesh.helper.PolyTransform;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.mesh.stream.ReadOnlyPolyStream;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.painting.SurfaceTopology;
import grondag.xm.surface.XmSurfaceImpl;
import grondag.xm.surface.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Direction;

public class WedgePrimitive extends AbstractWedgePrimitive {
    private static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
            .add("back", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

    public static final XmSurfaceImpl SURFACE_BACK = SURFACES.get(0);
    public static final XmSurfaceImpl SURFACE_BOTTOM = SURFACES.get(1);
    public static final XmSurfaceImpl SURFACE_TOP = SURFACES.get(2);
    public static final XmSurfaceImpl SURFACE_SIDES = SURFACES.get(3);
    
    public WedgePrimitive(String idString) {
        super(idString);
    }
    
    @Override
    public XmSurfaceListImpl surfaces(SimpleModelState modelState) {
        return SURFACES;
    }

    @Override
    protected ReadOnlyPolyStream buildPolyStream(int edgeIndex, boolean isCorner, boolean isInside) {
        // Default geometry bottom/back against down/south faces. Corner is on right.
      
        final WritablePolyStream stream = PolyStreams.claimWritable();
        final MutablePolygon quad = stream.writer();
        final PolyTransform transform = PolyTransform.edgeSidedTransform(edgeIndex);
        
        quad.rotation(0, Rotation.ROTATE_NONE);
        quad.lockUV(0, true);
        stream.saveDefaults();

        // bottom is always the same
        quad.surface(SURFACE_BOTTOM);
        quad.nominalFace(Direction.DOWN);
        quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
        //TODO: remove
        quad.colorAll(0, 0xFF303030);
        transform.apply(quad);
        stream.append();
        
        // back is full except for outside corners
        quad.surface(SURFACE_BACK);
        quad.nominalFace(Direction.SOUTH);

        if(isCorner && !isInside) {
            quad.setupFaceQuad(Direction.SOUTH, 
                    new FaceVertex(0, 0, 0), 
                    new FaceVertex(1, 0, 0), 
                    new FaceVertex(0.5f, 0.5f, 0),
                    new FaceVertex(0, 1, 0),
                    Direction.UP);
            quad.assignLockedUVCoordinates(0);
        } else {
            quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        }
        //TODO: remove
        quad.colorAll(0, 0xFFFF3030);
        transform.apply(quad);
        stream.append();
        
        if(!isCorner || isInside) {
            quad.surface(SURFACE_SIDES);
            quad.nominalFace(Direction.EAST);
            quad.setupFaceQuad(Direction.EAST, 
                    new FaceVertex(0, 0, 0), 
                    new FaceVertex(1, 0, 0), 
                    new FaceVertex(0.5f, 0.5f, 0),
                    new FaceVertex(0, 1, 0),
                    Direction.UP);
            quad.assignLockedUVCoordinates(0);
            transform.apply(quad);
            stream.append();
        }
//        } else {
//            quad.setupFaceQuad(Direction.EAST, 
//                    new FaceVertex(0, 0, 0), 
//                    new FaceVertex(1, 0, 0), 
//                    new FaceVertex(0.5f, 0.5f, 0.5f),
//                    new FaceVertex(0, 1, 1.0f),
//                    Direction.UP);
//        }
        
        quad.surface(SURFACE_SIDES);
        quad.nominalFace(Direction.WEST);
        if(!isCorner || !isInside) {
            quad.setupFaceQuad(Direction.WEST, 
                    new FaceVertex(0, 0, 0), 
                    new FaceVertex(1, 0, 0), 
                    new FaceVertex(1, 1, 0),
                    new FaceVertex(0.5f, 0.5f, 0),
                    Direction.UP);

        } else {
            quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        }
        quad.assignLockedUVCoordinates(0);
        transform.apply(quad);
        stream.append();
        
        // front/top
        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_TOP);
                quad.nominalFace(Direction.UP);
                quad.setupFaceQuad(Direction.UP, 
                        new FaceVertex(0, 0, 1),
                        new FaceVertex(0.5f, 0.5f, 0.5f),
                        new FaceVertex(1, 1, 0),
                        new FaceVertex(0, 1, 0),
                        Direction.SOUTH);
                quad.assignLockedUVCoordinates(0);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_TOP);
                quad.nominalFace(Direction.UP);
                quad.setupFaceQuad(Direction.UP, 
                        new FaceVertex(0, 0, 1),
                        new FaceVertex(1, 0, 0),
                        new FaceVertex(1, 1, 0),
                        new FaceVertex(0.5f, 0.5f, 0.5f),
                        Direction.SOUTH);
                quad.assignLockedUVCoordinates(0);
                transform.apply(quad);
                stream.append();
                
                // inside has an extra side face on the front
                quad.surface(SURFACE_SIDES);
                quad.nominalFace(Direction.NORTH);
                quad.setupFaceQuad(Direction.NORTH, 
                        new FaceVertex(0, 0, 0),
                        new FaceVertex(1, 0, 0),
                        new FaceVertex(1, 1, 0),
                        new FaceVertex(0.5f, 0.5f, 0),
                        Direction.UP);
                quad.assignLockedUVCoordinates(0);
                transform.apply(quad);
                stream.append();
                
            } else {
                // outside
                quad.surface(SURFACE_TOP);
                quad.nominalFace(Direction.UP);
                quad.setupFaceQuad(Direction.UP, 
                        new FaceVertex(0, 0, 1),
                        new FaceVertex(0.5f, 0.5f, 0.5f),
                        new FaceVertex(1, 1, 0),
                        new FaceVertex(0, 1, 1),
                        Direction.SOUTH);
                quad.assignLockedUVCoordinates(0);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_TOP);
                quad.nominalFace(Direction.UP);
                quad.setupFaceQuad(Direction.UP, 
                        new FaceVertex(0, 0, 1),
                        new FaceVertex(1, 0, 1),
                        new FaceVertex(1, 1, 0),
                        new FaceVertex(0.5f, 0.5f, 0.5f),
                        Direction.SOUTH);
                quad.assignLockedUVCoordinates(0);
                transform.apply(quad);
                stream.append();
            }
        } else {
            quad.surface(SURFACE_TOP);
            quad.nominalFace(Direction.UP);
            quad.setupFaceQuad(Direction.UP, 
                    new FaceVertex(0, 0, 1),
                    new FaceVertex(1, 0, 1),
                    new FaceVertex(1, 1, 0),
                    new FaceVertex(0, 1, 0),
                    Direction.SOUTH);
            quad.assignLockedUVCoordinates(0);
            transform.apply(quad);
            stream.append();
        }

        return stream.releaseAndConvertToReader();
    }
}
