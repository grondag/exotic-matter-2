package grondag.brocade.collision;

import static grondag.brocade.collision.octree.OctreeCoordinates.ALL_EMPTY;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import grondag.fermion.config.FermionConfig;
import grondag.brocade.collision.octree.OctreeCoordinates;
import grondag.brocade.collision.octree.VoxelVolume16;
import grondag.brocade.primitives.TriangleBoxTest;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.vertex.IVec3f;
import net.minecraft.util.math.BoundingBox;

public class OptimalBoxGenerator extends AbstractBoxGenerator implements Consumer<IPolygon> {
    private static void div1(final float[] polyData, final long[] voxelBits) {
        if (TriangleBoxTest.triBoxOverlap(CLOW1, CLOW1, CLOW1, R1, polyData))
            div2(00000, 0.0f, 0.0f, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CLOW1, CLOW1, R1, polyData))
            div2(01000, D1, 0.0f, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CLOW1, CHIGH1, CLOW1, R1, polyData))
            div2(02000, 0.0f, D1, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CHIGH1, CLOW1, R1, polyData))
            div2(03000, D1, D1, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CLOW1, CLOW1, CHIGH1, R1, polyData))
            div2(04000, 0.0f, 0.0f, D1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CLOW1, CHIGH1, R1, polyData))
            div2(05000, D1, 0.0f, D1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CLOW1, CHIGH1, CHIGH1, R1, polyData))
            div2(06000, 0.0f, D1, D1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CHIGH1, CHIGH1, R1, polyData))
            div2(07000, D1, D1, D1, polyData, voxelBits);

    }

    private static void div2(int baseIndex, float x0, float y0, float z0, float[] polyData, long[] voxelBits) {
        final float a0 = x0 + CLOW2;
        final float a1 = x0 + CHIGH2;
        final float b0 = y0 + CLOW2;
        final float b1 = y0 + CHIGH2;
        final float c0 = z0 + CLOW2;
        final float c1 = z0 + CHIGH2;

        final float x1 = x0 + D2;
        final float y1 = y0 + D2;
        final float z1 = z0 + D2;

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c0, R2, polyData))
            div3(baseIndex + 0000, x0, y0, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R2, polyData))
            div3(baseIndex + 0100, x1, y0, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R2, polyData))
            div3(baseIndex + 0200, x0, y1, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R2, polyData))
            div3(baseIndex + 0300, x1, y1, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R2, polyData))
            div3(baseIndex + 0400, x0, y0, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R2, polyData))
            div3(baseIndex + 0500, x1, y0, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R2, polyData))
            div3(baseIndex + 0600, x0, y1, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R2, polyData))
            div3(baseIndex + 0700, x1, y1, z1, polyData, voxelBits);
    }

    private static void div3(int baseIndex, float x0, float y0, float z0, float[] polyData, long[] voxelBits) {
        final float a0 = x0 + CLOW3;
        final float a1 = x0 + CHIGH3;
        final float b0 = y0 + CLOW3;
        final float b1 = y0 + CHIGH3;
        final float c0 = z0 + CLOW3;
        final float c1 = z0 + CHIGH3;

        final float x1 = x0 + D3;
        final float y1 = y0 + D3;
        final float z1 = z0 + D3;

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c0, R3, polyData))
            div4(baseIndex + 000, x0, y0, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R3, polyData))
            div4(baseIndex + 010, x1, y0, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R3, polyData))
            div4(baseIndex + 020, x0, y1, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R3, polyData))
            div4(baseIndex + 030, x1, y1, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R3, polyData))
            div4(baseIndex + 040, x0, y0, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R3, polyData))
            div4(baseIndex + 050, x1, y0, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R3, polyData))
            div4(baseIndex + 060, x0, y1, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R3, polyData))
            div4(baseIndex + 070, x1, y1, z1, polyData, voxelBits);
    }

    private static void div4(int baseIndex, float x0, float y0, float z0, float[] polyData, long[] voxelBits) {
        final float a0 = x0 + CLOW4;
        final float a1 = x0 + CHIGH4;
        final float b0 = y0 + CLOW4;
        final float b1 = y0 + CHIGH4;
        final float c0 = z0 + CLOW4;
        final float c1 = z0 + CHIGH4;

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c0, R4, polyData))
            setVoxelBit(baseIndex + 00, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R4, polyData))
            setVoxelBit(baseIndex + 01, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R4, polyData))
            setVoxelBit(baseIndex + 02, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R4, polyData))
            setVoxelBit(baseIndex + 03, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R4, polyData))
            setVoxelBit(baseIndex + 04, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R4, polyData))
            setVoxelBit(baseIndex + 05, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R4, polyData))
            setVoxelBit(baseIndex + 06, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R4, polyData))
            setVoxelBit(baseIndex + 07, voxelBits);
    }

    static void setVoxelBit(int voxelIndex4, long[] voxelBits) {
        final int xyz = OctreeCoordinates.indexToXYZ4(voxelIndex4);
        voxelBits[xyz >> 6] |= (1L << (xyz & 63));
    }

    public static final double VOXEL_VOLUME = 1.0 / 8 / 8 / 8;

    private final long[] voxelBits = new long[128];
    private final float[] polyData = new float[36];
    private final SimpleBoxListBuilder builder = new SimpleBoxListBuilder();
    final long[] snapshot = new long[8];
    final BoxFinder bf = new BoxFinder();

    @Override
    protected void acceptTriangle(IVec3f v0, IVec3f v1, IVec3f v2) {
        final float[] data = polyData;
        TriangleBoxTest.packPolyData(v0, v1, v2, data);
        div1(polyData, voxelBits);
    }

    /**
     * Returns voxel volume of loaded mesh after simplification. Simplification
     * level is estimated based on the count of maximal bounding volumes vs the
     * budget per mesh. Call after inputing mesh via
     * {@link #accept(grondag.exotic_matter.model.primitives.IPolygon)} and before
     * calling {@link #build()}
     * <p>
     * .
     * 
     * Returns -1 if mesh isn't appropriate for optimization.
     */
    public final double prepare() {
        final long[] data = this.voxelBits;
        VoxelVolume16.fillVolume(data);
        bf.clear();
        VoxelVolume16.forEachSimpleVoxel(data, 4, (x, y, z) -> bf.setFilled(x, y, z));

        // handle very small meshes that don't half-fill any simple voxels; avoid having
        // no collision boxes
        if (bf.isEmpty())
            VoxelVolume16.forEachSimpleVoxel(data, 1, (x, y, z) -> bf.setFilled(x, y, z));

        // prep for next use
        System.arraycopy(ALL_EMPTY, 0, data, 0, 64);

        bf.calcCombined();
        bf.populateMaximalVolumes();

        // find maximal volumes to enable estimate of simplification level
        int overage = bf.volumeCount - ConfigXM.BLOCKS.collisionBoxBudget;

        if (overage > 0) {
            bf.simplify(overage);
            bf.calcCombined();
            bf.populateMaximalVolumes();
            if (bf.volumeCount > 16)
                return -1;
        }

        bf.saveTo(snapshot);
        int voxelCount = 0;
        for (long bits : snapshot)
            voxelCount += Long.bitCount(bits);

        return voxelCount * VOXEL_VOLUME;
    }

