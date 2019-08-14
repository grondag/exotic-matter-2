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

import java.util.Arrays;
import java.util.function.Consumer;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.connect.model.BlockEdgeSided;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.mesh.helper.PolyTransform;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.surface.XmSurfaceImpl;
import grondag.xm.surface.XmSurfaceImpl.XmSurfaceListImpl;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.mesh.stream.ReadOnlyPolyStream;
import net.minecraft.util.math.Direction;

public class StairPrimitive extends AbstractWedgePrimitive {
    public static final XmSurfaceListImpl SURFACES = CubePrimitive.SURFACES;

    public static final XmSurfaceImpl SURFACE_DOWN = CubePrimitive.SURFACE_DOWN;
    public static final XmSurfaceImpl SURFACE_UP = CubePrimitive.SURFACE_UP;
    public static final XmSurfaceImpl SURFACE_NORTH = CubePrimitive.SURFACE_NORTH;
    public static final XmSurfaceImpl SURFACE_SOUTH = CubePrimitive.SURFACE_SOUTH;
    public static final XmSurfaceImpl SURFACE_WEST = CubePrimitive.SURFACE_WEST;
    public static final XmSurfaceImpl SURFACE_EAST = CubePrimitive.SURFACE_EAST;
    
    public static final XmSurfaceImpl SURFACE_BOTTOM = CubePrimitive.SURFACE_BOTTOM;
    public static final XmSurfaceImpl SURFACE_TOP = CubePrimitive.SURFACE_TOP;
    public static final XmSurfaceImpl SURFACE_BACK = CubePrimitive.SURFACE_BACK;
    public static final XmSurfaceImpl SURFACE_FRONT = CubePrimitive.SURFACE_FRONT;
    public static final XmSurfaceImpl SURFACE_LEFT = CubePrimitive.SURFACE_LEFT;
    public static final XmSurfaceImpl SURFACE_RIGHT = CubePrimitive.SURFACE_RIGHT;
    
    private static final int KEY_COUNT = BlockEdgeSided.COUNT * 3;
    
    private final ReadOnlyPolyStream[] CACHE = new ReadOnlyPolyStream[KEY_COUNT];
    
    public StairPrimitive(String idString) {
        super(idString);
    }
    
    @Override
    public XmSurfaceListImpl surfaces(SimpleModelState modelState) {
        return SURFACES;
    }

    // mainly for run-time testing
    @Override
    public void invalidateCache() { 
        Arrays.fill(CACHE, null);
    }
    
    static int computeKey(int edgeIndex, boolean isCorner, boolean isInside) {
        return edgeIndex * 3 + (isCorner ? (isInside ? 1 : 2) : 0);
    }

    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        final int edgeIndex = modelState.orientationIndex();
        final boolean isCorner = isCorner(modelState);
        final boolean isInside = isInsideCorner(modelState);
        final int key = computeKey(edgeIndex, isCorner, isInside);
        
        ReadOnlyPolyStream stream = CACHE[key];
        if(stream == null) {
            stream = produceQuadsInner(edgeIndex, isCorner, isInside);
            CACHE[key] = stream;
        }
        
