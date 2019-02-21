package grondag.brocade.mesh;

import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NONE;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.collision.CollisionBoxDispatcher;
import grondag.exotic_matter.model.collision.ICollisionHandler;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.painting.SurfaceTopology;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.stream.IPolyStream;
import grondag.exotic_matter.model.primitives.stream.IWritablePolyStream;
import grondag.exotic_matter.model.primitives.stream.PolyStreams;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.StateFormat;
import grondag.exotic_matter.model.varia.SideShape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SphereMeshFactory extends ShapeMeshGenerator implements ICollisionHandler {
    private static final Surface SURFACE_MAIN = Surface.builder(SurfaceTopology.TILED).withAllowBorders(false)
            .withDisabledLayers(PaintLayer.LAMP, PaintLayer.CUT).build();

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public SphereMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NONE);
        this.cachedQuads = generateQuads();
    }

    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target) {
        if (cachedQuads.isEmpty())
            return;

        cachedQuads.origin();
        IPolygon reader = cachedQuads.reader();

        do
            target.accept(reader);
        while (cachedQuads.next());
    }

    private IPolyStream generateQuads() {
        IWritablePolyStream stream = PolyStreams.claimWritable();
        stream.writer().setLockUV(0, false);
        stream.writer().setSurface(SURFACE_MAIN);
        stream.saveDefaults();

        MeshHelper.makeIcosahedron(new Vec3d(.5, .5, .5), 0.6, stream, false);
        return stream.releaseAndConvertToReader();
    }

    @Override
    public boolean isCube(ISuperModelState modelState) {
        return false;
    }

    @Override
    public boolean rotateBlock(IBlockState blockState, World world, BlockPos pos, EnumFacing axis, ISuperBlock block,
            ISuperModelState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState) {
        return 0;
    }

    @Override
    public @Nonnull ICollisionHandler collisionHandler() {
        return this;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, EnumFacing side) {
        return SideShape.MISSING;
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(ISuperModelState modelState) {
        return CollisionBoxDispatcher.getCollisionBoxes(modelState);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(ISuperModelState modelState) {
        return Block.FULL_BLOCK_AABB;
    }

    @Override
    public boolean hasLampSurface(ISuperModelState modelState) {
        return false;
    }
}
