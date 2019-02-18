package grondag.brocade.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.model.render.RenderLayoutProducer;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.MetaUsage;
import grondag.exotic_matter.varia.ItemHelper;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

/** base class for tile entity blocks */
public abstract class SuperBlockPlus extends SuperBlock implements ITileEntityProvider
{
    /**
     * Prevent concurrency weirdness in {@link #getTileEntityReliably(World, BlockPos)}
     */
    private static final Object TILE_ENTITY_AD_HOCK_CREATION_LOCK = new Object();
    
    public SuperBlockPlus(String blockName, Material defaultMaterial, ISuperModelState defaultModelState, @Nullable RenderLayoutProducer renderLayout)
    {
        super(blockName, defaultMaterial, defaultModelState, renderLayout);
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta)
    {
        return new SuperTileEntity();
    }

    @SuppressWarnings("null")
    public TileEntity getTileEntityReliably (World world, BlockPos pos)
    {
        TileEntity result = world.getTileEntity(pos);
        if (result == null) 
        {
            synchronized(TILE_ENTITY_AD_HOCK_CREATION_LOCK)
            {
                result = world.getTileEntity(pos);
                if (result == null) 
                {
                    result = createNewTileEntity(world, 0);
                    world.setTileEntity(pos, result);
                }
            }
        }
        return result;
    }
    
    @Override
    public ISuperModelState computeModelState(IBlockState state, ISuperBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        TileEntity myTE = world.getTileEntity(pos);
        if(myTE != null && myTE instanceof SuperTileEntity) 
        {
            IBlockState currentState = world.getBlockState(pos);
            ISuperModelState result = ((SuperTileEntity)myTE).getModelState(currentState, world, pos, refreshFromWorldIfNeeded);
            
            // honor passed in species if different
            if(currentState.getValue(META) != state.getValue(META) && result.metaUsage() != MetaUsage.NONE)
            {
                result = result.clone();
                result.setMetaData(state.getValue(META));
            }
            return result;
        }
        else
        {
            return super.computeModelState(state, world, pos, refreshFromWorldIfNeeded);
        }
    }
    
    /** 
     * Use when absolutely certain given block state is current.
     */
    @Override
    public ISuperModelState getModelStateAssumeStateIsCurrent(IBlockState state, ISuperBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded)
    {
        if(state instanceof IExtendedBlockState)
        {
            ISuperModelState result = ((IExtendedBlockState)state).getValue(ISuperBlock.MODEL_STATE);
            if(result != null) 
                return result;
        }
        
        TileEntity myTE = world.getTileEntity(pos);
        if(myTE != null && myTE instanceof SuperTileEntity) 
        {
            return ((SuperTileEntity)myTE).getModelState(state, world, pos, refreshFromWorldIfNeeded);
            
        }
        else
        {
            return computeModelState(state, world, pos, refreshFromWorldIfNeeded);
        }
    }
    
    @Override
    public ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        IBlockState currentState = world.getBlockState(pos);
        
        ItemStack stack = super.getStackFromBlock(currentState, world, pos);
        
        if(!stack.isEmpty())
        {
            TileEntity blockTE = world.getTileEntity(pos);
            if (blockTE != null && blockTE instanceof SuperTileEntity) 
            {
                // force refresh of TE state before persisting in stack
                ((SuperTileEntity)blockTE).getModelState(currentState, world, pos, true);
                blockTE.writeToNBT(ItemHelper.getOrCreateStackTag(stack));
            }
        }
        return stack;
    }
    
    /**
     * Need to destroy block here because did not do it during removedByPlayer.
     */
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) 
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }
    
    
    /**
     * {@inheritDoc} <br><br>
     * 
     * SuperModelBlock: Defer destruction of block until after drops when harvesting so can gather NBT from tile entity.
     */
    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) 
    {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    /**
     * Note this does not send an update to client.
     * At this time, isn't needed because model state is always
     * set on server immediately after block state is set, in the
     * same thread, and so tile entity data always goes along with 
     * block state packet.
     */
    public void setModelState(World world, BlockPos pos, ISuperModelState modelState)
    {
        TileEntity blockTE = world.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof SuperTileEntity) 
        {
            ((SuperTileEntity)blockTE).setModelState(modelState);
        }
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        
        // restore TE state from stack
        // on client side, stack state may not include all elements (like storage) 
        TileEntity blockTE = worldIn.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof SuperTileEntity) 
        {
            ((SuperTileEntity)blockTE).readModNBT(ItemHelper.getOrCreateStackTag(stack));
        }
    }
    
}
