package grondag.brocade.collision;

import java.util.List;
import java.util.stream.Collectors;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BoundingBox;

public interface ICollisionHandler {
    public static final BoundingBox FULL_BLOCK_BOX = new BoundingBox(0, 0, 0, 1, 1, 1);
    
    public default List<BoundingBox> getCollisionBoxes(ISuperModelState modelState, BlockPos offset) {
        return getCollisionBoxes(modelState).stream().map(aabb -> aabb.offset(offset)).collect(Collectors.toList());
    }

    public default List<BoundingBox> getCollisionBoxes(ISuperModelState modelState) {
        return CollisionBoxDispatcher.getCollisionBoxes(modelState);
    }

    public default BoundingBox getCollisionBoundingBox(ISuperModelState modelState) {
        return FULL_BLOCK_BOX;
    }

    public default BoundingBox getRenderBoundingBox(ISuperModelState modelState) {
        return FULL_BLOCK_BOX;
    }

    /**
     * Creates an AABB with the bounds and rotation provided.
     */
    public static BoundingBox makeRotatedAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Matrix4f rotation)
    {
        Vector3f minPos = new Vector3f(minX, minY, minZ);
        Vector3f maxPos = new Vector3f(maxX, maxY, maxZ);
        rotation.transformPosition(minPos);
        rotation.transformPosition(maxPos);
        return new BoundingBox(minPos.x, minPos.y, minPos.z, 
                maxPos.x, maxPos.y, maxPos.z);
    }
    
    public static BoundingBox makeRotatedAABB(BoundingBox fromAABB, Matrix4f rotation)
    {
        return makeRotatedAABB((float)fromAABB.minX, (float)fromAABB.minY, (float)fromAABB.minZ, (float)fromAABB.maxX, (float)fromAABB.maxY, (float)fromAABB.maxZ, rotation);
    }
}
