package grondag.brocade.mesh;

import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_HAS_AXIS;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.collision.ICollisionHandler;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.painting.SurfaceTopology;
import grondag.exotic_matter.model.primitives.PolyFactory;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.StateFormat;
import grondag.exotic_matter.model.varia.SideShape;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StackedPlatesMeshFactory extends ShapeMeshGenerator implements ICollisionHandler {
    // This may not be the right setup - refactored surfaces at a time this wasn't
    // being actively used.
    protected static Surface TOP_AND_BOTTOM_SURFACE = Surface.builder(SurfaceTopology.CUBIC)
            .withEnabledLayers(PaintLayer.BASE, PaintLayer.MIDDLE, PaintLayer.OUTER).withAllowBorders(false).build();

    protected static Surface SIDE_SURFACE = Surface.builder(SurfaceTopology.CUBIC).withEnabledLayers(PaintLayer.CUT)
            .withAllowBorders(false).build();

    public StackedPlatesMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ORIENTATION);
    }

    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target) {
        final int meta = modelState.getMetaData();
        final Matrix4f matrix = modelState.getMatrix4f();
        final float height = (meta + 1) / 16;

        IMutablePolygon template = PolyFactory.COMMON_POOL.newPaintable(4);
        template.setRotation(0, Rotation.ROTATE_NONE);
        template.setLockUV(0, true);

        IMutablePolygon quad = template.claimCopy(4);
        quad.setSurface(TOP_AND_BOTTOM_SURFACE);
        quad.setNominalFace(EnumFacing.UP);
        quad.setupFaceQuad(0, 0, 1, 1, 1 - height, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        for (EnumFacing face : EnumFacing.Plane.HORIZONTAL.facings()) {
            quad = template.claimCopy(4);
            quad.setSurface(SIDE_SURFACE);
            quad.setNominalFace(face);
            quad.setupFaceQuad(0, 0, 1, height, 0, EnumFacing.UP);
            quad.transform(matrix);
            target.accept(quad);
        }

        quad = template.claimCopy(4);
        quad.setSurface(TOP_AND_BOTTOM_SURFACE);
        quad.setNominalFace(EnumFacing.DOWN);
        quad.setupFaceQuad(0, 0, 1, 1, 0, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        template.release();
    }

    @Override
    public boolean isAdditive() {
        return true;
    }

    @Override
    public boolean isCube(ISuperModelState modelState) {
        return modelState.getMetaData() == 15;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block,
            ISuperModelState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState) {
        return modelState.getAxis() == EnumFacing.Axis.Y ? 255 : modelState.getMetaData();
    }

    @Override
    public BlockOrientationType orientationType(ISuperModelState modelState) {
        return BlockOrientationType.FACE;
    }

    @Override
    public @Nonnull ICollisionHandler collisionHandler() {
        return this;
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState) {
        return ImmutableList.of(getCollisionBoundingBox(modelState));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ISuperModelState modelState) {
        return Useful.makeRotatedAABB(0, 0, 0, 1, (modelState.getMetaData() + 1) / 16f, 1, modelState.getMatrix4f());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(ISuperModelState modelState) {
        return getCollisionBoundingBox(modelState);
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, EnumFacing side) {
        if (modelState.getMetaData() == 15)
            return SideShape.SOLID;

        if (side.getAxis() == modelState.getAxis()) {
            return (side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) == modelState.isAxisInverted()
                    ? SideShape.SOLID
                    : SideShape.MISSING;
        } else {
            return modelState.getMetaData() > 8 ? SideShape.PARTIAL : SideShape.MISSING;
        }
    }

    @Override
    public int getMetaData(ISuperModelState modelState) {
        return (int) (modelState.getStaticShapeBits() & 0xF);
    }

    @Override
    public void setMetaData(ISuperModelState modelState, int meta) {
        modelState.setStaticShapeBits(meta);
    }

    @Override
    public boolean hasLampSurface(ISuperModelState modelState) {
        return false;
    }

}
