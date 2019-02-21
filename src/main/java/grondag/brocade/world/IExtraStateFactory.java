package grondag.brocade.world;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.state.ISuperModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IExtraStateFactory {
    public static final IExtraStateFactory NONE = new IExtraStateFactory() {
        @Override
        public @Nullable ISuperModelState get(IBlockAccess worldIn, BlockPos pos, IBlockState state) {
            return null;
        }
    };

    public @Nullable ISuperModelState get(IBlockAccess worldIn, BlockPos pos, IBlockState state);
}
