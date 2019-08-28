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
package grondag.xm.mesh.helper;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.orientation.CubeCorner;
import grondag.xm.api.orientation.CubeEdge;
import grondag.xm.api.orientation.CubeRotation;
import grondag.xm.api.orientation.HorizontalFace;
import grondag.xm.api.orientation.OrientationType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;

@API(status = INTERNAL)
@SuppressWarnings("rawtypes")
public class PolyTransformImpl implements PolyTransform {

    private final Matrix4f matrix;

    private PolyTransformImpl(Matrix4f matrix) {
        this.matrix = matrix;
    }

    @Override
    public void accept(MutablePolygon poly) {
        final Matrix4f matrix = this.matrix;
        final int vertexCount = poly.vertexCount();
        final Vector3f vec = VEC3.get();

        // transform vertices
        for (int i = 0; i < vertexCount; i++) {
            matrix.transformPosition(poly.x(i) - 0.5f, poly.y(i) - 0.5f, poly.z(i) - 0.5f, vec);
            poly.pos(i, vec.x + 0.5f, vec.y + 0.5f, vec.z + 0.5f);

            if (poly.hasNormal(i)) {
                matrix.transformDirection(poly.normalX(i), poly.normalY(i), poly.normalZ(i), vec);
                vec.normalize();
                poly.normal(i, vec.x, vec.y, vec.z);
            }
        }

        // transform nominal face
        Vec3i oldVec = poly.nominalFace().getVector();
        matrix.transformDirection(oldVec.getX(), oldVec.getY(), oldVec.getZ(), vec);
        poly.nominalFace(PolyHelper.faceForNormal(vec.x, vec.y, vec.z));
        final Direction cullFace = poly.cullFace();
        if(cullFace != null) {
            oldVec = cullFace.getVector();
            matrix.transformDirection(oldVec.getX(), oldVec.getY(), oldVec.getZ(), vec);
            poly.cullFace(PolyHelper.faceForNormal(vec.x, vec.y, vec.z));
        }
    }

    private static final ThreadLocal<Vector3f> VEC3 = ThreadLocal.withInitial(Vector3f::new);

    private final static PolyTransformImpl[][] LOOKUP = new PolyTransformImpl[OrientationType.values().length][];
    private final static PolyTransformImpl[] EXACT = new PolyTransformImpl[CubeRotation.COUNT];
    private final static PolyTransformImpl[] EDGE = new PolyTransformImpl[CubeEdge.COUNT];
    private final static PolyTransformImpl[] CORNER = new PolyTransformImpl[CubeCorner.COUNT];
    private final static PolyTransformImpl[] FACE = new PolyTransformImpl[6];
    private final static PolyTransformImpl[] HORIZONTAL_FACE = new PolyTransformImpl[HorizontalFace.COUNT];
    private final static PolyTransformImpl[] AXIS = new PolyTransformImpl[3];
    

    // mainly for run-time testing
    public static void invalidateCache() { 
        populateLookups();
    }
    
    static {
        LOOKUP[OrientationType.ROTATION.ordinal()] = EXACT;
        LOOKUP[OrientationType.EDGE.ordinal()] =  EDGE;
        LOOKUP[OrientationType.CORNER.ordinal()] =  CORNER;
        LOOKUP[OrientationType.FACE.ordinal()] =  FACE;
        LOOKUP[OrientationType.HORIZONTAL_FACE.ordinal()] = HORIZONTAL_FACE;
        LOOKUP[OrientationType.AXIS.ordinal()] = AXIS;
        LOOKUP[OrientationType.NONE.ordinal()] = new PolyTransformImpl[1];
        populateLookups();
    }
    
    private static void populateLookups() {
        CubeRotation.forEach(e -> {
            EXACT[e.ordinal()] = createEdgeTransform(e);
        });
        
        LOOKUP[OrientationType.NONE.ordinal()][0] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];

        AXIS[Axis.Y.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];
        AXIS[Axis.X.ordinal()] = EXACT[CubeRotation.EAST_UP.ordinal()];
        AXIS[Axis.Z.ordinal()] = EXACT[CubeRotation.NORTH_UP.ordinal()];
        
