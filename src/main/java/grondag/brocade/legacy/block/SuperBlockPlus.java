package grondag.brocade.legacy.block;




import grondag.brocade.model.render.RenderLayoutProducer;
import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.state.MetaUsage;
import grondag.fermion.varia.ItemHelper;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

/** base class for tile entity blocks */
public abstract class SuperBlockPlus extends SuperBlock implements ITileEntityProvider {
    /**
     * Prevent concurrency weirdness in
     * {@link #getTileEntityReliably(World, BlockPos)}
     */
    private static final Object TILE_ENTITY_AD_HOCK_CREATION_LOCK = new Object();

    public SuperBlockPlus(String blockName, Material defaultMaterial, ISuperModelState defaultModelState,
            RenderLayoutProducer renderLayout) {
        super(blockName, defaultMaterial, defaultModelState, renderLayout);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new SuperTileEntity();
    }

    @SuppressWarnings("null")
    public TileEntity getTileEntityReliably(World world, BlockPos pos) {
        TileEntity result = world.getTileEntity(pos);
        if (result == null) {
            synchronized (TILE_ENTITY_AD_HOCK_CREATION_LOCK) {
                result = world.getTileEntity(pos);
                if (result == null) {
                    result = createNewTileEntity(world, 0);
                    world.setTileEntity(pos, result);
                }
            }
        }
        return result;
    }

    @Override
    public ISuperModelState computeModelState(BlockState state, BlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded) {
        BlockEntity myTE = world.getBlockEntity(pos);
        if (myTE != null && myTE instanceof SuperTileEntity) {
            BlockState currentState = world.getBlockState(pos);
            ISuperModelState result = ((SuperTileEntity) myTE).getModelState(currentState, world, pos,
                    refreshFromWorldIfNeeded);

            // honor passed in species if different
            if (currentState.getValue(META) != state.getValue(META) && result.metaUsage() != MetaUsage.NONE) {
                result = result.clone();
                result.setMetaData(state.getValue(META));
            }
            return result;
        } else {
            return super.computeModelState(state, world, pos, refreshFromWorldIfNeeded);
        }
    }

    /**
     * Use when absolutely certain given block state is current.
     */
    @Override
    public ISuperModelState getModelStateAssumeStateIsCurrent(BlockState state, BlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded) {
        if (state instanceof IExtendedBlockState) {
            ISuperModelState result = ((IExtendedBlockState) state).getValue(ISuperBlock.MODEL_STATE);
            if (result != null)
                return result;
        }

        TileEntity myTE = world.getTileEntity(pos);
        if (myTE != null && myTE instanceof SuperTileEntity) {
            return ((SuperTileEntity) myTE).getModelState(state, world, pos, refreshFromWorldIfNeeded);

        } else {
            return computeModelState(state, world, pos, refreshFromWorldIfNeeded);
        }
    }

    @Override
    public ItemStack getStackFromBlock(BlockState state, ExtendedBlockView world, BlockPos pos) {
        BlockState currentState = world.getBlockState(pos);

        ItemStack stack = super.getStackFromBlock(currentState, world, pos);

        if (!stack.isEmpty()) {
            TileEntity blockTE = world.getTileEntity(pos);
            if (blockTE != null && blockTE instanceof SuperTileEntity) {
                // force refresh of TE state before persisting in stack
                ((SuperTileEntity) blockTE).getModelState(currentState, world, pos, true);
                blockTE.writeToNBT(ItemHelper.getOrCreateStackTag(stack));
            }
        }
        return stack;
    }

    /**
     * Need to destroy block here because did not do it during removedByPlayer.
     */
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, BlockState state,
            TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * 
     * SuperModelBlock: Defer destruction of block until after drops when harvesting
     * so can gather NBT from tile entity.
     */
    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos,
            EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /**
     * Note this does not send an update to client. At this time, isn't needed
     * because model state is always set on server immediately after block state is
     * set, in the same thread, and so tile entity data always goes along with block
     * state packet.
     */
    public void setModelState(World world, BlockPos pos, ISuperModelState modelState) {
        TileEntity blockTE = world.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof SuperTileEntity) {
            ((SuperTileEntity) blockTE).setModelState(modelState);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state,
            LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        // restore TE state from stack
        // on client side, stack state may not include all elements (like storage)
        TileEntity blockTE = worldIn.getTileEntity(pos);
        if (blockTE != null && blockTE instanceof SuperTileEntity) {
            ((SuperTileEntity) blockTE).readModNBT(ItemHelper.getOrCreateStackTag(stack));
        }
    }

}
