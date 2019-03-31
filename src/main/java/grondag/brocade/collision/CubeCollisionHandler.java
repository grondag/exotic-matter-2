package grondag.brocade.collision;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.util.math.BoundingBox;

public class CubeCollisionHandler implements ICollisionHandler {

    public static CubeCollisionHandler INSTANCE = new CubeCollisionHandler();
    
    private static final List<BoundingBox> FULL_BLOCK_AABB_LIST = ImmutableList.of(FULL_BLOCK_BOX);

    @Override
    public List<BoundingBox> getCollisionBoxes(ISuperModelState modelState) {
        return FULL_BLOCK_AABB_LIST;
    }

    @Override
    public BoundingBox getCollisionBoundingBox(ISuperModelState modelState) {
        return FULL_BLOCK_BOX;
    }

    @Override
    public BoundingBox getRenderBoundingBox(ISuperModelState modelState) {
        return FULL_BLOCK_BOX;
    }
}