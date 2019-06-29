package grondag.brocade.mesh;

import static grondag.brocade.state.MeshStateData.STATE_FLAG_HAS_AXIS;
import static grondag.brocade.state.MeshStateData.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.brocade.state.MeshStateData.STATE_FLAG_NEEDS_SPECIES;

import java.util.function.Consumer;

import grondag.brocade.block.BrocadeBlock;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.painting.Surface;
import grondag.brocade.painting.SurfaceTopology;
import grondag.brocade.primitives.PolyTransform;
import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.IWritablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;
import grondag.brocade.state.MeshState;
import grondag.brocade.state.StateFormat;
import grondag.fermion.world.Rotation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class StackedPlatesMeshFactory extends MeshFactory {
    // This may not be the right setup - refactored surfaces at a time this wasn't
    // being actively used.
    protected static Surface TOP_AND_BOTTOM_SURFACE = Surface.builder(SurfaceTopology.CUBIC)
            .withEnabledLayers(PaintLayer.BASE, PaintLayer.MIDDLE, PaintLayer.OUTER).withAllowBorders(false).build();

    protected static Surface SIDE_SURFACE = Surface.builder(SurfaceTopology.CUBIC).withEnabledLayers(PaintLayer.CUT)
            .withAllowBorders(false).build();

    public StackedPlatesMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ORIENTATION);
    }

    private static final Direction[] HORIZONTAL_FACES = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
    
    @Override
    public void produceShapeQuads(MeshState modelState, Consumer<IPolygon> target) {
        final int meta = modelState.getMetaData();
        final PolyTransform transform = PolyTransform.get(modelState);
        final float height = (meta + 1) / 16;
        
        // PERF: if have a consumer and doing this dynamically - should consumer simply be a stream?
        // Why create a stream just to pipe it to the consumer?  Or cache the result.
        final IWritablePolyStream stream = PolyStreams.claimWritable();
        final IMutablePolygon writer = stream.writer();

        writer.setRotation(0, Rotation.ROTATE_NONE);
        writer.setLockUV(0, true);
        stream.saveDefaults();
        
        writer.setSurface(TOP_AND_BOTTOM_SURFACE);
        writer.setNominalFace(Direction.UP);
        writer.setupFaceQuad(0, 0, 1, 1, 1 - height, Direction.NORTH);
        transform.apply(writer);
        stream.append();

        for (Direction face : HORIZONTAL_FACES) {
            writer.setSurface(SIDE_SURFACE);
            writer.setNominalFace(face);
            writer.setupFaceQuad(0, 0, 1, height, 0, Direction.UP);
            transform.apply(writer);
            stream.append();
        }

        writer.setSurface(TOP_AND_BOTTOM_SURFACE);
        writer.setNominalFace(Direction.DOWN);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();

        if (stream.origin()) {
            IPolygon reader = stream.reader();

            do
                target.accept(reader);
            while (stream.next());
        }
        stream.release();
    }

    @Override
    public boolean isAdditive() {
        return true;
    }

    @Override
    public boolean isCube(MeshState modelState) {
        return modelState.getMetaData() == 15;
    }

    @Override
    public boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis, BrocadeBlock block,
            MeshState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(MeshState modelState) {
        return modelState.getAxis() == Direction.Axis.Y ? 255 : modelState.getMetaData();
    }

    @Override
    public BlockOrientationType orientationType(MeshState modelState) {
        return BlockOrientationType.FACE;
    }

    @Override
    public int getMetaData(MeshState modelState) {
        return (int) (modelState.getStaticShapeBits() & 0xF);
    }

    @Override
    public void setMetaData(MeshState modelState, int meta) {
        modelState.setStaticShapeBits(meta);
    }

    @Override
    public boolean hasLampSurface(MeshState modelState) {
        return false;
    }

}
