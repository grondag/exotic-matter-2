package grondag.xm2.mixin;

import org.spongepowered.asm.mixin.Mixin;

import grondag.xm2.block.wip.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.block.wip.XmBlockStateAccess;
import net.minecraft.block.BlockState;

@Mixin(BlockState.class)
public abstract class MixinBlockState implements XmBlockStateAccess {
	private XmBlockStateImpl xmBlockState;
	
	@Override
	public void xm2_blockState(XmBlockStateImpl state) {
		xmBlockState = state;
	}

	@Override
	public XmBlockStateImpl xm2_blockState() {
		return xmBlockState;
	}
}
