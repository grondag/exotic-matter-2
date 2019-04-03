package grondag.brocade.world;



import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface IExtraStateFactory {
    public static final IExtraStateFactory NONE = new IExtraStateFactory() {
        @Override
        public ISuperModelState get(BlockView worldIn, BlockPos pos, BlockState state) {
            return null;
        }
    };

    public ISuperModelState get(BlockView worldIn, BlockPos pos, BlockState state);
}
