package grondag.xm2.mesh;

import java.util.function.Consumer;

import grondag.fermion.world.Rotation;
import grondag.xm2.primitives.PolyTransform;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.IWritablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ModelState;
import net.minecraft.util.math.Direction;

public class StairMeshFactory extends AbstractWedgeMeshFactory {
    @Override
    public void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target) {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y orthogonalAxis with full sides against north/east
        // faces.

        // PERF: if have a consumer and doing this dynamically - should consumer simply be a stream?
        // Why create a stream just to pipe it to the consumer?  Or cache the result.
        final IWritablePolyStream stream = PolyStreams.claimWritable();
        final IMutablePolygon quad = stream.writer();
        
        PolyTransform transform = PolyTransform.get(modelState);

        quad.setRotation(0, Rotation.ROTATE_NONE);
        quad.setLockUV(0, true);
        stream.saveDefaults();

        quad.setSurface(BACK_AND_BOTTOM_SURFACE);
        quad.setNominalFace(Direction.NORTH);
        quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.setSurface(BACK_AND_BOTTOM_SURFACE);
        quad.setNominalFace(Direction.EAST);
        quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        transform.apply(quad);
        stream.append();

        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.UP, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.UP, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.UP, 0.5, 0.0, 1.0, 0.5, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.DOWN, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.DOWN, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.DOWN, 0.0, 0.0, 0.5, 0.5, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.SOUTH, 0.5, 0.0, 1.0, 1.0, 0.0, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.setSurface(TOP_SURFACE);
        // salt is so cuts appear different from top/front face
        // wedges can't connect textures with adjacent flat blocks consistently anyway,
        // so doesn't hurt them
        quad.setTextureSalt(1);
        quad.setupFaceQuad(Direction.SOUTH, 0.0, 0.0, 0.5, 1.0, 0.5, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(Direction.WEST, 0.0, 0.0, 0.5, 1.0, 0.0, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.setSurface(TOP_SURFACE);
        quad.setTextureSalt(1);
        quad.setupFaceQuad(Direction.WEST, 0.5, 0.0, 1.0, 1.0, 0.5, Direction.UP);
        transform.apply(quad);
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
    public boolean hasLampSurface(ModelState modelState) {
        return false;
    }
}
