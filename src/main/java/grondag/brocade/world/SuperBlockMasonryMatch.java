package grondag.brocade.world;

import grondag.brocade.block.ISuperBlock;
import grondag.brocade.block.ISuperBlockAccess;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/** returns true if NO border should be displayed */
public class SuperBlockMasonryMatch extends AbstractNonFaceTest
{
    private final ISuperBlock block;
    private final int matchSpecies;
    private final BlockPos origin;
    
    /** pass in the info for the block you want to match */
    public SuperBlockMasonryMatch(ISuperBlock block, int matchSpecies, BlockPos pos)
    {
        this.block = block;
        //last param = false prevents recursion - we don't need the full model state (which depends on this logic)
        this.matchSpecies = matchSpecies;
        this.origin = pos.toImmutable();
    }
    
    @Override 
    public boolean wantsModelState() { return true; }

    @Override
    protected boolean testBlock(ISuperBlockAccess world, BlockState ibs, BlockPos pos)
    {
        return testBlock(world, ibs, pos, world.getModelState(this.block, ibs, pos, false));
    }
    
    @Override
    protected boolean testBlock(ISuperBlockAccess world, BlockState blockState, BlockPos pos, ISuperModelState modelState)
    {
        // for masonry blocks, a join indicates that a border IS present
        
        boolean isSibling = blockState.getBlock() == this.block && modelState.hasMasonryJoin();
        boolean isMate = isSibling 
               && matchSpecies == modelState.getSpecies();
        
        // display mortar when against solid superblocks, even if not masonry
        boolean isSolid = blockState.isFullOpaque(world, pos) && blockState.getBlock() instanceof ISuperBlock;
        
        // no mortar between mates or non-solid superblocks
        if(isMate || !isSolid) return false;
        
        // always mortar if not a sibling
        if(!isSibling) return true;
        
        // between siblings, only mortar on three sides of cube
        // (other sibling will do the mortar on other sides)
        return(pos.getX() == origin.getX() + 1 
                || pos.getY() == origin.getY() - 1
                || pos.getZ() == origin.getZ() + 1);
    }
}
