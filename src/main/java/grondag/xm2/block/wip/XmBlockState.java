package grondag.xm2.block.wip;

import javax.annotation.Nullable;

import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.state.ModelState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ExtendedBlockView;

public interface XmBlockState {
    
	static @Nullable XmBlockState get(BlockState fromState) {
		return XmBlockStateAccess.get(fromState);
	}
	
	/**
	 * Minecraft block state associated with this Exotic Matter block state.
	 * Association is always 1:1.
	 */
	BlockState blockState();
	
    /**
     * Block test that should be used for border/shape joins for this
     * block. Used in model state refresh from world.
     */
    BlockTest blockJoinTest();

    /**
     * Returns an instance of the default model state for this block. Because model
     * states are mutable, every call returns a new instance.
     */
    ModelState defaultModelState();

    /**
     * If last parameter is false, does not perform a refresh from world for
     * world-dependent state attributes. Use this option to prevent infinite
     * recursion when need to reference some static state ) information in order to
     * determine dynamic world state. Block tests are main use case for false.
    */
    ModelState getModelState(BlockView world, BlockPos pos, boolean refreshFromWorld);

    /**
     * True if this is an instance of an IFlowBlock and also a filler block. Avoids
     * performance hit of casting to the IFlowBlock Interface. (Based on performance
     * profile results.)
     */
    default boolean isFlowFiller() {
    	return false;
    };

    /**
     * True if this is an instance of an IFlowBlock and also a height block. Avoids
     * performance hit of casting to the IFlowBlock Interface. (Based on performance
     * profile results.)
     */
    default boolean isFlowHeight() {
    	return false;
    }

    /**
     * Only true for virtual blocks. Avoids "instanceof" checking.
     */
    default boolean isVirtual() {
        return false;
    }

    /**
     * True if block at the given position is actually solid (not replaceable) or is
     * virtual and visible to the given player.
     * UGLY: why not use isReplaceable directly?
     */
    default boolean isVirtuallySolid(BlockPos pos, PlayerEntity player) {
        return !((BlockState)this).getMaterial().isReplaceable();
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
    	XmBlockState xmState = (XmBlockState)state;
        return xmState.isVirtual() ? xmState.isVirtuallySolid(pos, player)
                : !state.getBlock().getMaterial(state).isReplaceable();
    }
}