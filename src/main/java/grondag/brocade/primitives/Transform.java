package grondag.brocade.primitives;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.model.TRSRTransformation;

public class Transform
{
    private final static Matrix4f[] MATRIX_LOOKUP = new Matrix4f[32];
    
    private final static EnumFacing[][]FACING_MAP = new EnumFacing[32][6];
    private final static EnumFacing[][]FACING_MAP_INVERSE = new EnumFacing[32][6];
    
    private final static FaceMap[] FACE_MAPS = new FaceMap[32];
    /**
     * Facemap that contains identify transform.
     */
    public static final FaceMap IDENTITY_FACEMAP;
    
    static
    {
        final Axis[] avals = {Axis.X, Axis.Y, Axis.Z, null};
        for(Axis axis : avals)
        {
            for(int r = 0; r < Rotation.COUNT; r++)
            {
                final Rotation rot = Rotation.VALUES[r];
                populateLookups(axis, false, rot);
                populateLookups(axis, true, rot);
            }
        }
        
        IDENTITY_FACEMAP = getFaceMap(computeKey(null, false, Rotation.ROTATE_NONE));
    }
    
    private static void populateLookups(@Nullable Axis axis, boolean isAxisInverted, Rotation rotation)
    {
        int key = computeKey(axis, isAxisInverted, rotation);
        Matrix4f matrix = computeMatrix(axis, isAxisInverted, rotation);
//        Log.info(String.format("key=%d axis=%s isInverted=%s rotation=%s", 
//                key,
//                axis == null ? "null" : axis.toString(),
//                Boolean.toString(isAxisInverted),
//                rotation.toString()));
        MATRIX_LOOKUP[key] = matrix;
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            EnumFacing mappedFace = TRSRTransformation.rotate(matrix, face);
            FACING_MAP[key][face.ordinal()] = mappedFace;
            FACING_MAP_INVERSE[key][mappedFace.ordinal()] = face;
//            Log.info(String.format("%s -> %s", face.toString(), FACE_LOOKUP[key][face.ordinal()].toString()));
        }
        FACE_MAPS[key] = new FaceMap(key);
    }
    
    /**
     * Find appropriate transformation assuming base model is oriented to Y orthogonalAxis, positive.
     * This is different than the Minecraft/Forge default because I brain that way.<br><br>
     * 
     * Models in default state should have orthogonalAxis = Y with positive
     * orientation (if orientation applies) and whatever rotation
     * that represents "none".  Generally, in this mod, north is
     * considered "top" of the reference frame when looking down
     * the Y-orthogonalAxis. <br><br>
     * 
     * Models that are oriented to an edge, like stairs and wedges
     * should have a default geometry such that the North and East
     * faces are "full" or "behind" the sloped part of the geometry.<br><br>
     * 
     * With a model in the default state, the rotation occurs
     * start - around the Y-orthogonalAxis, followed by the transformation
     * from the Y orthogonalAxis/orientation to whatever new orthogonalAxis/orientation is 
     * given. <br><br>
     * 
     * Because 4d rotational matrices are the brain-child of
     * malevolent walrus creatures from hyperspace, this means
     * the order of multiplication is the opposite of what I just described.
     * See this in {@link #getMatrixForAxisAndRotation(net.minecraft.util.EnumFacing.Axis, boolean, Rotation)}
     */
    public static Matrix4f getMatrix4f(ISuperModelState modelState)
    {
        return MATRIX_LOOKUP[computeTransformKey(modelState)];
    }
    
    private static Matrix4f computeMatrix(@Nullable EnumFacing.Axis axis, boolean isAxisInverted, Rotation rotation)
    {
        if(axis != null)
        {
            if(rotation != Rotation.ROTATE_NONE)
            {
                return getMatrixForAxisAndRotation(axis, isAxisInverted, rotation);
            }
            else
            {
                return getMatrixForAxis(axis, isAxisInverted);
            }
        }
        else if(rotation != Rotation.ROTATE_NONE)
        {
            return getMatrixForRotation(rotation);
        }
        else
        {
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y0);
        }
    }
    
    /**
     * Compute array lookup index.  Key space is not efficient,
     * because no axis is equivalent to Y axis, but only 32 values
     * vs 16 and allows us to use bit arithmetic. Could be called
     * frequently.
     * 
     * @param axis  null if model state has no access
     * @param isAxisInverted is ignored if axis
     * @param axisRotation null handled as no axis rotation
     */
    private static int computeKey(@Nullable EnumFacing.Axis axis, boolean isAxisInverted, @Nullable Rotation rotation)
    {
        int bits = 0;
        if(axis != null)
        {
            bits = (axis.ordinal() + 1)
                    | (isAxisInverted ? 4 : 0);
        }
        if(rotation != null)
        {
            bits |= rotation.ordinal() << 3;
        }
        return bits;       
    }
    
    /**
     * Returns a key that can be used to retrieve values
     * without the input model state. Useful for some 
     * serialization scenarios.
     */
    public static int computeTransformKey(ISuperModelState modelState)
    {
        return modelState.hasAxis()
            ? computeKey(modelState.getAxis(), modelState.isAxisInverted(), modelState.getAxisRotation())
            : computeKey(null, false, modelState.getAxisRotation());
    }
    
    /**
     * See {@link #getMatrix4f()}t
     */
    private static Matrix4f getMatrixForAxis(EnumFacing.Axis axis, boolean isAxisInverted)
    {
        switch(axis)
        {
        case X:
            return ForgeHooksClient.getMatrix(isAxisInverted ? ModelRotation.X90_Y270 : ModelRotation.X90_Y90);
    
        case Y:
            return ForgeHooksClient.getMatrix(isAxisInverted ? ModelRotation.X180_Y0 : ModelRotation.X0_Y0);
    
        case Z:
            return ForgeHooksClient.getMatrix(isAxisInverted ? ModelRotation.X90_Y0 : ModelRotation.X270_Y0);
            
        default:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y0);
        
        }
    }
    
    /**
     * See {@link #getMatrix4f()}t
     */
    private static Matrix4f getMatrixForRotation(Rotation rotation)
    {
        switch(rotation)
        {
        default:
        case ROTATE_NONE:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y0);
    
        case ROTATE_90:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y90);
    
        case ROTATE_180:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y180);
            
        case ROTATE_270:
            return ForgeHooksClient.getMatrix(ModelRotation.X0_Y270);
        }
    }

    /**
     * See {@link #getMatrix4f()}t
     */
    private static Matrix4f getMatrixForAxisAndRotation(EnumFacing.Axis axis, boolean isAxisInverted, Rotation rotation)
    {
        Matrix4f result = getMatrixForAxis(axis, isAxisInverted);
        result.mul(getMatrixForRotation(rotation));
        return result;
    }
    
    /**
     * Transforms input face by applying axis, axis inversion 
     * and rotation of the model state and returns the result.
     * If the model state has no transformations (or doesn't 
     * have any orientation to be transformed) then simply 
     * returns the input face.
     */
    public static EnumFacing rotateFace(ISuperModelState modelState, EnumFacing face)
    {
        return FACE_MAPS[computeTransformKey(modelState)].map(face);
    }
    
    /**
     * Returns list of face -> face mappings for the given
     * model state.  The ordinal of the input face is the index
     * of the output (mapped) face in the provided list.<p>
     * 
     * Equivalently, list containing results of calling {@link #rotateFace(ISuperModelState, EnumFacing)}
     * for each face in Enum order.
     */
    public static FaceMap getFaceMap(ISuperModelState modelState)
    {
        return FACE_MAPS[computeTransformKey(modelState)];
    }
    
    /**
     * Same as {@link #getFaceMap(ISuperModelState)} but uses the output
     * of {@link #computeTransformKey(ISuperModelState)} instead of modelstate.
     */
    public static FaceMap getFaceMap(int transformKey)
    {
        return FACE_MAPS[transformKey];
    }
    
    public static class FaceMap
    {
        public final int index;
        
        private FaceMap(int index)
        {
            this.index = index;
        }
        
        /**
         * Returns face that results from applying the transform
         * associated with this map.
         */
        public EnumFacing map(EnumFacing fromFace)
        {
            return FACING_MAP[this.index][fromFace.ordinal()];
        }
        
        /**
         * Inverse of {@link #map(EnumFacing)}.  Maps from output
         * face of transform back to input face.
         */
        public EnumFacing inverseMap(EnumFacing toFace)
        {
            return FACING_MAP_INVERSE[this.index][toFace.ordinal()];
        }
    }
}
