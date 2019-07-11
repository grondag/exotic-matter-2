package grondag.xm2.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;

public interface VirtualBlock {
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
        return block instanceof VirtualBlock;
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
        return isVirtualBlock(block) ? ((VirtualBlock) block).isVirtuallySolid(pos, player)
                : !block.getMaterial(state).isReplaceable();
    }
}