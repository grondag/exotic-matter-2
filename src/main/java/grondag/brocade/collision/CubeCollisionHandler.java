package grondag.brocade.collision;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.model.state.ISuperModelState;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;

public class CubeCollisionHandler implements ICollisionHandler
{

    public static CubeCollisionHandler INSTANCE = new CubeCollisionHandler();
    
    private static final List<AxisAlignedBB> FULL_BLOCK_AABB_LIST = ImmutableList.of(Block.FULL_BLOCK_AABB);
 
    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState)
    {
        return FULL_BLOCK_AABB_LIST;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ISuperModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ISuperModelState modelState)
    {
        return Block.FULL_BLOCK_AABB;
    }
}