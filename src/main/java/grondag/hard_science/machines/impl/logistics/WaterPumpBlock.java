package grondag.hard_science.machines.impl.logistics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.artbox.ArtBoxTextures;
import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** WIP */
public class WaterPumpBlock extends MachineBlock
{
    public WaterPumpBlock(String name)
    {
        super(name, ModGui.MODULAR_TANK.ordinal(), MachineBlock.creatBasicMachineModelState(null, ArtBoxTextures.BORDER_CHANNEL_DOTS));
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new WaterPumpMachine();
    }
    
    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta)
    {
        return new MachineTileEntityTickable();
    }
    
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return ArtBoxTextures.DECAL_DRIP.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
