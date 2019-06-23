package grondag.brocade.mesh;

import static grondag.brocade.state.MeshStateData.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.brocade.block.BrocadeBlock;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.painting.Surface;
import grondag.brocade.painting.SurfaceTopology;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.IPolyStream;
import grondag.brocade.primitives.stream.IWritablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;
import grondag.brocade.state.MeshState;
import grondag.brocade.state.StateFormat;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SphereMeshFactory extends MeshFactory {
    private static final Surface SURFACE_MAIN = Surface.builder(SurfaceTopology.TILED).withAllowBorders(false)
            .withDisabledLayers(PaintLayer.LAMP, PaintLayer.CUT).build();

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public SphereMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NONE);
        this.cachedQuads = generateQuads();
    }

    @Override
    public void produceShapeQuads(MeshState modelState, Consumer<IPolygon> target) {
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
    public boolean isCube(MeshState modelState) {
        return false;
    }

    @Override
    public boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis, BrocadeBlock block,
            MeshState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(MeshState modelState) {
        return 0;
    }

    @Override
    public boolean hasLampSurface(MeshState modelState) {
        return false;
    }
}
