package grondag.hard_science.machines.impl.logistics;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.artbox.ArtBoxTextures;
import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.machines.support.MachineItemBlock;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResourcePredicate;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.storage.FluidContainer;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ModularTankBlock extends MachineBlock
{
    private final boolean dedicated;
    private final IResourcePredicate<StorageTypeFluid> predicate;
    private final int kLcapacity;
    
    public ModularTankBlock(String name, int kL, boolean dedicated, IResourcePredicate<StorageTypeFluid> predicate)
    {
        super(name, ModGui.MODULAR_TANK.ordinal(), MachineBlock.creatBasicMachineModelState(null, ArtBoxTextures.BORDER_CHANNEL_DOTS));
        this.kLcapacity = kL;
        this.dedicated = dedicated;
        this.predicate = predicate;
    }

    @Override
    public AbstractMachine createNewMachine()
    {
        TankMachine result = dedicated
                ? new TankMachine.Dedicated()
                : new TankMachine.Flexible();
        result.setCapacityInBlocks(this.kLcapacity);
        result.setContentPredicate(this.predicate);
        return result;
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
    public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) 
    {
        // allow fluid handling logic to happen
        if(!world.isRemote && player.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
        {
            TileEntity te = world.getTileEntity(pos);
            if(te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side))
            {
                IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
                if(FluidUtil.interactWithFluidHandler(player, hand, fluidHandler))
                    return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
    
    @Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        ItemStack result = super.getStackFromBlock(state, world, pos);
        
        // add lore for stacks generated on server
        if(result != null)
        {
            TileEntity blockTE = world.getTileEntity(pos);
            if (blockTE != null && blockTE instanceof MachineTileEntity) 
            {
                MachineTileEntity mste = (MachineTileEntity)blockTE;
                
                // client won't have the storage instance needed to do this
                if(mste.getWorld().isRemote) return result;
                
                FluidContainer store = mste.machine().fluidStorage();
                
                if(store.usedCapacity() == 0) return result;

                // save client-side display info
                NBTTagCompound displayTag = result.getOrCreateSubCompound("display");
                    
                NBTTagList loreTag = new NBTTagList(); 

                List<AbstractResourceWithQuantity<StorageTypeFluid>> items 
                        = store.find(store.storageType().MATCH_ANY);
                       
                if(!items.isEmpty())
                {
                    loreTag.appendTag(new NBTTagString(items.get(0).toString()));
                }
                displayTag.setTag("Lore", loreTag);
                    
                result.setItemDamage(Math.max(1, (int) (MachineItemBlock.MAX_DAMAGE * store.availableCapacity() / store.getCapacity())));
            }
        }
        return result;
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
