package grondag.brocade.mesh;

import static grondag.brocade.model.state.ModelStateData.STATE_FLAG_HAS_AXIS;
import static grondag.brocade.model.state.ModelStateData.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.brocade.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;

import java.util.List;



import grondag.brocade.block.ISuperBlock;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.painting.Surface;
import grondag.brocade.painting.SurfaceTopology;
import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.state.StateFormat;
import grondag.brocade.collision.CollisionBoxDispatcher;
import grondag.brocade.collision.ICollisionHandler;
import grondag.brocade.model.varia.SideShape;
import grondag.brocade.world.BlockCorner;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractWedgeMeshFactory extends ShapeMeshGenerator implements ICollisionHandler {

    protected static final Surface BACK_AND_BOTTOM_SURFACE = Surface.builder(SurfaceTopology.CUBIC)
            .withDisabledLayers(PaintLayer.CUT, PaintLayer.LAMP).build();

    protected static final Surface SIDE_SURFACE = Surface.builder(BACK_AND_BOTTOM_SURFACE).withAllowBorders(false)
            .build();

    protected static final Surface TOP_SURFACE = Surface.builder(SIDE_SURFACE).withIgnoreDepthForRandomization(true)
            .build();

    public AbstractWedgeMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ROTATION);
    }

    @Override
    public boolean isCube(ISuperModelState modelState) {
        return false;
    }

    @Override
    public boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis, ISuperBlock block,
            ISuperModelState modelState) {
        // not currently implemented - ambivalent about it
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState) {
        return modelState.getAxis() == Direction.Axis.Y ? 7 : 255;
    }

    @Override
    public BlockOrientationType orientationType(ISuperModelState modelState) {
        return BlockOrientationType.EDGE;
    }

    @Override
    public ICollisionHandler collisionHandler() {
        return this;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, Direction side) {
        BlockCorner corner = BlockCorner.find(modelState.getAxis(), modelState.getAxisRotation());
        return side == corner.face1 || side == corner.face2 ? SideShape.SOLID : SideShape.MISSING;
    }

    @Override
    public List<BoundingBox> getCollisionBoxes(ISuperModelState modelState) {
        return CollisionBoxDispatcher.getCollisionBoxes(modelState);
    }

    @Override
    public BoundingBox getCollisionBoundingBox(ISuperModelState modelState) {
        return ICollisionHandler.FULL_BLOCK_BOX;
    }

    @Override
    public BoundingBox getRenderBoundingBox(ISuperModelState modelState) {
        return ICollisionHandler.FULL_BLOCK_BOX;
    }

    @Override
    public boolean isAxisOrthogonalToPlacementFace() {
        return true;
    }

}