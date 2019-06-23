package grondag.brocade.block;

import grondag.brocade.connect.api.world.BlockTest;
import grondag.brocade.connect.api.world.BlockTestContext;
import grondag.brocade.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

// For masonry, true result means border IS present
public class SuperBlockMasonryMatch implements BlockTest {
    private SuperBlockMasonryMatch() {}
    
    public static final SuperBlockMasonryMatch INSTANCE = new SuperBlockMasonryMatch();
    
    @Override
    public boolean apply(BlockTestContext context) {
        
        if(context.fromModelState() == null) {
            return false;
        }
        
        final ISuperModelState fromState = (ISuperModelState)context.fromModelState();
        final ISuperModelState toState = (ISuperModelState)context.toModelState();
        final BlockState toBlockState = context.toBlockState();
        final BlockState fromBlockState = context.fromBlockState();
        final BlockPos toPos = context.toPos();
        
        // if not a sibling, mortar if against full opaque
        if(fromBlockState.getBlock() != toBlockState.getBlock() || toState == null) {
            return toBlockState.isFullOpaque(context.world(), toPos);
        }
        
        // no mortar between siblings with same species
        if(fromState.getSpecies() == toState.getSpecies()) {
            return false;
        };
        
        final BlockPos fromPos = context.fromPos();
        
        // between siblings, only mortar on three sides of cube
        // (other sibling will do the mortar on other sides)
        return(toPos.getX() == fromPos.getX() + 1 
                || toPos.getY() == fromPos.getY() - 1
                || toPos.getZ() == fromPos.getZ() + 1);
    }
}
