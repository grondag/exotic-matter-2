package grondag.brocade.world;

import grondag.brocade.block.ISuperBlock;
import grondag.brocade.block.ISuperBlockAccess;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SuperBlockBorderMatch extends AbstractNonFaceTest
{
    private final ISuperBlock block;
    private final ISuperModelState matchModelState;
    private final boolean isSpeciesPartOfMatch;
    
    /** pass in the info for the block you want to match */
    public SuperBlockBorderMatch(ISuperBlock block, ISuperModelState modelState, boolean isSpeciesPartOfMatch)
    {
        this.block = block;
        this.matchModelState = modelState;
        this.isSpeciesPartOfMatch = isSpeciesPartOfMatch;
    }
    
    /** assumes you want to match block at given position */
    public SuperBlockBorderMatch(ISuperBlockAccess world, BlockState ibs, BlockPos pos, boolean isSpeciesPartOfMatch)
    {
        this.block = ((ISuperBlock)ibs.getBlock());
        //last param = false prevents recursion - we don't need the full model state (which depends on this logic)
        this.matchModelState = world.getModelState(this.block, ibs, pos, false);
        this.isSpeciesPartOfMatch = isSpeciesPartOfMatch;
    }
    
    @Override 
    public boolean wantsModelState() { return true; }
    
    @Override
    protected boolean testBlock(ISuperBlockAccess world, BlockState ibs, BlockPos pos, ISuperModelState modelState)
    {
        return ibs.getBlock() == this.block 
                && this.matchModelState.doShapeAndAppearanceMatch(modelState)
                && (!this.isSpeciesPartOfMatch || !modelState.hasSpecies() || (this.matchModelState.getSpecies() == modelState.getSpecies()));
    }

    @Override
    protected boolean testBlock(ISuperBlockAccess world, BlockState ibs, BlockPos pos)
    {
        return testBlock(world, ibs, pos, world.getModelState(this.block, ibs, pos, false));
    }
}