        if (stream.origin()) {
            Polygon reader = stream.reader();

            do
                target.accept(reader);
            while (stream.next());
        }
    }
    
    private static ReadOnlyPolyStream produceQuadsInner(int edgeIndex, boolean isCorner, boolean isInside) {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is X orthogonalAxis with full sides against north/down faces.

        // For corners the default orientation has the extra octant on the right (east)
        // side against the back (north) face. 
        
        // Sides are split into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.
        
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
        transform.apply(quad);
        stream.append();

        
        // back is full except for outside corners
        if(isCorner && !isInside) {
            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.0, 0.0, 0.5, 0.5, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();

            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.5, 0.0, 1.0, 0.5, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();

            quad.surface(SURFACE_BACK);
            quad.setupFaceQuad(Direction.SOUTH, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();
        } else {
            quad.surface(SURFACE_BACK);
            quad.nominalFace(Direction.SOUTH);
            quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
            transform.apply(quad);
            stream.append();
        }

        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.UP);
                transform.apply(quad);
                stream.append();
                
                // Extra, inset top quadrant on inside corner
                
                // make cuts appear different from top/front face
                quad.textureSalt(1); 
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.5, 0.5, 1.0, 1.0, 0.5, Direction.UP);
                transform.apply(quad);
                stream.append();
                
            } else {
                // Left side top quadrant is inset on an outside corner
                quad.textureSalt(1); 
                quad.surface(SURFACE_LEFT);
                quad.setupFaceQuad(Direction.EAST, 0.0, 0.5, 0.5, 1.0, 0.5, Direction.UP);
                transform.apply(quad);
                stream.append();
            }

        } else {
            quad.surface(SURFACE_LEFT);
            quad.setupFaceQuad(Direction.EAST, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();
        }
        
        quad.surface(SURFACE_LEFT);
        quad.setupFaceQuad(Direction.EAST, 0.0, 0.0, 0.5, 0.5, 0.0, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_LEFT);
        quad.setupFaceQuad(Direction.EAST, 0.5, 0.0, 1.0, 0.5, 0.0, Direction.UP);
        transform.apply(quad);
        stream.append();

        
        // right side is a full face on an inside corner
        if(isCorner && isInside) {
            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.0, 0.0, 1.0, 1.0, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();
        } else {
            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.0, 0.0, 0.5, 0.5, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();

            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.5, 0.0, 1.0, 0.5, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();

            quad.surface(SURFACE_RIGHT);
            quad.setupFaceQuad(Direction.WEST, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();
        }


        // front 
        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0, 0.0, 0.5, 0.5, 0.0, Direction.UP);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5, 0.0, 1.0, 0.5, 0.0, Direction.UP);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.UP);
                transform.apply(quad);
                stream.append();
                
                quad.textureSalt(1); 
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0, 0.5, 0.5, 1.0, 0.5, Direction.UP);
                transform.apply(quad);
                stream.append();
            } else {
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.0, 0.0, 1.0, 0.5, 0.0, Direction.UP);
                transform.apply(quad);
                stream.append();
                
                quad.textureSalt(1); 
                quad.surface(SURFACE_FRONT);
                quad.setupFaceQuad(Direction.NORTH, 0.5, 0.5, 1.0, 1.0, 0.5, Direction.UP);
                transform.apply(quad);
                stream.append();
            }

        } else {
            quad.surface(SURFACE_FRONT);
            quad.setupFaceQuad(Direction.NORTH, 0.0, 0.0, 1.0, 0.5, 0.0, Direction.UP);
            transform.apply(quad);
            stream.append();
            
            quad.textureSalt(1);
            quad.surface(SURFACE_FRONT);
            quad.setupFaceQuad(Direction.NORTH, 0.0, 0.5, 1.0, 1.0, 0.5, Direction.UP);
            transform.apply(quad);
            stream.append();
        }

        // top 
        if(isCorner) {
            if(isInside) {
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.SOUTH);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.SOUTH);
                transform.apply(quad);
                stream.append();

                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5, 0.0, 1.0, 0.5, 0.0, Direction.SOUTH);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0, 0.0, 0.5, 0.5, 0.5, Direction.SOUTH);
                transform.apply(quad);
                stream.append();
            } else {
                quad.surface(SURFACE_TOP);
                quad.setupFaceQuad(Direction.UP, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.SOUTH);
                transform.apply(quad);
                stream.append();

                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0, 0.0, 0.5, 0.5, 0.5, Direction.SOUTH);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.0, 0.5, 0.5, 1.0, 0.5, Direction.SOUTH);
                transform.apply(quad);
                stream.append();
                
                quad.surface(SURFACE_TOP);
                quad.textureSalt(1);
                quad.setupFaceQuad(Direction.UP, 0.5, 0.0, 1.0, 0.5, 0.5, Direction.SOUTH);
                transform.apply(quad);
                stream.append();
            }
        } else {
            quad.surface(SURFACE_TOP);
            quad.setupFaceQuad(Direction.UP, 0.0, 0.5, 1.0, 1.0, 0.0, Direction.SOUTH);
            transform.apply(quad);
            stream.append();

            quad.surface(SURFACE_TOP);
            quad.textureSalt(1);
            quad.setupFaceQuad(Direction.UP, 0.0, 0.0, 1.0, 0.5, 0.5, Direction.SOUTH);
            transform.apply(quad);
            stream.append();
        }
        return stream.releaseAndConvertToReader();
    }
}
