package grondag.brocade.mesh;

import static grondag.brocade.model.state.ModelStateData.STATE_FLAG_NONE;

import java.util.List;
import java.util.function.Consumer;



import grondag.brocade.block.ISuperBlock;
import grondag.brocade.collision.CollisionBoxDispatcher;
import grondag.brocade.collision.ICollisionHandler;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.painting.Surface;
import grondag.brocade.painting.SurfaceTopology;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.IPolyStream;
import grondag.brocade.primitives.stream.IWritablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;
import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.state.StateFormat;
import grondag.brocade.model.varia.SideShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BoundingBox;
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
    public boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis, ISuperBlock block,
            ISuperModelState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ISuperModelState modelState) {
        return 0;
    }

    @Override
    public ICollisionHandler collisionHandler() {
        return this;
    }

    @Override
    public SideShape sideShape(ISuperModelState modelState, Direction side) {
        return SideShape.MISSING;
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
    public boolean hasLampSurface(ISuperModelState modelState) {
        return false;
    }
}
