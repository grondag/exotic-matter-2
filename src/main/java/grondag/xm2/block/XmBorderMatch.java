package grondag.xm2.block;

import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.connect.api.world.BlockTestContext;
import grondag.xm2.state.ModelState;
import net.minecraft.block.BlockState;

public class XmBorderMatch implements BlockTest {
    private XmBorderMatch() {}
    
    public static final XmBorderMatch INSTANCE = new XmBorderMatch();
    
    @Override
    public boolean apply(BlockTestContext context) {
        final ModelState fromState = (ModelState)context.fromModelState();
        final ModelState toState = (ModelState)context.toModelState();
        final BlockState toBlockState = context.toBlockState();
        final BlockState fromBlockState = context.fromBlockState();
        
        if(fromBlockState.getBlock() != toBlockState.getBlock() || fromState == null || toState == null) {
            return false;
        }
        
        return fromState.doShapeAndAppearanceMatch(toState) && fromState.getSpecies() == toState.getSpecies();
    }
}
