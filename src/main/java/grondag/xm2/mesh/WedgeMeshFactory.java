package grondag.xm2.mesh;

import java.util.function.Consumer;

import grondag.fermion.world.Rotation;
import grondag.xm2.primitives.FaceVertex;
import grondag.xm2.primitives.PolyTransform;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.IWritablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ModelState;
import net.minecraft.util.math.Direction;

public class WedgeMeshFactory extends AbstractWedgeMeshFactory {
    @Override
    public void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target) {
        // Axis for this shape is through the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y axis with full sides against north/down faces.

        // PERF: caching
        final IWritablePolyStream stream = PolyStreams.claimWritable();
        final IMutablePolygon writer = stream.writer();
        
        PolyTransform transform = PolyTransform.get(modelState);
        
        writer.setRotation(0, Rotation.ROTATE_NONE);
        writer.setLockUV(0, true);
        stream.saveDefaults();

        writer.setSurface(BACK_AND_BOTTOM_SURFACE);
        writer.setNominalFace(Direction.NORTH);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        writer.spriteColorAll(0, 0xFF00FFFF);
        transform.apply(writer);
        stream.append();

        writer.setSurface(BACK_AND_BOTTOM_SURFACE);
        writer.setNominalFace(Direction.DOWN);
        writer.spriteColorAll(0, 0xFFFF0000);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(3);
        writer.setSurface(SIDE_SURFACE);
        writer.setNominalFace(Direction.EAST);
        writer.setupFaceQuad(Direction.EAST, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0), new FaceVertex(1, 1, 0), Direction.UP);
        writer.assignLockedUVCoordinates(0);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(3);
        writer.setSurface(SIDE_SURFACE);
        writer.setNominalFace(Direction.WEST);
        writer.setupFaceQuad(Direction.WEST, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0), new FaceVertex(0, 1, 0), Direction.UP);
        writer.assignLockedUVCoordinates(0);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(4);
        writer.setSurface(TOP_SURFACE);
        writer.setNominalFace(Direction.UP);
        writer.setupFaceQuad(Direction.UP, new FaceVertex(0, 0, 1), new FaceVertex(1, 0, 1), new FaceVertex(1, 1, 0),
                new FaceVertex(0, 1, 0), Direction.NORTH);
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
    public boolean hasLampSurface(ModelState modelState) {
        return false;
    }
}
