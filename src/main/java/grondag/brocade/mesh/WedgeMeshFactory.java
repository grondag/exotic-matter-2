package grondag.brocade.mesh;

import java.util.function.Consumer;

import javax.vecmath.Matrix4f;

import grondag.exotic_matter.model.primitives.FaceVertex;
import grondag.exotic_matter.model.primitives.PolyFactory;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.EnumFacing;

public class WedgeMeshFactory extends AbstractWedgeMeshFactory
{
    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target)
    {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y orthogonalAxis with full sides against north/east faces.

        Matrix4f matrix = modelState.getMatrix4f();
        
        IMutablePolygon template = PolyFactory.COMMON_POOL.newPaintable(4);
        template.setRotation(0, Rotation.ROTATE_NONE);
        template.setLockUV(0, true);

        IMutablePolygon quad = template.claimCopy();
        quad.setSurface(BACK_AND_BOTTOM_SURFACE);
        quad.setNominalFace(EnumFacing.NORTH);
        quad.setupFaceQuad(0, 0, 1, 1, 0, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);
      
        quad = template.claimCopy();
        quad.setSurface(BACK_AND_BOTTOM_SURFACE);
        quad.setNominalFace(EnumFacing.EAST);
        quad.setupFaceQuad(0, 0, 1, 1, 0, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);
        
        quad = template.claimCopy();
        quad.setSurface(SIDE_SURFACE);
        quad.setNominalFace(EnumFacing.UP);
        quad.setupFaceQuad(EnumFacing.UP,
                new FaceVertex(0, 1, 0),
                new FaceVertex(1, 0, 0),
                new FaceVertex(1, 1, 0), 
                EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);
        
        quad = template.claimCopy();
        quad.setSurface(SIDE_SURFACE);
        quad.setNominalFace(EnumFacing.DOWN);
        quad.setupFaceQuad(EnumFacing.DOWN,
                new FaceVertex(0, 0, 0),
                new FaceVertex(1, 1, 0),
                new FaceVertex(0, 1, 0), 
                EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);
        
        quad = template.claimCopy();
        quad.setSurface(TOP_SURFACE);
        quad.setNominalFace(EnumFacing.SOUTH);
        quad.setupFaceQuad(EnumFacing.SOUTH,
                new FaceVertex(0, 0, 1),
                new FaceVertex(1, 0, 0),
                new FaceVertex(1, 1, 0), 
                new FaceVertex(0, 1, 1), 
                EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);
        
        template.release();
    }

    @Override
    public boolean hasLampSurface(ISuperModelState modelState)
    {
        return false;
    }
}
