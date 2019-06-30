package grondag.xm2.collision;

import static grondag.xm2.collision.octree.OctreeCoordinates.ALL_EMPTY;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import grondag.xm2.collision.octree.OctreeCoordinates;
import grondag.xm2.collision.octree.VoxelVolume8;
import grondag.xm2.primitives.TriangleBoxTest;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.vertex.IVec3f;
import net.minecraft.util.math.Box;

/**
 * Generates non-intersecting collision boxes for a model within a single block
 * at 1/4 block distance (per axis).
 * <p>
 * 
 * Identifies which voxels intersects with polys in the block mesh to build a
 * shell at 1/8 resolution, then fills the shell interior and outputs 1/4 voxels
 * that are at least half full.
 * <p>
 * 
 * Output voxels sharing a face joined together by
 * {@link JoiningBoxListBuilder}. No other attempt is made to reduce box count -
 * instead relying on the low resolution to keep box counts reasonable.
 * <p>
 * 
 * During the shell identification, voxels are addressed using Octree
 * coordinates but those coordinates are never saved to state (exist only in the
 * call stack.) When leaf nodes are identified, voxel bits are set using
 * Cartesian coordinates converted from octree coordinates because Cartesian
 * representation is better (or at least as good) for the subsequent
 * simplification, fill and output operations.
 */
public class FastBoxGenerator extends AbstractBoxGenerator implements Consumer<IPolygon> {
    private static void div1(final float[] polyData, final long[] voxelBits) {
        if (TriangleBoxTest.triBoxOverlap(CLOW1, CLOW1, CLOW1, R1, polyData))
            div2(0000, 0.0f, 0.0f, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CLOW1, CLOW1, R1, polyData))
            div2(0100, D1, 0.0f, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CLOW1, CHIGH1, CLOW1, R1, polyData))
            div2(0200, 0.0f, D1, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CHIGH1, CLOW1, R1, polyData))
            div2(0300, D1, D1, 0.0f, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CLOW1, CLOW1, CHIGH1, R1, polyData))
            div2(0400, 0.0f, 0.0f, D1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CLOW1, CHIGH1, R1, polyData))
            div2(0500, D1, 0.0f, D1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CLOW1, CHIGH1, CHIGH1, R1, polyData))
            div2(0600, 0.0f, D1, D1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(CHIGH1, CHIGH1, CHIGH1, R1, polyData))
            div2(0700, D1, D1, D1, polyData, voxelBits);

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
            div3(baseIndex + 000, x0, y0, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R2, polyData))
            div3(baseIndex + 010, x1, y0, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R2, polyData))
            div3(baseIndex + 020, x0, y1, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R2, polyData))
            div3(baseIndex + 030, x1, y1, z0, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R2, polyData))
            div3(baseIndex + 040, x0, y0, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R2, polyData))
            div3(baseIndex + 050, x1, y0, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R2, polyData))
            div3(baseIndex + 060, x0, y1, z1, polyData, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R2, polyData))
            div3(baseIndex + 070, x1, y1, z1, polyData, voxelBits);
    }

    private static void div3(int baseIndex, float x0, float y0, float z0, float[] polyData, long[] voxelBits) {
        final float a0 = x0 + CLOW3;
        final float a1 = x0 + CHIGH3;
        final float b0 = y0 + CLOW3;
        final float b1 = y0 + CHIGH3;
        final float c0 = z0 + CLOW3;
        final float c1 = z0 + CHIGH3;

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c0, R3, polyData))
            setVoxelBit(baseIndex + 00, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R3, polyData))
            setVoxelBit(baseIndex + 01, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R3, polyData))
            setVoxelBit(baseIndex + 02, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R3, polyData))
            setVoxelBit(baseIndex + 03, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R3, polyData))
            setVoxelBit(baseIndex + 04, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R3, polyData))
            setVoxelBit(baseIndex + 05, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R3, polyData))
            setVoxelBit(baseIndex + 06, voxelBits);

        if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R3, polyData))
            setVoxelBit(baseIndex + 07, voxelBits);
    }

    static void setVoxelBit(int voxelIndex3, long[] voxelBits) {
        final int xyz = OctreeCoordinates.indexToXYZ3(voxelIndex3);
        voxelBits[xyz >> 6] |= (1L << (xyz & 63));
    }

    private final long[] voxelBits = new long[16];
    private final float[] polyData = new float[36];
    private final JoiningBoxListBuilder builder = new JoiningBoxListBuilder();

    @Override
    protected final void acceptTriangle(IVec3f v0, IVec3f v1, IVec3f v2) {
        final float[] data = polyData;
        TriangleBoxTest.packPolyData(v0, v1, v2, data);
        div1(polyData, voxelBits);
    }

    public final ImmutableList<Box> build() {
        builder.clear();
        final long[] data = this.voxelBits;
        VoxelVolume8.fillVolume(data);
        VoxelVolume8.forEachSimpleVoxel(data, 4, (x, y, z) -> {
            builder.addSorted(x, y, z, x + 2, y + 2, z + 2);
        });

        // handle very small meshes that don't half-fill any simple voxels; avoid having
        // no collision boxes
        if (builder.isEmpty())
            VoxelVolume8.forEachSimpleVoxel(data, 1, (x, y, z) -> builder.addSorted(x, y, z, x + 2, y + 2, z + 2));

        // prep for next use
        System.arraycopy(ALL_EMPTY, 0, data, 0, 16);

        return builder.build();
    }
}
