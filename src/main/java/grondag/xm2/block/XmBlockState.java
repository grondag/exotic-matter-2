package grondag.xm2.block;

import javax.annotation.Nullable;

import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.state.ModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

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
}