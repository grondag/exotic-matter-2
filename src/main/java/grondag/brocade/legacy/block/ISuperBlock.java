package grondag.brocade.legacy.block;

import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.world.IBlockTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.ExtendedBlockView;

public interface ISuperBlock {
    public static final IntProperty SPECIES = IntProperty.of("brocade_species", 0, 15);
    
    String getItemStackDisplayName(ItemStack stack);

    /**
     * Factory for block test that should be used for border/shape joins for this
     * block. Used in model state refresh from world.
     */
    IBlockTest blockJoinTest(BlockView worldIn, BlockState state, BlockPos pos, ISuperModelState modelState);

    /**
     * Returns an instance of the default model state for this block. Because model
     * states are mutable, every call returns a new instance.
     */
    ISuperModelState getDefaultModelState();

    /**
     * If last parameter is false, does not perform a refresh from world for
     * world-dependent state attributes. Use this option to prevent infinite
     * recursion when need to reference some static state ) information in order to
     * determine dynamic world state. Block tests are main use case for false.
     * 
     * 
     */
    ISuperModelState getModelState(BlockView world, BlockPos pos, boolean refreshFromWorldIfNeeded);

    /**
     * Use when absolutely certain given block state is current.
     */
    ISuperModelState getModelStateAssumeStateIsCurrent(BlockState state, BlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded);

    /**
     * Returns model state without caching the value in any way. May use a cached
     * value to satisfy the request.
     * <p>
     * 
     * At least one vanilla routine passes in a block state that does not match
     * world. (After block updates, passes in previous state to detect collision box
     * changes.) <br>
     * <br>
     * 
     * We don't want to update our current state based on stale block state, so for
     * TE blocks the refresh must be coded so we don't inject bad (stale) modelState
     * into TE. <br>
     * <br>
     * 
     * However, we do want to honor the given world state if species is different
     * than current. We do this by directly changing species, because that is only
     * thing that can changed in model state based on block state, and also affects
     * collision box. <br>
     * <br>
     * 
     * NOTE: there is probably still a bug here, because collision box can change
     * based on other components of model state (orthogonalAxis, for example) and
     * those changes may not be detected by path finding.
     */
    ISuperModelState computeModelState(BlockState state, BlockView world, BlockPos pos,
            boolean refreshFromWorldIfNeeded);

    int getOcclusionKey(BlockState state, BlockView world, BlockPos pos, Direction side);

    /**
     * True if this is an instance of an IFlowBlock and also a filler block. Avoids
     * performance hit of casting to the IFlowBlock Interface. (Based on performance
     * profile results.)
     */
    boolean isFlowFiller();

    /**
     * True if this is an instance of an IFlowBlock and also a height block. Avoids
     * performance hit of casting to the IFlowBlock Interface. (Based on performance
     * profile results.)
     */
    boolean isFlowHeight();

    /**
     * Only true for virtual blocks. Avoids "instanceof" checking.
     */
    default boolean isVirtual() {
        return false;
    }

    /**
     * True if block at the given position is actually solid (not replaceable) or is
     * virtual and visible to the given player.
     */
    default boolean isVirtuallySolid(BlockPos pos, PlayerEntity player) {
        return !((Block) this).getMaterial(player.world.getBlockState(pos)).isReplaceable();
    }

    /**
     * Convenience call for {@link #isVirtual()} when you don't know what type of
     * block it is. Will return false for any block that doesn't implement
     * ISuperBlock.
     */
    public static boolean isVirtualBlock(Block block) {
        return block instanceof ISuperBlock && ((ISuperBlock) block).isVirtual();
    }

    /**
     * Convenience call for
     * {@link #isVirtuallySolid(ExtendedBlockView, BlockPos, EntityPlayer)} when you
     * don't know what type of block it is. Will return false for any block that
     * doesn't implement ISuperBlock.
     */
    public static boolean isVirtuallySolidBlock(BlockPos pos, PlayerEntity player) {
        return isVirtuallySolidBlock(player.world.getBlockState(pos), pos, player);
    }

    /**
     * Convenience call for
     * {@link #isVirtuallySolid(ExtendedBlockView, BlockPos, EntityPlayer)} when you
     * don't know what type of block it is. Will return negation of
     * {@link #isReplaceable(ExtendedBlockView, BlockPos)} for any block that doesn't
     * implement ISuperBlock.
     * 
     * UGLY: really needed?  Seems redundant of isVirtuallySolid, plus have mixins now
     */
    public static boolean isVirtuallySolidBlock(BlockState state, BlockPos pos, PlayerEntity player) {
        Block block = state.getBlock();
        return isVirtualBlock(block) ? ((ISuperBlock) block).isVirtuallySolid(pos, player)
                : !block.getMaterial(state).isReplaceable();
    }
}