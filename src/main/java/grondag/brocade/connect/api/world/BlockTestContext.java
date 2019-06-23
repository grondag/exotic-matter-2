package grondag.brocade.connect.api.world;

import grondag.fermion.shadow.jankson.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

//TODO: move this and others to separate world package - prevent conflict with earlier release
public interface BlockTestContext {
    BlockView world();
    
    BlockPos fromPos();
    BlockState fromBlockState();
    @Nullable Object fromModelState();

    BlockPos toPos();
    BlockState toBlockState();
    @Nullable Object toModelState();
}
