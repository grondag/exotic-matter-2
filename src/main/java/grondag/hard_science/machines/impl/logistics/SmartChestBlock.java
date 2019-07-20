package grondag.hard_science.machines.impl.logistics;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.artbox.ArtBoxTextures;
import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.machines.base.MachineContainerBlock;
import grondag.hard_science.machines.base.MachineTileEntity;
import grondag.hard_science.machines.base.MachineTileEntityTickable;
import grondag.hard_science.machines.support.MachineItemBlock;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResourceWithQuantity;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SmartChestBlock extends MachineContainerBlock
{
    private final boolean dedicated;
    
    public SmartChestBlock(String name, boolean dedicated)
    {
        super(name, ModGui.SMART_CHEST.ordinal(), MachineBlock.creatBasicMachineModelState(ArtBoxTextures.DECAL_SKINNY_DIAGNAL_CROSS_BARS, ArtBoxTextures.BORDER_SINGLE_BOLD_LINE));
        this.dedicated = dedicated;
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) 
    {
        return new MachineTileEntityTickable();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite()
    {
        return ArtBoxTextures.DECAL_CHEST.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout()
    {
        return ModPortLayouts.utb_low_carrier_all;
    }
    
    @Override
    public AbstractMachine createNewMachine()
    {
        return dedicated 
                ? new SmartChestMachine.Dedicated()
                : new SmartChestMachine.Flexible();
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
                
                // if device is somehow missing nothing to do, and would cause NPE to continue
                if(mste.machine() == null) return result;
                
                IResourceContainer<StorageTypeStack> store = mste.machine().itemStorage();
                
                if(store.usedCapacity() == 0) return result;
                
                // save client-side display info
                NBTTagCompound displayTag = result.getOrCreateSubCompound("display");
                    
                NBTTagList loreTag = new NBTTagList(); 

                List<AbstractResourceWithQuantity<StorageTypeStack>> items 
                        = store.find(store.storageType().MATCH_ANY)
                        .stream()
                        .sorted(ItemResourceWithQuantity.SORT_BY_QTY_DESC).collect(Collectors.toList());

                if(!items.isEmpty())
                {
                    long printedQty = 0;
                    int printedCount = 0;
                    for(AbstractResourceWithQuantity<StorageTypeStack> item : items)
                    {
                        loreTag.appendTag(new NBTTagString(item.toString()));
                        printedQty += item.getQuantity();
                        if(++printedCount == 10)
                        {
                            //FIXME: localize
                            loreTag.appendTag(new NBTTagString(String.format("...plus %,d of %d other items", 
                                    store.usedCapacity() - printedQty, items.size() - printedCount)));
                            break;
                        }
                    }
                    
                    result.setItemDamage(Math.max(1, (int) (MachineItemBlock.MAX_DAMAGE * store.availableCapacity() / store.getCapacity())));
                }
                displayTag.setTag("Lore", loreTag);
            }
        }
        return result;
    }
}
