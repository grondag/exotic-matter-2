package grondag.brocade.world;

import grondag.brocade.block.ISuperBlockAccess;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Base class for block tests that don't care about facing.
 */
public abstract class AbstractNonFaceTest implements IBlockTest{
    abstract protected boolean testBlock(ISuperBlockAccess world, BlockState ibs, BlockPos pos, ISuperModelState modelState);

    abstract protected boolean testBlock(ISuperBlockAccess world, BlockState ibs, BlockPos pos);
    
    @Override
    public boolean testBlock(Direction face, ISuperBlockAccess world, BlockState ibs, BlockPos pos)
    {
        return this.testBlock(world, ibs, pos);
    }
    
    @Override
    public boolean testBlock(Direction face, ISuperBlockAccess world, BlockState ibs, BlockPos pos, ISuperModelState modelState)
    {
        return this.testBlock(world, ibs, pos, modelState);
    }

    @Override
    public boolean testBlock(BlockCorner corner, ISuperBlockAccess world, BlockState ibs, BlockPos pos)
    {
        return this.testBlock(world, ibs, pos);
    }

    @Override
    public boolean testBlock(BlockCorner face, ISuperBlockAccess world, BlockState ibs, BlockPos pos, ISuperModelState modelState)
    {
        return this.testBlock(world, ibs, pos, modelState);
    }
    
    @Override
    public boolean testBlock(FarCorner corner, ISuperBlockAccess world, BlockState ibs, BlockPos pos)
    {
        return this.testBlock(world, ibs, pos);
    }
    
    @Override
    public boolean testBlock(FarCorner face, ISuperBlockAccess world, BlockState ibs, BlockPos pos, ISuperModelState modelState)
    {
        return this.testBlock(world, ibs, pos, modelState);
    }
}
