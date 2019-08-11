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
import org.joml.Vector4f;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.mesh.polygon.IMutablePolygon;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3i;

@SuppressWarnings("rawtypes")
public class PolyTransform {

    private final Matrix4f matrix;

    public PolyTransform(Matrix4f matrix) {
        this.matrix = matrix;
    }

    public void apply(IMutablePolygon poly) {
        final Matrix4f matrix = this.matrix;
        final int vertexCount = poly.vertexCount();
        final Vector3f vec = VEC3.get();
        final Vec3i oldVec = poly.nominalFace().getVector();

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
        matrix.transformDirection(oldVec.getX(), oldVec.getY(), oldVec.getZ(), vec);
        poly.setNominalFace(QuadHelper.computeFaceForNormal(vec.x, vec.y, vec.z));
    }

    private static final ThreadLocal<Vector3f> VEC3 = ThreadLocal.withInitial(Vector3f::new);

    private final static PolyTransform[] LOOKUP = new PolyTransform[32];

    private final static Direction[][] FACING_MAP = new Direction[32][6];
    private final static Direction[][] FACING_MAP_INVERSE = new Direction[32][6];

    private final static FaceMap[] FACE_MAPS = new FaceMap[32];

    /**
     * Facemap that contains identity transform.
     */
    public static final FaceMap IDENTITY_FACEMAP;

    static {
        final Axis[] avals = { Axis.X, Axis.Y, Axis.Z, null };
        for (Axis axis : avals) {
            for (ClockwiseRotation rot : ClockwiseRotation.values()) {
                populateLookups(axis, false, rot);
                populateLookups(axis, true, rot);
            }
        }

        IDENTITY_FACEMAP = getFaceMap(computeKey(null, false, ClockwiseRotation.ROTATE_NONE));
    }

    private static void populateLookups(Axis axis, boolean isAxisInverted, ClockwiseRotation rotation) {
        int key = computeKey(axis, isAxisInverted, rotation);
        Matrix4f matrix = computeMatrix(axis, isAxisInverted, rotation);
        LOOKUP[key] = new PolyTransform(matrix);

        for (int i = 0; i < 6; i++) {
            Direction face = ModelHelper.faceFromIndex(i);
            Vec3i dir = face.getVector();
            Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
            matrix.transform(vec);
            Direction mappedFace = Direction.getFacing(vec.x, vec.y, vec.z);

            FACING_MAP[key][face.ordinal()] = mappedFace;
            FACING_MAP_INVERSE[key][mappedFace.ordinal()] = face;
        }
        FACE_MAPS[key] = new FaceMap(key);
    }

    /**
     * Find appropriate transformation assuming base model is oriented to Y
     * orthogonalAxis, positive. This is different than the Minecraft/Forge default
     * because I brain that way.<br>
     * <br>
     * 
     * Models in default state should have orthogonalAxis = Y with positive
     * orientation (if orientation applies) and whatever rotation that represents
     * "none". Generally, in this mod, north is considered "top" of the reference
     * frame when looking down the Y-orthogonalAxis. <br>
     * <br>
     * 
     * Models that are oriented to an edge, like stairs and wedges should have a
     * default geometry such that the North and East faces are "full" or "behind"
     * the sloped part of the geometry.<br>
     * <br>
     * 
     * With a model in the default state, the rotation occurs start - around the
     * Y-orthogonalAxis, followed by the transformation from the Y
     * orthogonalAxis/orientation to whatever new orthogonalAxis/orientation is
     * given. <br>
     * <br>
     * 
     * Because 4d rotational matrices are the brain-child of malevolent walrus
     * creatures from hyperspace, this means the order of multiplication is the
     * opposite of what I just described. See this in
     * {@link #getMatrixForAxisAndRotation(net.minecraft.util.math.Direction.Axis, boolean, Rotation)}
     */
    public static PolyTransform get(PrimitiveModelState modelState) {

        // TODO: put back
        return new PolyTransform(computeMatrix(modelState.axis(), modelState.isAxisInverted(), modelState.axisRotation()));
        // return LOOKUP[computeTransformKey(modelState)];
    }

    private static Matrix4f computeMatrix(Direction.Axis axis, boolean isAxisInverted, ClockwiseRotation rotation) {
        if (axis != null) {
            if (rotation != ClockwiseRotation.ROTATE_NONE) {
                return getMatrixForAxisAndRotation(axis, isAxisInverted, rotation);
            } else {
                return getMatrixForAxis(axis, isAxisInverted);
            }
        } else if (rotation != ClockwiseRotation.ROTATE_NONE) {
            return getMatrixForRotation(rotation);
        } else {
            return new Matrix4f().identity();
        }
    }

