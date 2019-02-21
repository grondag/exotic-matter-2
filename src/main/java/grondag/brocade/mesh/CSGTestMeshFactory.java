package grondag.brocade.mesh;

import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NONE;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.CSG2.CSG;
import grondag.exotic_matter.model.collision.CollisionBoxDispatcher;
import grondag.exotic_matter.model.collision.ICollisionHandler;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.painting.SurfaceTopology;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.stream.CsgPolyStream;
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
import net.minecraft.world.World;

public class CSGTestMeshFactory extends ShapeMeshGenerator implements ICollisionHandler {
    private static final Surface SURFACE_MAIN = Surface.builder(SurfaceTopology.CUBIC)
            .withDisabledLayers(PaintLayer.CUT, PaintLayer.LAMP).build();
    private static final Surface SURFACE_LAMP = Surface.builder(SurfaceTopology.CUBIC)
            .withEnabledLayers(PaintLayer.LAMP).build();

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public CSGTestMeshFactory() {
        super(StateFormat.BLOCK, STATE_FLAG_NONE);
        this.cachedQuads = getTestQuads();
    }

    @Override
    public void produceShapeQuads(ISuperModelState modelState, Consumer<IPolygon> target) {
        if (cachedQuads.origin()) {
            IPolygon reader = cachedQuads.reader();

            do
                target.accept(reader);
            while (cachedQuads.next());
        }
    }

    private IPolyStream getTestQuads() {
        // union opposite overlapping coplanar faces
//      result = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, .4, .5, 1, 1, 1), template));
//      delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(.3, 0, 0, .7, .6, .5), template));
//      result = result.union(delta);

        // union opposite overlapping coplanar faces created by diff
//      result = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9), template));
//      delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.3, 0.03, 0.5, 0.5, 0.95, 0.7), template));  
//      result = result.difference(delta);
//      delta = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.3, 0, 0, 0.4, .2, 1), template));
//      result = result.union(delta);

        // cylinder/cone test
//      result = new CSGShape(QuadFactory.makeCylinder(new Vec3d(.5, 0, .5), new Vec3d(.5, 1, .5), 0.5, 0, template));

        // icosahedron (sphere) test
//    result = new CSGShape(QuadFactory.makeIcosahedron(new Vec3d(.5, .5, .5), 0.5, template));

        CsgPolyStream quadsA = PolyStreams.claimCSG();
        quadsA.writer().setLockUV(0, true);
        quadsA.writer().setSurface(SURFACE_MAIN);
        quadsA.saveDefaults();
        MeshHelper.makePaintableBox(new AxisAlignedBB(0, 0.4, 0.4, 1.0, 0.6, 0.6), quadsA);

        CsgPolyStream quadsB = PolyStreams.claimCSG();
        quadsB.writer().setLockUV(0, true);
        quadsB.writer().setSurface(SURFACE_LAMP);
        quadsB.saveDefaults();
        MeshHelper.makePaintableBox(new AxisAlignedBB(0.2, 0, 0.4, 0.6, 1.0, 0.8), quadsB);

        IWritablePolyStream output = PolyStreams.claimWritable();
        CSG.union(quadsA, quadsB, output);

        quadsA.release();
        quadsB.release();

//      IPolyStream result = PolyStreams.claimRecoloredCopy(output);
//      output.release();
//      return result;
        return output.releaseAndConvertToReader();

//      quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0, 0.3, 1, 1, .7), template));
//      result = result.difference(quadsB);

//      template.color = borderColor.getColorMap(EnumColorMap.HIGHLIGHT);
//      quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0.2, 0.2, 0, 0.8, 0.8, 1), template));
//      result = result.difference(quadsB);
//
//      template.color = borderColor.getColorMap(EnumColorMap.HIGHLIGHT);
//      quadsB = new CSGShape(QuadFactory.makeBox(new AxisAlignedBB(0, 0, .4, 1, .4, .65), template));
//      result = result.difference(quadsB);

//      result.recolor();
    }

    @Override
    public boolean isCube(ISuperModelState modelState) {
        return true;
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
