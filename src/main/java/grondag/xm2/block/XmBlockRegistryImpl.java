package grondag.xm2.block;

import java.util.function.Function;

import grondag.xm2.Xm;
import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.state.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class XmBlockRegistryImpl {
	private XmBlockRegistryImpl() {}

	public static void register(
			Block block, 
			Function<BlockState, ModelState> defaultStateFunc, 
			WorldToModelStateFunction worldStateFunc,
			BlockTest blockJoinTest ) {
		
		for(BlockState blockState : block.getStateFactory().getStates()) {
			if(XmBlockState.get(blockState) != null) {
				//TODO: localize
				Xm.LOG.warn(String.format("[%s] BlockState %s already associated with an XmBlockState. Skipping." , Xm.MODID, blockState.toString()));
				return;
			}
			XmBlockStateImpl xmState = new XmBlockStateImpl(
					defaultStateFunc.apply(blockState),
					worldStateFunc,
					blockJoinTest,
					blockState);
			((XmBlockStateAccess)blockState).xm2_blockState(xmState);
		}
	}
	
	public static class XmBlockStateImpl implements XmBlockState {
		public final WorldToModelStateFunction worldStateFunc;
		public final BlockTest blockJoinTest;
		public final ModelState defaultModelState;
		public final BlockState blockState;
		
		private XmBlockStateImpl(
				ModelState defaultModelState,
				WorldToModelStateFunction worldStateFunc,
				BlockTest blockJoinTest,
				BlockState blockState ) {
			
			this.defaultModelState = defaultModelState;
			this.worldStateFunc = worldStateFunc;
			this.blockJoinTest = blockJoinTest;
			this.blockState = blockState;
		}

		@Override
		public BlockTest blockJoinTest() {
			return blockJoinTest;
		}

		@Override
		public ModelState defaultModelState() {
			return defaultModelState;
		}

		@Override
		public ModelState getModelState(BlockView world, BlockPos pos, boolean refreshFromWorld) {
			return worldStateFunc.apply(this, world, pos, refreshFromWorld);
		}

		@Override
		public BlockState blockState() {
			return blockState;
		}
	}
}