//    /**
//     * Call after prepare but before build.
//     * Only for use in dev environment.
//     */
//    public final void generateCalibrationOutput()
//    {
//        final long[] data = this.voxelBits;
//        final long[] savedData = new long[128];
//        System.arraycopy(data, 0, savedData, 0, 128);
//        
//        VoxelVolume16.fillVolume(data);
//        bf.clear();
//        VoxelVolume16.forEachSimpleVoxel(data, 4, (x, y, z) -> bf.setFilled(x, y, z));
//        
//        // handle very small meshes that don't half-fill any simple voxels; avoid having no collision boxes
//        if(bf.isEmpty())
//            VoxelVolume16.forEachSimpleVoxel(data, 1, (x, y, z) -> bf.setFilled(x, y, z));
//        
//        bf.saveTo(snapshot);
//        builder.clear();
//        
//        bf.calcCombined();
//        bf.populateMaximalVolumes();
//        bf.populateIntersects();
//        
//        // find maximal volumes to enable estimate of simplification level
//        int volCount = bf.volumeCount;
//        int intersectionCount = 0;
//        for(long vi : bf.intersects)
//        {
//            intersectionCount += Long.bitCount(vi);
//        }
//        intersectionCount /= 2;
//        int startingVoxels = bf.filledVoxelCount();
//        
//        bf.outputBoxes(builder);
//
//        if(builder.size() > ConfigXM.BLOCKS.collisionBoxBudget)
//        {
//            int simplificationLevel = 1;
//            while(true)
//            {
//                bf.restoreFrom(snapshot);
//                
//                if(bf.simplify(simplificationLevel))
//                {
//                    int voxDiff = bf.filledVoxelCount() - startingVoxels;
//                    
//                    builder.clear();
//                    bf.outputBoxes(builder);
//                    if(builder.size() <= ConfigXM.BLOCKS.collisionBoxBudget)
//                    {
//                        ExoticMatter.INSTANCE.info("%d, %d, %d, %d, %d, %d",
//                                volCount,
//                                intersectionCount,
//                                startingVoxels,
//                                voxDiff,
//                                simplificationLevel,
//                                builder.size()
//                                );
//                        break;
//                    }
//                    else
//                        simplificationLevel++;
//                }
//                else assert false : "Unable to simplify below target volume count";
//                
//            }
//        }
//        System.arraycopy(savedData, 0, data, 0, 128);
//        builder.clear();
//    }

    public final ImmutableList<BoundingBox> build() {
        builder.clear();
        bf.outputBoxes(builder);
        return builder.build();
    }
}
