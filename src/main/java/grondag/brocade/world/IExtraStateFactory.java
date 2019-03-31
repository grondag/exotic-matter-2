package grondag.brocade.world;



import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IExtraStateFactory {
    public static final IExtraStateFactory NONE = new IExtraStateFactory() {
        @Override
        public ISuperModelState get(IBlockAccess worldIn, BlockPos pos, BlockState state) {
            return null;
        }
    };

    public ISuperModelState get(IBlockAccess worldIn, BlockPos pos, BlockState state);
}
