package grondag.brocade.world;

import grondag.brocade.api.block.BrocadeBlock;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SuperBlockBorderMatch extends AbstractNonFaceTest
{
    private final BrocadeBlock block;
    private final ISuperModelState matchModelState;
    private final boolean isSpeciesPartOfMatch;
    
    /** pass in the info for the block you want to match */
    public SuperBlockBorderMatch(BrocadeBlock block, ISuperModelState modelState, boolean isSpeciesPartOfMatch)
    {
        this.block = block;
        this.matchModelState = modelState;
        this.isSpeciesPartOfMatch = isSpeciesPartOfMatch;
    }
    
    /** assumes you want to match block at given position */
    public SuperBlockBorderMatch(BlockView world, BlockState blockState, BlockPos pos, boolean isSpeciesPartOfMatch)
    {
        this.block = ((BrocadeBlock)blockState.getBlock());
        //last param = false prevents recursion - we don't need the full model state (which depends on this logic)
        this.matchModelState = block.getModelStateAssumeStateIsCurrent(blockState, world, pos, true);
        this.isSpeciesPartOfMatch = isSpeciesPartOfMatch;
    }
    
    @Override 
    public boolean wantsModelState() { return true; }
    
    @Override
    protected boolean testBlock(BlockView world, BlockState blockState, BlockPos pos, ISuperModelState modelState)
    {
        return blockState.getBlock() == this.block && testBlockInner(modelState);
    }

    @Override
    protected boolean testBlock(BlockView world, BlockState blockState, BlockPos pos)
    {
        return blockState.getBlock() == this.block && testBlockInner(block.getModelStateAssumeStateIsCurrent(blockState, world, pos, false));
    }
    
    private boolean testBlockInner(ISuperModelState modelState) {
        return this.matchModelState.doShapeAndAppearanceMatch(modelState)
                && (!this.isSpeciesPartOfMatch || !modelState.hasSpecies() || (this.matchModelState.getSpecies() == modelState.getSpecies()));
    }
}
