/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.collision;

import static grondag.xm.collision.OctreeCoordinates.ALL_EMPTY;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.mesh.polygon.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

@API(status = INTERNAL)
class MeshVoxelizer extends AbstractMeshVoxelizer implements Consumer<Polygon> {
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

    static boolean getVoxelBit(int voxelIndex4, long[] voxelBits) {
        final int xyz = OctreeCoordinates.indexToXYZ4(voxelIndex4);
        return (voxelBits[xyz >> 6] & (1L << (xyz & 63))) != 0;
    }
    
    static final double VOXEL_VOLUME = 1.0 / 8 / 8 / 8;

    private final long[] voxelBits = new long[128];
    private final float[] polyData = new float[36];
    final long[] snapshot = new long[8];

    @Override
    protected void acceptTriangle(Vec3f v0, Vec3f v1, Vec3f v2) {
        final float[] data = polyData;
        TriangleBoxTest.packPolyData(v0, v1, v2, data);
        div1(polyData, voxelBits);
    }

    // field because lambdas... OK because threadlocal
    VoxelShape result;
    
    static final double SIZE = 1.0/8.0;
    final VoxelShape build() {
        result = VoxelShapes.empty();
        
        final long[] data = this.voxelBits;
        VoxelVolume16.fillVolume(data);
        
        //PERF: output larger voxels when have them
        VoxelVolume16.forEachSimpleVoxel(data, 4, (x, y, z) -> {
            final double x0 = x * SIZE;
            final double y0 = y * SIZE;
            final double z0 = z * SIZE;
            result = VoxelShapes.union(result, VoxelShapes.cuboid(x0, y0, z0, x0 + SIZE, y0 + SIZE, z0 + SIZE));
        });

        if (result.isEmpty()) {
            // handle very small meshes that don't half-fill any simple voxels; avoid having no collision volume
            VoxelVolume16.forEachSimpleVoxel(data, 1, (x, y, z) -> {
                final double x0 = x * SIZE;
                final double y0 = y * SIZE;
                final double z0 = z * SIZE;
                result = VoxelShapes.union(result, VoxelShapes.cuboid(x0, y0, z0, x0 + SIZE, y0 + SIZE, z0 + SIZE));
            });
        }
        
        // prep for next use
        System.arraycopy(ALL_EMPTY, 0, data, 0, 128);
        
        return result;
    }
}
