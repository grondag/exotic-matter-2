package grondag.brocade.block;

import grondag.brocade.connect.api.world.BlockTest;
import grondag.brocade.connect.api.world.BlockTestContext;
import grondag.brocade.state.MeshState;
import net.minecraft.block.BlockState;

public class BrocadeBlockBorderMatch implements BlockTest {
    private BrocadeBlockBorderMatch() {}
    
    public static final BrocadeBlockBorderMatch INSTANCE = new BrocadeBlockBorderMatch();
    
    @Override
    public boolean apply(BlockTestContext context) {
        final MeshState fromState = (MeshState)context.fromModelState();
        final MeshState toState = (MeshState)context.toModelState();
        final BlockState toBlockState = context.toBlockState();
        final BlockState fromBlockState = context.fromBlockState();
        
        if(fromBlockState.getBlock() != toBlockState.getBlock() || fromState == null || toState == null) {
            return false;
        }
        
        return fromState.doShapeAndAppearanceMatch(toState) && fromState.getSpecies() == toState.getSpecies();
    }
}
