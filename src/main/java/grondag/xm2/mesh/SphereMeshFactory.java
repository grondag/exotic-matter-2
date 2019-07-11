package grondag.xm2.mesh;

import static grondag.xm2.state.ModelStateData.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.xm2.painting.PaintLayer;
import grondag.xm2.painting.Surface;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.IPolyStream;
import grondag.xm2.primitives.stream.IWritablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.StateFormat;
import net.minecraft.util.math.Vec3d;

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
    public void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target) {
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
    public boolean isCube(ModelState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState) {
        return 0;
    }

    @Override
    public boolean hasLampSurface(ModelState modelState) {
        return false;
    }
}
