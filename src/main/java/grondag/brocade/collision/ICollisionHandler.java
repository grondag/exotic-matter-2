package grondag.brocade.collision;

import java.util.List;
import java.util.stream.Collectors;

import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BoundingBox;

public interface ICollisionHandler {
    public default List<BoundingBox> getCollisionBoxes(ISuperModelState modelState, BlockPos offset) {
        return getCollisionBoxes(modelState).stream().map(aabb -> aabb.offset(offset)).collect(Collectors.toList());
    }

    public default List<BoundingBox> getCollisionBoxes(ISuperModelState modelState) {
        return CollisionBoxDispatcher.getCollisionBoxes(modelState);
    }

    public default BoundingBox getCollisionBoundingBox(ISuperModelState modelState) {
        return Block.FULL_BLOCK_AABB;
    }

    public default BoundingBox getRenderBoundingBox(ISuperModelState modelState) {
        return Block.FULL_BLOCK_AABB;
    }

}
