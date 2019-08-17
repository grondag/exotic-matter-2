package grondag.xm.api.collision;

import com.google.common.collect.ImmutableList;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.collision.CollisionDispatcherImpl;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class CollisionDispatcher {
    private CollisionDispatcher() {}
    
    public static ImmutableList<Box> boxesFor(ModelState modelState) {
        return CollisionDispatcherImpl.boxesFor(modelState);
    }

    public static VoxelShape shapeFor(ModelState modelState) {
        return CollisionDispatcherImpl.shapeFor(modelState);
    }
}
