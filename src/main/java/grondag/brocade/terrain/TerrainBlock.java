package grondag.brocade.terrain;

import grondag.brocade.block.SimpleBrocadeBlock;
import grondag.brocade.state.MeshState;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;

public class TerrainBlock extends SimpleBrocadeBlock implements IHotBlock {
    public static final IntProperty HEAT = IntProperty.of("brocade_heat", 0, 15);
    public static final EnumProperty<TerrainType> TERRAIN_TYPE = EnumProperty.of("brocade_terrain", TerrainType.class);
    
    public TerrainBlock(Settings blockSettings, MeshState defaultModelState) {
        super(blockSettings, defaultModelState);
    }

}
