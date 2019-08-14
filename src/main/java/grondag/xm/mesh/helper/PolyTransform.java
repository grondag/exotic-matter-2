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

import org.joml.Matrix4f;
import org.joml.Vector3f;

import grondag.xm.api.connect.model.BlockEdgeSided;
import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.model.varia.BlockOrientationType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

@SuppressWarnings("rawtypes")
public class PolyTransform {

    private final Matrix4f matrix;

    public PolyTransform(Matrix4f matrix) {
        this.matrix = matrix;
    }

    public void apply(MutablePolygon poly) {
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
        poly.nominalFace(QuadHelper.computeFaceForNormal(vec.x, vec.y, vec.z));
        final Direction cullFace = poly.cullFace();
        if(cullFace != null) {
            oldVec = cullFace.getVector();
            matrix.transformDirection(oldVec.getX(), oldVec.getY(), oldVec.getZ(), vec);
            poly.cullFace(QuadHelper.computeFaceForNormal(vec.x, vec.y, vec.z));
        }
    }

    private static final ThreadLocal<Vector3f> VEC3 = ThreadLocal.withInitial(Vector3f::new);

    private final static PolyTransform[][] LOOKUP;

//    private final static Direction[][] FACING_MAP = new Direction[32][6];
//    private final static Direction[][] FACING_MAP_INVERSE = new Direction[32][6];
//
//    private final static FaceMap[] FACE_MAPS = new FaceMap[32];

//    /**
//     * Facemap that contains identity transform.
//     */
//    public static final FaceMap IDENTITY_FACEMAP;

    static {
        LOOKUP = new PolyTransform[BlockOrientationType.values().length][];
        
        //TODO: populate
        
//        final Axis[] avals = { Axis.X, Axis.Y, Axis.Z, null };
//        for (Axis axis : avals) {
//            for (ClockwiseRotation rot : ClockwiseRotation.values()) {
//                populateLookups(axis, false, rot);
//                populateLookups(axis, true, rot);
//            }
//        }

//        IDENTITY_FACEMAP = getFaceMap(computeKey(null, false, ClockwiseRotation.ROTATE_NONE));
    }

//    private static void populateLookups(Axis axis, boolean isAxisInverted, ClockwiseRotation rotation) {
//        int key = computeKey(axis, isAxisInverted, rotation);
//        Matrix4f matrix = computeMatrix(axis, isAxisInverted, rotation);
//        LOOKUP[key] = new PolyTransform(matrix);
//
//        for (int i = 0; i < 6; i++) {
//            Direction face = ModelHelper.faceFromIndex(i);
//            Vec3i dir = face.getVector();
//            Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
//            matrix.transform(vec);
//            Direction mappedFace = Direction.getFacing(vec.x, vec.y, vec.z);
//
//            FACING_MAP[key][face.ordinal()] = mappedFace;
//            FACING_MAP_INVERSE[key][mappedFace.ordinal()] = face;
//        }
//        FACE_MAPS[key] = new FaceMap(key);
//    }

    /**
     * Find appropriate transformation assuming base model is oriented with as follows:
     * Axis = Y with positive orientation if orientation applies.<p>
     * 
     * For the default rotation, generally, {@code DOWN} is considered the "bottom"
     * and {@code SOUTH} is the "back" when facing the "front" of the primitive.<p>
     * 
     * For primitives oriented to a corner, the default corner is "bottom, right, back"
     * in the frame just described, or {@code DOWN}, {@code SOUTH}, {@code EAST} in terms
     * of specific faces.
     */
    public static PolyTransform get(PrimitiveModelState modelState) {
        
        return computeLookup(modelState.orientationType(), modelState.orientationIndex());
    }