        HORIZONTAL_FACE[HorizontalFace.NORTH.ordinal()] = EXACT[CubeRotation.NORTH_EAST.ordinal()];
        HORIZONTAL_FACE[HorizontalFace.EAST.ordinal()] = EXACT[CubeRotation.EAST_SOUTH.ordinal()];
        HORIZONTAL_FACE[HorizontalFace.SOUTH.ordinal()] = EXACT[CubeRotation.SOUTH_WEST.ordinal()];
        HORIZONTAL_FACE[HorizontalFace.WEST.ordinal()] = EXACT[CubeRotation.WEST_SOUTH.ordinal()];
        
        FACE[Direction.DOWN.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];
        FACE[Direction.UP.ordinal()] = EXACT[CubeRotation.UP_SOUTH.ordinal()];
        FACE[Direction.NORTH.ordinal()] = EXACT[CubeRotation.NORTH_EAST.ordinal()];
        FACE[Direction.SOUTH.ordinal()] = EXACT[CubeRotation.SOUTH_WEST.ordinal()];
        FACE[Direction.EAST.ordinal()] = EXACT[CubeRotation.EAST_SOUTH.ordinal()];
        FACE[Direction.WEST.ordinal()] = EXACT[CubeRotation.WEST_SOUTH.ordinal()];
        
        CORNER[CubeCorner.UP_NORTH_EAST.ordinal()] = EXACT[CubeRotation.NORTH_UP.ordinal()];
        CORNER[CubeCorner.UP_NORTH_WEST.ordinal()] = EXACT[CubeRotation.UP_NORTH.ordinal()];
        CORNER[CubeCorner.UP_SOUTH_EAST.ordinal()] = EXACT[CubeRotation.UP_SOUTH.ordinal()];
        CORNER[CubeCorner.UP_SOUTH_WEST.ordinal()] = EXACT[CubeRotation.SOUTH_UP.ordinal()];
        CORNER[CubeCorner.DOWN_NORTH_EAST.ordinal()] = EXACT[CubeRotation.DOWN_NORTH.ordinal()];
        CORNER[CubeCorner.DOWN_NORTH_WEST.ordinal()] = EXACT[CubeRotation.NORTH_DOWN.ordinal()];
        CORNER[CubeCorner.DOWN_SOUTH_EAST.ordinal()] = EXACT[CubeRotation.DOWN_EAST.ordinal()];
        CORNER[CubeCorner.DOWN_SOUTH_WEST.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];
        
