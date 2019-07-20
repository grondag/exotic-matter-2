package grondag.hard_science.machines.impl.building;

import grondag.artbox.ArtBoxTextures;
import grondag.exotic_matter.world.Rotation;
import grondag.hard_science.gui.control.machine.RadialGaugeSpec;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.matter.MatterColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;


public class BlockFabricatorTileEntity extends MachineTileEntityTickable
{
    @SideOnly(value = Side.CLIENT)
    public static final RadialGaugeSpec[] BASIC_BUILDER_GAUGE_SPECS = new RadialGaugeSpec[6];

    @SideOnly(value = Side.CLIENT)
    public static void initRenderSpecs()
    {
        BASIC_BUILDER_GAUGE_SPECS[0] = new RadialGaugeSpec(BlockFabricatorMachine.BUFFER_INDEX_TIO2, RenderBounds.BOUNDS_BOTTOM_1, 1.2, ArtBoxTextures.DECAL_DUST.getSampleSprite(), 0xFFFFFFFF, Rotation.ROTATE_NONE);
        BASIC_BUILDER_GAUGE_SPECS[1] = new RadialGaugeSpec(BlockFabricatorMachine.BUFFER_INDEX_RESIN_A, RenderBounds.BOUNDS_LEFT_UPPER, 1.2, ArtBoxTextures.DECAL_MIX.getSampleSprite(), MatterColors.RESIN_A, Rotation.ROTATE_NONE);
        BASIC_BUILDER_GAUGE_SPECS[2] = new RadialGaugeSpec(BlockFabricatorMachine.BUFFER_INDEX_RESIN_B, RenderBounds.BOUNDS_LEFT_MIDDLE, 1.2, ArtBoxTextures.DECAL_MIX.getSampleSprite(), MatterColors.RESIN_B, Rotation.ROTATE_180);
        BASIC_BUILDER_GAUGE_SPECS[3] = new RadialGaugeSpec(BlockFabricatorMachine.BUFFER_INDEX_FILLER, RenderBounds.BOUNDS_LEFT_LOWER, 1.2, ArtBoxTextures.DECAL_DUST.getSampleSprite(), MatterColors.DEPLETED_MINERAL_DUST, Rotation.ROTATE_NONE);
        BASIC_BUILDER_GAUGE_SPECS[4] = new RadialGaugeSpec(BlockFabricatorMachine.BUFFER_INDEX_NANOLIGHT, RenderBounds.BOUNDS_BOTTOM_2, 1.2, ArtBoxTextures.DECAL_STAR_16.getSampleSprite(), 0xFFFFFFFF, Rotation.ROTATE_NONE);
        BASIC_BUILDER_GAUGE_SPECS[5] = new RadialGaugeSpec(BlockFabricatorMachine.BUFFER_INDEX_HDPE, RenderBounds.BOUNDS_BOTTOM_3, 1.4, 
                ArtBoxTextures.DECAL_LARGE_DOT.getSampleSprite(), MatterColors.HDPE, Rotation.ROTATE_NONE,
                "C2H4", 0xFF000000);
    }


    @Override
    public IItemHandler getItemHandler()
    {
        if(this.world == null || this.world.isRemote) return null;
        if(this.machine() == null) return null;
        return this.machine().getBufferManager();
    }
   
}
