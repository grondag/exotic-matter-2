package grondag.brocade.mesh;

import static grondag.brocade.state.MeshStateData.STATE_FLAG_HAS_AXIS;
import static grondag.brocade.state.MeshStateData.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.brocade.state.MeshStateData.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.brocade.state.MeshStateData.STATE_FLAG_NEEDS_SPECIES;

import grondag.brocade.block.BrocadeBlock;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.painting.Surface;
import grondag.brocade.painting.SurfaceTopology;
import grondag.brocade.state.MeshState;
import grondag.brocade.state.StateFormat;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class AbstractWedgeMeshFactory extends MeshFactory {

    protected static final Surface BACK_AND_BOTTOM_SURFACE = Surface.builder(SurfaceTopology.CUBIC)
            .withDisabledLayers(PaintLayer.CUT, PaintLayer.LAMP).build();

    protected static final Surface SIDE_SURFACE = Surface.builder(BACK_AND_BOTTOM_SURFACE).withAllowBorders(false)
            .build();

    protected static final Surface TOP_SURFACE = Surface.builder(SIDE_SURFACE).withIgnoreDepthForRandomization(true)
            .build();

    public AbstractWedgeMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ROTATION | STATE_FLAG_HAS_AXIS_ORIENTATION);
    }

    @Override
    public boolean isCube(MeshState modelState) {
        return false;
    }

    @Override
    public boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis, BrocadeBlock block,
            MeshState modelState) {
        // not currently implemented - ambivalent about it
        return false;
    }

    @Override
    public int geometricSkyOcclusion(MeshState modelState) {
        return modelState.getAxis() == Direction.Axis.Y ? 7 : 255;
    }

    @Override
    public BlockOrientationType orientationType(MeshState modelState) {
        return BlockOrientationType.EDGE;
    }

    @Override
    public boolean isAxisOrthogonalToPlacementFace() {
        return true;
    }
}