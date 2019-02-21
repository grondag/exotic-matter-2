package grondag.brocade.block;

import grondag.brocade.Brocade;
import grondag.fermion.world.PackedBlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.world.ExtendedBlockView;

public class SuperBlockWorldAccess {
    private static ThreadLocal<ISuperBlockAccess> localAccess = new ThreadLocal<ISuperBlockAccess>() {
        @Override
        protected ISuperBlockAccess initialValue() {
            return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER ? new PassthruWrapper()
                    : ExoticMatter.proxy.clientWorldStateCache();
        }
    };

    public static ISuperBlockAccess access(ExtendedBlockView access) {
        if (access instanceof ISuperBlockAccess)
            return (ISuperBlockAccess) access;

        ISuperBlockAccess result = localAccess.get();
        if (result instanceof PassthruWrapper)
            ((PassthruWrapper) result).wrap(access);

        return result;
    }

    private static class PassthruWrapper implements ISuperBlockAccess {
        @SuppressWarnings("null")
        private ExtendedBlockView wrapped;

        private void wrap(ExtendedBlockView toWrap) {
            wrapped = toWrap;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return wrapped.getBlockState(pos);
        }

        @Override
        public ExtendedBlockView wrapped() {
            return wrapped;
        }

        @Override
        public BlockState getBlockState(long packedBlockPos) {
            return wrapped.getBlockState(PackedBlockPos.unpack(packedBlockPos));
        }
    }
}
