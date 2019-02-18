package grondag.brocade.block;

import grondag.exotic_matter.ExoticMatter;
import grondag.exotic_matter.world.PackedBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class SuperBlockWorldAccess
{
    private static ThreadLocal<ISuperBlockAccess> localAccess = new ThreadLocal<ISuperBlockAccess>()
    {
        @Override
        protected ISuperBlockAccess initialValue()
        {
            return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                    ? new PassthruWrapper()
                    : ExoticMatter.proxy.clientWorldStateCache();
        }
    };
    
    public static ISuperBlockAccess access(IBlockAccess access)
    {
        if(access instanceof ISuperBlockAccess)
            return (ISuperBlockAccess)access;
        
        ISuperBlockAccess result = localAccess.get();
        if(result instanceof PassthruWrapper)
            ((PassthruWrapper)result).wrap(access);
        
        return result;
    }
    
    private static class PassthruWrapper implements ISuperBlockAccess
    {
        @SuppressWarnings("null")
        private IBlockAccess wrapped;
        
        private void wrap(IBlockAccess toWrap)
        {
            wrapped = toWrap;
        }
        
        @Override
        public IBlockState getBlockState(BlockPos pos)
        {
            return wrapped.getBlockState(pos);
        }

        @Override
        public IBlockAccess wrapped()
        {
            return wrapped;
        }

        @Override
        public IBlockState getBlockState(long packedBlockPos)
        {
            return wrapped.getBlockState(PackedBlockPos.unpack(packedBlockPos));
        }
    }
}
