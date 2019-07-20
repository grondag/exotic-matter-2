package grondag.hard_science.machines.impl.processing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.artbox.ArtBoxTextures;
import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DigesterBlock extends MachineBlock
{
    public DigesterBlock(String name)
    {
        super(name, ModGui.DIGESTER.ordinal(), MachineBlock.creatBasicMachineModelState(null, ArtBoxTextures.BORDER_CAUTION));
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new DigesterMachine();
    }
    
    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta)
    {
        return new DigesterTileEntity();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return ArtBoxTextures.DECAL_BIG_DIAMOND.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