    /**
     * Compute array lookup index. Key space is not efficient, because no axis is
     * equivalent to Y axis, but only 32 values vs 16 and allows us to use bit
     * arithmetic. Could be called frequently.
     * 
     * @param axis           null if model state has no access
     * @param isAxisInverted is ignored if axis
     * @param axisRotation   null handled as no axis rotation
     */
    private static int computeKey(Direction.Axis axis, boolean isAxisInverted, ClockwiseRotation rotation) {
        int bits = 0;
        if (axis != null) {
            bits = (axis.ordinal() + 1) | (isAxisInverted ? 4 : 0);
        }
        if (rotation != null) {
            bits |= rotation.ordinal() << 3;
        }
        return bits;
    }

    /**
     * Returns a key that can be used to retrieve values without the input model
     * state. Useful for some serialization scenarios.
     */
    public static int computeTransformKey(PrimitiveModelState modelState) {
        return modelState.hasAxis() ? computeKey(modelState.axis(), modelState.isAxisInverted(), modelState.axisRotation())
                : computeKey(null, false, modelState.axisRotation());
    }

    private static Matrix4f getMatrixForAxis(Direction.Axis axis, boolean isAxisInverted) {
        switch (axis) {
        case X:
            return isAxisInverted ? new Matrix4f().identity().rotate((float) Math.toRadians(270), 1, 0, 0).rotate((float) Math.toRadians(90), 0, 0, 1)
                    : new Matrix4f().identity().rotate((float) Math.toRadians(90), 1, 0, 0).rotate((float) Math.toRadians(270), 0, 0, 1);

        case Y:
            return isAxisInverted ? new Matrix4f().identity().rotate((float) Math.toRadians(180), 1, 0, 0) : new Matrix4f().identity();

        case Z:
            return isAxisInverted ? new Matrix4f().identity().rotate((float) Math.toRadians(270), 1, 0, 0).rotate((float) Math.toRadians(180), 0, 1, 0)
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
            // inverted because JOML is counter-clockwise with RH coordinates
            return new Matrix4f().identity().rotate((float) Math.toRadians(270), 0, 1, 0);

        case ROTATE_180:
            return new Matrix4f().identity().rotate((float) Math.toRadians(180), 0, 1, 0);

        case ROTATE_270:
            // inverted because JOML is counter-clockwise with RH coordinates
            return new Matrix4f().identity().rotate((float) Math.toRadians(90), 0, 1, 0);
        }
    }

    private static Matrix4f getMatrixForAxisAndRotation(Direction.Axis axis, boolean isAxisInverted, ClockwiseRotation rotation) {
        Matrix4f result = getMatrixForAxis(axis, isAxisInverted);
        result.mul(getMatrixForRotation(rotation));
        return result;
    }

    /**
     * Transforms input face by applying axis, axis inversion and rotation of the
     * model state and returns the result. If the model state has no transformations
     * (or doesn't have any orientation to be transformed) then simply returns the
     * input face.
     */
    public static Direction rotateFace(PrimitiveModelState modelState, Direction face) {
        return FACE_MAPS[computeTransformKey(modelState)].map(face);
    }

    /**
     * Returns list of face -> face mappings for the given model state. The ordinal
     * of the input face is the index of the output (mapped) face in the provided
     * list.
     * <p>
     * 
     * Equivalently, list containing results of calling
     * {@link #rotateFace(MutableModelState, Direction)} for each face in Enum
     * order.
     */
    public static FaceMap getFaceMap(PrimitiveModelState modelState) {
        return FACE_MAPS[computeTransformKey(modelState)];
    }

    /**
     * Same as {@link #getFaceMap(MutableModelState)} but uses the output of
     * {@link #computeTransformKey(MutableModelState)} instead of modelstate.
     */
    public static FaceMap getFaceMap(int transformKey) {
        return FACE_MAPS[transformKey];
    }

    public static class FaceMap {
        public final int index;

        private FaceMap(int index) {
            this.index = index;
        }

        /**
         * Returns face that results from applying the transform associated with this
         * map.
         */
        public Direction map(Direction fromFace) {
            return FACING_MAP[this.index][fromFace.ordinal()];
        }

        /**
         * Inverse of {@link #map(Direction)}. Maps from output face of transform back
         * to input face.
         */
        public Direction inverseMap(Direction toFace) {
            return FACING_MAP_INVERSE[this.index][toFace.ordinal()];
        }
    }
}