    private static PolyTransform computeLookup(BlockOrientationType type, int orientationIndex) {
        
        Matrix4f matrix = new Matrix4f().identity();
        if(type != null) {
//            //TODO: put back pre-population of lookups
//            return LOOKUP[type.ordinal()][orientationIndex];
            
            
            switch (type) {
            case AXIS:
              //TODO - can probably map to edge_sided
                break;
            case CORNER:
              //TODO - can probably map to edge_sided
                break;
            case EDGE:
              //TODO - can probably map to edge_sided
                break;
            case EDGE_SIDED:
                edgeSidedMatrix(matrix, orientationIndex);
                break;
            case FACE:
              //TODO - can probably map to edge_sided
                break;
            case NONE:
            default:
                
                break;
            
            }
        }
        
        return new PolyTransform(matrix);
    }

    
    private static void edgeSidedMatrix(Matrix4f matrix, int index) {
        BlockEdgeSided edge = BlockEdgeSided.fromOrdinal(index);
        
        switch(edge) {
        case DOWN_EAST:
            matrix.rotate((float) Math.toRadians(90), 0, 1, 0);
            break;
        case DOWN_NORTH:
            matrix.rotate((float) Math.toRadians(180), 0, 1, 0);
            break;
        case DOWN_SOUTH:
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
            
            
            // TODO: finish all these once have a block to test them
            
        case EAST_DOWN:
            break;
        case EAST_NORTH:
            break;
        case EAST_SOUTH:
            break;
        case EAST_UP:
            break;
        case NORTH_DOWN:
            break;
        case NORTH_EAST:
            break;
        case NORTH_UP:
            break;
        case NORTH_WEST:
            break;
        case SOUTH_DOWN:
            break;
        case SOUTH_EAST:
            break;
        case SOUTH_UP:
            break;
        case SOUTH_WEST:
            break;

        case WEST_DOWN:
            break;
        case WEST_NORTH:
            break;
        case WEST_SOUTH:
            break;
        case WEST_UP:
            break;
        default:
            break;
        
        };
    }
    
    private static Matrix4f getMatrixForAxis(Direction.Axis axis, boolean isAxisInverted) {
        switch (axis) {
        case X:
            // may not be right - not tested since last change
            return isAxisInverted ? 
                    new Matrix4f().identity().rotate((float) Math.toRadians(270), 0, 0, 1).rotate((float) Math.toRadians(90), 0, 1, 0)
                    : new Matrix4f().identity().rotate((float) Math.toRadians(90), 0, 0, 1).rotate((float) Math.toRadians(270), 0, 1, 0);

        case Y:
            return isAxisInverted ? new Matrix4f().identity().scale(1f, -1f, 1f)
                    : new Matrix4f().identity();


        case Z:
            // may not be right - not tested since last change
            return isAxisInverted ? new Matrix4f().identity().rotate((float) Math.toRadians(270), 1, 0, 0) 
                    : new Matrix4f().identity().rotate((float) Math.toRadians(90), 1, 0, 0);

        default:
            return new Matrix4f().identity();

        }
    }

    private static Matrix4f getMatrixForRotation(ClockwiseRotation rotation) {
        switch (rotation) {
        default:
        case ROTATE_NONE:
            return new Matrix4f().identity();

        case ROTATE_90:
            // inverse because JOML is counter-clockwise right-handed
            return new Matrix4f().identity().rotate((float) Math.toRadians(270), 0, 1, 0);

        case ROTATE_180:
            return new Matrix4f().identity().rotate((float) Math.toRadians(180), 0, 1, 0);

        case ROTATE_270:
            // inverse because JOML is counter-clockwise right-handed
            return new Matrix4f().identity().rotate((float) Math.toRadians(90), 0, 1, 0);
        }
    }

    private static Matrix4f getMatrixForAxisAndRotation(Direction.Axis axis, boolean isAxisInverted, ClockwiseRotation rotation) {
        Matrix4f result = getMatrixForAxis(axis, isAxisInverted);
        result.mul(getMatrixForRotation(rotation));
        return result;
    }
}
