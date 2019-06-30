package grondag.xm2.block;

import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.connect.api.world.BlockTestContext;
import grondag.xm2.state.ModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

// For masonry, true result means border IS present
public class XmMasonryMatch implements BlockTest {
    private XmMasonryMatch() {}
    
    public static final XmMasonryMatch INSTANCE = new XmMasonryMatch();
    
    @Override
    public boolean apply(BlockTestContext context) {
        
        if(context.fromModelState() == null) {
            return false;
        }
        
        final ModelState fromState = (ModelState)context.fromModelState();
        final ModelState toState = (ModelState)context.toModelState();
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
