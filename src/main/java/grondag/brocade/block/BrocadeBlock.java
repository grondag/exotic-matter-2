package grondag.brocade.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntegerProperty;

//TODO: mixin to block
public interface BrocadeBlock {
    public static final IntegerProperty HEAT = IntegerProperty.create("brocade_heat", 0, 15);
    public static final IntegerProperty SPECIES = IntegerProperty.create("brocade_species", 0, 15);
    public static final IntegerProperty HEIGHT = IntegerProperty.create("brocade_species", 0, 12);
    
    default boolean brocade_isTerrain(BlockState state) {
        return false;
    }
    
    default boolean brocade_isHot(BlockState state) {
        return false;
    }

    default boolean brocade_isTerrainFiller(BlockState state) {
        return false;
    }

    default boolean brocade_isTerrainHeight(BlockState state) {
        return false;
    }
}
