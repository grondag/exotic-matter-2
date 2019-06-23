package grondag.brocade.mesh;

import java.util.function.Consumer;

import org.joml.Matrix4f;

import grondag.brocade.primitives.FaceVertex;
import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.IWritablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;
import grondag.brocade.state.ISuperModelState;
import grondag.fermion.world.Rotation;
import net.minecraft.util.math.Direction;

public class WedgeMeshFactory extends AbstractWedgeMeshFactory {
    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target) {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y orthogonalAxis with full sides against north/east
        // faces.

        // PERF: if have a consumer and doing this dynamically - should consumer simply be a stream?
        // Why create a stream just to pipe it to the consumer?  Or cache the result.
        final IWritablePolyStream stream = PolyStreams.claimWritable();
        final IMutablePolygon writer = stream.writer();
        
        Matrix4f matrix = modelState.getMatrix4f();

        writer.setRotation(0, Rotation.ROTATE_NONE);
        writer.setLockUV(0, true);
        stream.saveDefaults();

        writer.setSurface(BACK_AND_BOTTOM_SURFACE);
        writer.setNominalFace(Direction.NORTH);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        writer.transform(matrix);
        stream.append();

        writer.setSurface(BACK_AND_BOTTOM_SURFACE);
        writer.setNominalFace(Direction.EAST);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        writer.transform(matrix);
        stream.append();

        writer.setSurface(SIDE_SURFACE);
        writer.setNominalFace(Direction.UP);
        writer.setupFaceQuad(Direction.UP, new FaceVertex(0, 1, 0), new FaceVertex(1, 0, 0), new FaceVertex(1, 1, 0),
                Direction.NORTH);
        writer.transform(matrix);
        stream.append();

        writer.setSurface(SIDE_SURFACE);
        writer.setNominalFace(Direction.DOWN);
        writer.setupFaceQuad(Direction.DOWN, new FaceVertex(0, 0, 0), new FaceVertex(1, 1, 0), new FaceVertex(0, 1, 0),
                Direction.NORTH);
        writer.transform(matrix);
        stream.append();

        writer.setSurface(TOP_SURFACE);
        writer.setNominalFace(Direction.SOUTH);
        writer.setupFaceQuad(Direction.SOUTH, new FaceVertex(0, 0, 1), new FaceVertex(1, 0, 0), new FaceVertex(1, 1, 0),
                new FaceVertex(0, 1, 1), Direction.UP);
        writer.transform(matrix);
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
    public boolean hasLampSurface(ISuperModelState modelState) {
        return false;
    }
}