        EDGE[CubeEdge.DOWN_SOUTH.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];
        EDGE[CubeEdge.DOWN_WEST.ordinal()] = EXACT[CubeRotation.DOWN_WEST.ordinal()];
        EDGE[CubeEdge.DOWN_NORTH.ordinal()] = EXACT[CubeRotation.DOWN_NORTH.ordinal()];
        EDGE[CubeEdge.DOWN_EAST.ordinal()] = EXACT[CubeRotation.DOWN_EAST.ordinal()];
        EDGE[CubeEdge.UP_NORTH.ordinal()] = EXACT[CubeRotation.UP_NORTH.ordinal()];
        EDGE[CubeEdge.UP_EAST.ordinal()] = EXACT[CubeRotation.UP_EAST.ordinal()];
        EDGE[CubeEdge.UP_SOUTH.ordinal()] = EXACT[CubeRotation.UP_SOUTH.ordinal()];
        EDGE[CubeEdge.UP_WEST.ordinal()] = EXACT[CubeRotation.UP_WEST.ordinal()];
        EDGE[CubeEdge.NORTH_EAST.ordinal()] = EXACT[CubeRotation.NORTH_EAST.ordinal()];
        EDGE[CubeEdge.NORTH_WEST.ordinal()] = EXACT[CubeRotation.NORTH_WEST.ordinal()];
        EDGE[CubeEdge.SOUTH_EAST.ordinal()] = EXACT[CubeRotation.SOUTH_EAST.ordinal()];
        EDGE[CubeEdge.SOUTH_WEST.ordinal()] = EXACT[CubeRotation.SOUTH_WEST.ordinal()];
    }


    public static PolyTransform get(BaseModelState modelState) {
        return LOOKUP[modelState.orientationType().ordinal()][modelState.orientationIndex()];
    }

    public static PolyTransform forEdgeRotation(int ordinal) {
        return EXACT[ordinal];
    }
    
    public static PolyTransform get(CubeRotation corner) {
        return EXACT[corner.ordinal()];
    }
    
    public static PolyTransform get(Axis axis) {
        return AXIS[axis.ordinal()];
    }
    
    public static PolyTransform get(Direction face) {
        return FACE[face.ordinal()];
    }
    
    private static PolyTransformImpl createEdgeTransform(CubeRotation edge) {
        Matrix4f matrix = new Matrix4f().identity();
        
        switch(edge) {
        case DOWN_EAST:
            matrix.rotate((float) Math.toRadians(90), 0, 1, 0);
            break;
        case DOWN_NORTH:
            matrix.rotate((float) Math.toRadians(180), 0, 1, 0);
            break;
        case DOWN_SOUTH:
            // default state
            break;
        case DOWN_WEST:
            matrix.rotate((float) Math.toRadians(270), 0, 1, 0);
            break;
        case UP_EAST:
            matrix.rotate((float) Math.toRadians(90), 0, 1, 0).rotate((float) Math.toRadians(180), 0, 0, 1);
            break;
        case UP_NORTH:
            matrix.rotate((float) Math.toRadians(0), 0, 1, 0).rotate((float) Math.toRadians(180), 1, 0, 0);
            break;
        case UP_SOUTH:
            matrix.rotate((float) Math.toRadians(180), 0, 0, 1);
            break;
        case UP_WEST:
            matrix.rotate((float) Math.toRadians(90), 0, 1, 0).rotate((float) Math.toRadians(180), 1, 0, 0);
            break;
        case EAST_DOWN:
            matrix.rotate((float) Math.toRadians(90), 0, 0, 1).rotate((float) Math.toRadians(270), 0, 1, 0);
            break;
        case EAST_NORTH:
            matrix.rotate((float) Math.toRadians(90), 0, 0, 1).rotate((float) Math.toRadians(180), 0, 1, 0);
            break;
        case EAST_SOUTH:
            matrix.rotate((float) Math.toRadians(90), 0, 0, 1);
            break;
        case EAST_UP:
            matrix.rotate((float) Math.toRadians(270), 1, 0, 0).rotate((float) Math.toRadians(90), 0, 0, 1);
            break;
        case WEST_DOWN:
            matrix.rotate((float) Math.toRadians(270), 0, 0, 1).rotate((float) Math.toRadians(90), 0, 1, 0);
            break;
        case WEST_NORTH:
            matrix.rotate((float) Math.toRadians(270), 0, 0, 1).rotate((float) Math.toRadians(180), 0, 1, 0);
            break;
        case WEST_SOUTH:
            matrix.rotate((float) Math.toRadians(270), 0, 0, 1);
            break;
        case WEST_UP:
            matrix.rotate((float) Math.toRadians(270), 0, 0, 1).rotate((float) Math.toRadians(270), 0, 1, 0);
            break;
        case NORTH_DOWN:
            matrix.rotate((float) Math.toRadians(90), 1, 0, 0);
            break;
        case NORTH_EAST:
            matrix.rotate((float) Math.toRadians(90), 1, 0, 0).rotate((float) Math.toRadians(90), 0, 1, 0);
            break;
        case NORTH_UP:
            matrix.rotate((float) Math.toRadians(180), 0, 0, 1).rotate((float) Math.toRadians(90), 1, 0, 0);
            break;
        case NORTH_WEST:
            matrix.rotate((float) Math.toRadians(90), 1, 0, 0).rotate((float) Math.toRadians(270), 0, 1, 0);
            break;
        case SOUTH_DOWN:
            matrix.rotate((float) Math.toRadians(270), 1, 0, 0).rotate((float) Math.toRadians(180), 0, 1, 0);
            break;
        case SOUTH_EAST:
            matrix.rotate((float) Math.toRadians(270), 1, 0, 0).rotate((float) Math.toRadians(90), 0, 1, 0);
            break;
        case SOUTH_UP:
            matrix.rotate((float) Math.toRadians(270), 1, 0, 0);
            break;
        case SOUTH_WEST:
            matrix.rotate((float) Math.toRadians(270), 1, 0, 0).rotate((float) Math.toRadians(270), 0, 1, 0);
            break;

        default:
            break;
        
        };
        return new PolyTransformImpl(matrix);
    }
}
