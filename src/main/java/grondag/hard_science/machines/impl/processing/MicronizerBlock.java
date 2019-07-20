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

public class MicronizerBlock extends MachineBlock
{
    public MicronizerBlock(String name)
    {
        super(name, ModGui.MICRONIZER.ordinal(), MachineBlock.creatBasicMachineModelState(null, ArtBoxTextures.BORDER_FILMSTRIP));
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        return new MicronizerMachine();
    }
    
    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta)
    {
        return new MicronizerTileEntity();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return ArtBoxTextures.DECAL_BUILDER.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
