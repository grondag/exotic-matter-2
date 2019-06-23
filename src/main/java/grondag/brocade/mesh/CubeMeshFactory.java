package grondag.brocade.mesh;

import static grondag.brocade.state.MeshStateData.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.brocade.block.BrocadeBlock;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.painting.Surface;
import grondag.brocade.painting.SurfaceTopology;
import grondag.brocade.primitives.CubeInputs;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.IPolyStream;
import grondag.brocade.primitives.stream.IWritablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;
import grondag.brocade.state.MeshState;
import grondag.brocade.state.StateFormat;
import grondag.fermion.world.Rotation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CubeMeshFactory extends MeshFactory {
    private static final Surface SURFACE_MAIN = Surface.builder(SurfaceTopology.CUBIC)
            .withDisabledLayers(PaintLayer.CUT, PaintLayer.LAMP).build();

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public CubeMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NONE);
        this.cachedQuads = getCubeQuads();
    }

    @Override
    public void produceShapeQuads(MeshState modelState, Consumer<IPolygon> target) {
        if (cachedQuads.origin()) {
            IPolygon reader = cachedQuads.reader();

            do
                target.accept(reader);
            while (cachedQuads.next());
        }
    }

    private IPolyStream getCubeQuads() {
        CubeInputs cube = new CubeInputs();
        cube.color = 0xFFFFFFFF;
        cube.textureRotation = Rotation.ROTATE_NONE;
        cube.isFullBrightness = false;
        cube.u0 = 0;
        cube.v0 = 0;
        cube.u1 = 1;
        cube.v1 = 1;
        cube.isOverlay = false;
        cube.surfaceInstance = SURFACE_MAIN;

        IWritablePolyStream stream = PolyStreams.claimWritable();
        cube.appendFace(stream, Direction.DOWN);
        cube.appendFace(stream, Direction.UP);
        cube.appendFace(stream, Direction.EAST);
        cube.appendFace(stream, Direction.WEST);
        cube.appendFace(stream, Direction.NORTH);
        cube.appendFace(stream, Direction.SOUTH);

        IPolyStream result = stream.releaseAndConvertToReader();

        result.origin();
        assert result.reader().vertexCount() == 4;

        return result;
    }

    @Override
    public boolean isCube(MeshState modelState) {
        return true;
    }

    @Override
    public boolean rotateBlock(BlockState blockState, World world, BlockPos pos, Direction axis, BrocadeBlock block,
            MeshState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(MeshState modelState) {
        return 255;
    }

    @Override
    public boolean hasLampSurface(MeshState modelState) {
        return true;
    }
}
