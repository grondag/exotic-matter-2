package grondag.brocade.mesh;

import java.util.function.Consumer;

import javax.vecmath.Matrix4f;

import grondag.exotic_matter.model.primitives.PolyFactory;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.EnumFacing;

public class StairMeshFactory extends AbstractWedgeMeshFactory {
    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target) {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y orthogonalAxis with full sides against north/east
        // faces.

        Matrix4f matrix = modelState.getMatrix4f();

        IMutablePolygon template = PolyFactory.COMMON_POOL.newPaintable(4);
        template.setRotation(0, Rotation.ROTATE_NONE);
        template.setLockUV(0, true);

        IMutablePolygon quad = template.claimCopy(4);
        quad.setSurface(BACK_AND_BOTTOM_SURFACE);
        quad.setNominalFace(EnumFacing.NORTH);
        quad.setupFaceQuad(0, 0, 1, 1, 0, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(BACK_AND_BOTTOM_SURFACE);
        quad.setNominalFace(EnumFacing.EAST);
        quad.setupFaceQuad(0, 0, 1, 1, 0, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);

        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.UP, 0.0, 0.5, 0.5, 1.0, 0.0, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.UP, 0.5, 0.5, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.UP, 0.5, 0.0, 1.0, 0.5, 0.0, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.DOWN, 0.0, 0.5, 0.5, 1.0, 0.0, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.DOWN, 0.5, 0.5, 1.0, 1.0, 0.0, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.DOWN, 0.0, 0.0, 0.5, 0.5, 0.0, EnumFacing.NORTH);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.SOUTH, 0.5, 0.0, 1.0, 1.0, 0.0, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(TOP_SURFACE);
        // salt is so cuts appear different from top/front face
        // wedges can't connect textures with adjacent flat blocks consistently anyway,
        // so doesn't hurt them
        quad.setTextureSalt(1);
        quad.setupFaceQuad(EnumFacing.SOUTH, 0.0, 0.0, 0.5, 1.0, 0.5, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(SIDE_SURFACE);
        quad.setupFaceQuad(EnumFacing.WEST, 0.0, 0.0, 0.5, 1.0, 0.0, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);

        quad = template.claimCopy(4);
        quad.setSurface(TOP_SURFACE);
        quad.setTextureSalt(1);
        quad.setupFaceQuad(EnumFacing.WEST, 0.5, 0.0, 1.0, 1.0, 0.5, EnumFacing.UP);
        quad.transform(matrix);
        target.accept(quad);

        template.release();
    }

    @Override
    public boolean hasLampSurface(ISuperModelState modelState) {
        return false;
    }
}
