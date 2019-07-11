package grondag.xm2.block.wip;

import javax.annotation.Nullable;

import grondag.xm2.block.wip.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.state.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface XmBlockStateAccess {
	
	void xm2_blockState(XmBlockStateImpl state);
	
	XmBlockStateImpl xm2_blockState();
	
	static @Nullable XmBlockStateImpl get(BlockState fromState) {
		return ((XmBlockStateAccess)fromState).xm2_blockState();
	}
	
	static @Nullable XmBlockStateImpl get(Block fromBlock) {
		return get(fromBlock.getDefaultState());
	}
	
	static @Nullable ModelState modelState(BlockState fromState, BlockView blockView, BlockPos pos, boolean refresh) {
		final XmBlockStateImpl xmState = get(fromState);
		return xmState == null ? null : xmState.getModelState(blockView, pos, refresh);
	}
}
