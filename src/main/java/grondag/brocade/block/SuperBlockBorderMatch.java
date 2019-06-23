package grondag.brocade.block;

import grondag.brocade.connect.api.world.BlockTest;
import grondag.brocade.connect.api.world.BlockTestContext;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;

public class SuperBlockBorderMatch implements BlockTest {
    private SuperBlockBorderMatch() {}
    
    public static final SuperBlockBorderMatch INSTANCE = new SuperBlockBorderMatch();
    
    @Override
    public boolean apply(BlockTestContext context) {
        final ISuperModelState fromState = (ISuperModelState)context.fromModelState();
        final ISuperModelState toState = (ISuperModelState)context.toModelState();
        final BlockState toBlockState = context.toBlockState();
        final BlockState fromBlockState = context.fromBlockState();
        
        if(fromBlockState.getBlock() != toBlockState.getBlock() || fromState == null || toState == null) {
            return false;
        }
        
        return fromState.doShapeAndAppearanceMatch(toState) && fromState.getSpecies() == toState.getSpecies();
    }
}
