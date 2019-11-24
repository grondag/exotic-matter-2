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

@API(status = INTERNAL)
class MeshVoxelizer extends AbstractMeshVoxelizer implements Consumer<Polygon> {
	private static void div1(final float[] polyData, final long[] voxelBits) {
		if (TriangleBoxTest.triBoxOverlap(CLOW1, CLOW1, CLOW1, R1, polyData)) {
			div2(00000, 0.0f, 0.0f, 0.0f, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(CHIGH1, CLOW1, CLOW1, R1, polyData)) {
			div2(01000, D1, 0.0f, 0.0f, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(CLOW1, CHIGH1, CLOW1, R1, polyData)) {
			div2(02000, 0.0f, D1, 0.0f, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(CHIGH1, CHIGH1, CLOW1, R1, polyData)) {
			div2(03000, D1, D1, 0.0f, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(CLOW1, CLOW1, CHIGH1, R1, polyData)) {
			div2(04000, 0.0f, 0.0f, D1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(CHIGH1, CLOW1, CHIGH1, R1, polyData)) {
			div2(05000, D1, 0.0f, D1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(CLOW1, CHIGH1, CHIGH1, R1, polyData)) {
			div2(06000, 0.0f, D1, D1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(CHIGH1, CHIGH1, CHIGH1, R1, polyData)) {
			div2(07000, D1, D1, D1, polyData, voxelBits);
		}

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

		if (TriangleBoxTest.triBoxOverlap(a0, b0, c0, R2, polyData)) {
			div3(baseIndex + 0000, x0, y0, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R2, polyData)) {
			div3(baseIndex + 0100, x1, y0, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R2, polyData)) {
			div3(baseIndex + 0200, x0, y1, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R2, polyData)) {
			div3(baseIndex + 0300, x1, y1, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R2, polyData)) {
			div3(baseIndex + 0400, x0, y0, z1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R2, polyData)) {
			div3(baseIndex + 0500, x1, y0, z1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R2, polyData)) {
			div3(baseIndex + 0600, x0, y1, z1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R2, polyData)) {
			div3(baseIndex + 0700, x1, y1, z1, polyData, voxelBits);
		}
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

		if (TriangleBoxTest.triBoxOverlap(a0, b0, c0, R3, polyData)) {
			div4(baseIndex + 000, x0, y0, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R3, polyData)) {
			div4(baseIndex + 010, x1, y0, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R3, polyData)) {
			div4(baseIndex + 020, x0, y1, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R3, polyData)) {
			div4(baseIndex + 030, x1, y1, z0, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R3, polyData)) {
			div4(baseIndex + 040, x0, y0, z1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R3, polyData)) {
			div4(baseIndex + 050, x1, y0, z1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R3, polyData)) {
			div4(baseIndex + 060, x0, y1, z1, polyData, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R3, polyData)) {
			div4(baseIndex + 070, x1, y1, z1, polyData, voxelBits);
		}
	}

	private static void div4(int baseIndex, float x0, float y0, float z0, float[] polyData, long[] voxelBits) {
		final float a0 = x0 + CLOW4;
		final float a1 = x0 + CHIGH4;
		final float b0 = y0 + CLOW4;
		final float b1 = y0 + CHIGH4;
		final float c0 = z0 + CLOW4;
		final float c1 = z0 + CHIGH4;

		if (TriangleBoxTest.triBoxOverlap(a0, b0, c0, R4, polyData)) {
			setVoxelBit(baseIndex + 00, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b0, c0, R4, polyData)) {
			setVoxelBit(baseIndex + 01, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b1, c0, R4, polyData)) {
			setVoxelBit(baseIndex + 02, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b1, c0, R4, polyData)) {
			setVoxelBit(baseIndex + 03, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b0, c1, R4, polyData)) {
			setVoxelBit(baseIndex + 04, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b0, c1, R4, polyData)) {
			setVoxelBit(baseIndex + 05, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a0, b1, c1, R4, polyData)) {
			setVoxelBit(baseIndex + 06, voxelBits);
		}

		if (TriangleBoxTest.triBoxOverlap(a1, b1, c1, R4, polyData)) {
			setVoxelBit(baseIndex + 07, voxelBits);
		}
	}

	static void setVoxelBit(int voxelIndex4, long[] voxelBits) {
		final int xyz = OctreeCoordinates.indexToXYZ4(voxelIndex4);
		voxelBits[xyz >> 6] |= (1L << (xyz & 63));
	}

	static boolean getVoxelBit(int voxelIndex4, long[] voxelBits) {
		final int xyz = OctreeCoordinates.indexToXYZ4(voxelIndex4);
		return (voxelBits[xyz >> 6] & (1L << (xyz & 63))) != 0;
	}

	/**
	 * Offset where voxels from mesh are stored.
	 */
	static final int INPUT = 0;

	/**
	 * Output of fill operation and source for iteration.
	 */
	static final int OUTPUT = 64;

	/**
	 * Holds temp results of reverse fill to detect single voxels
	 */
	static final int FILL_CHECK = 128;

	/**
	 * Holds current run marker for fill operation (only one compound word)
	 */
	static final int RUN_MARKER = FILL_CHECK + 64;

	private final long[] voxelBits = new long[RUN_MARKER + 4];

	private final float[] polyData = new float[36];

	@Override
	protected void acceptTriangle(Vec3f v0, Vec3f v1, Vec3f v2) {
		final float[] data = polyData;
		TriangleBoxTest.packPolyData(v0, v1, v2, data);
		div1(polyData, voxelBits);
	}

	// Will be interned by cache, and this instance is threadlocal so OK
	final VoxelVolumeKey result = new VoxelVolumeKey();

	/**
	 * @return THREADLOCAL shape key to retrieve or create voxel shape - expects to be interned by cache
	 */
	VoxelVolumeKey build() {

		final VoxelVolumeKey result = this.result;
		result.clear();

		final long[] data = voxelBits;
		fillVolume();

		VoxelVolume16.forEachSimpleVoxel(data, OUTPUT, 4, (x, y, z) -> {
			result.set(x, y, z);
		});

		if (result.isEmpty()) {
			// handle very small meshes that don't half-fill any simple voxels; avoid having no collision volume
			VoxelVolume16.forEachSimpleVoxel(data, OUTPUT, 1, (x, y, z) -> {
				result.set(x, y, z);
			});
		}

		// prep for next use
		System.arraycopy(ALL_EMPTY, 0, data, 0, 64);

		return result;
	}

	/**
	 * Straightforward z-axis flood fill. Seems more reliable than the previous
	 * implementation.
	 */
	public void fillVolume() {

		final long[] data = voxelBits;

		// sweep z-plane

		// Copy to upper words for compatibility with previous implementation
		// and because we need the unmodified inputs
		System.arraycopy(data, INPUT, data, OUTPUT, 64);

		System.arraycopy(data, INPUT, data, RUN_MARKER, 4);

		for(int z = 1; z < 15; z++) {
			// set if set in mask
			VoxelVolume16.compoundSet(data, z + OUTPUT / 4, data, RUN_MARKER / 4);

			// clear/set run marker
			// runs end at first encountered set pixel
			VoxelVolume16.compoundXor(data, RUN_MARKER / 4, data, z);
		}


		// sweep z-plane in opposite direction
		System.arraycopy(data, INPUT, data, FILL_CHECK, 64);


		System.arraycopy(data, INPUT + 60, data, RUN_MARKER, 4);

		for(int z = 14; z > 0; z--) {
			// set if set in mask
			VoxelVolume16.compoundSet(data, z + FILL_CHECK / 4, data, RUN_MARKER / 4);

			// clear/set run marker
			// runs end at first encountered set pixel
			VoxelVolume16.compoundXor(data, RUN_MARKER / 4, data, z);
		}

		// voxels must be set in both passes to be valid
		for (int z = 4; z < 60; z++) {
			data[OUTPUT + z] &= data[FILL_CHECK + z];
		}
	}
}
