package grondag.xm2.api.model;

import grondag.xm2.api.surface.XmSurfaceList;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface MutablePrimitiveModelState extends ModelPrimitiveState, MutableModelState {
    /**
     * Also resets shape-specific bits to default for the given shape. Does nothing
     * if shape is the same as existing.
     */
    void primitive(ModelPrimitive shape);
    
    @Override
    MutablePrimitiveModelState refreshFromWorld(XmBlockStateImpl state, BlockView world, BlockPos pos);
    
    @Override
    default void paintAll(int paintIndex) {
        XmSurfaceList slist = primitive().surfaces();
        final int limit = slist.size();
        for (int i = 0; i < limit; i++) {
            paint(i, paintIndex);
        }
    }
}
