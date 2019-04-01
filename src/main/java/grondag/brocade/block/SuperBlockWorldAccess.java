//TODO: remove after world state access sorted out

//package grondag.brocade.block;
//
//import grondag.brocade.Brocade;
//import grondag.fermion.world.PackedBlockPos;
//import net.minecraft.block.BlockState;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.BlockView;
//import net.minecraft.world.ExtendedBlockView;
//
//public class SuperBlockWorldAccess {
//    private static ThreadLocal<ISuperBlockAccess> localAccess = new ThreadLocal<ISuperBlockAccess>() {
//        @Override
//        protected ISuperBlockAccess initialValue() {
//            return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER ? new PassthruWrapper()
//                    : Brocade.proxy.clientWorldStateCache();
//        }
//    };
//
//    public static ISuperBlockAccess access(BlockView blockView) {
//        if (blockView instanceof ISuperBlockAccess)
//            return (ISuperBlockAccess) blockView;
//
//        ISuperBlockAccess result = localAccess.get();
//        if (result instanceof PassthruWrapper)
//            ((PassthruWrapper) result).wrap(blockView);
//
//        return result;
//    }
//
//    private static class PassthruWrapper implements ISuperBlockAccess {
//        private BlockView wrapped;
//
//        private void wrap(BlockView toWrap) {
//            wrapped = toWrap;
//        }
//
//        @Override
//        public BlockState getBlockState(BlockPos pos) {
//            return wrapped.getBlockState(pos);
//        }
//
//        @Override
//        public BlockState getBlockState(long packedBlockPos) {
//            return wrapped.getBlockState(PackedBlockPos.unpack(packedBlockPos));
//        }
//    }
//}
