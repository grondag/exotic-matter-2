package grondag.brocade.collision;

import java.util.List;
import java.util.stream.Collectors;

import grondag.exotic_matter.model.state.ISuperModelState;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface ICollisionHandler
{
    
    public default List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState, BlockPos offset)
    {
        return getCollisionBoxes(modelState).stream().map(aabb -> aabb.offset(offset)).collect(Collectors.toList());
    }
    
    public default List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState)
    {
        return CollisionBoxDispatcher.getCollisionBoxes(modelState);
    }

    public default AxisAlignedBB getCollisionBoundingBox(ISuperModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }

    public default AxisAlignedBB getRenderBoundingBox(ISuperModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }
 
}
