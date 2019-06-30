package grondag.xm2.terrain;

import grondag.xm2.block.XmSimpleBlock;
import grondag.xm2.state.ModelState;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;

public class TerrainBlock extends XmSimpleBlock implements IHotBlock {
    public static final IntProperty HEAT = IntProperty.of("xm2_heat", 0, 15);
    public static final EnumProperty<TerrainType> TERRAIN_TYPE = EnumProperty.of("xm2_terrain", TerrainType.class);
    
    public TerrainBlock(Settings blockSettings, ModelState defaultModelState) {
        super(blockSettings, defaultModelState);
    }

}
