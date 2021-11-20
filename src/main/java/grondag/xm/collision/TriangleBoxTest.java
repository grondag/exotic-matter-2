/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.collision;

import static grondag.xm.api.mesh.polygon.PolyHelper.EPSILON;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.Mth;

import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.Vec3f;

/**
 * Ported to Java from Tomas Akenine-Möller
 * http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/tribox3.txt
 * Heavily modified for performance in this context. Used for high-performance
 * voxelization of block models.
 */
@Internal
class TriangleBoxTest {
	/**
	 *
	 * @param normX
	 * @param normY
	 * @param normZ
	 * @param vX
	 * @param vY
	 * @param vZ
	 * @param boxHalfSize
	 * @return True if plane specified by point and normal intersects with a box of given radius centered at origin.
	 */
	static boolean planeBoxOverlap(final float normX, final float normY, final float normZ, final float vX, final float vY, final float vZ,
			float boxHalfSize) {
		float minX, minY, minZ, maxX, maxY, maxZ;

		if (normX > 0.0f) {
			minX = -boxHalfSize - vX;
			maxX = boxHalfSize - vX;
		} else {
			minX = boxHalfSize - vX;
			maxX = -boxHalfSize - vX;
		}

		if (normY > 0.0f) {
			minY = -boxHalfSize - vY;
			maxY = boxHalfSize - vY;
		} else {
			minY = boxHalfSize - vY;
			maxY = -boxHalfSize - vY;
		}

		if (normZ > 0.0f) {
			minZ = -boxHalfSize - vZ;
			maxZ = boxHalfSize - vZ;
		} else {
			minZ = boxHalfSize - vZ;
			maxZ = -boxHalfSize - vZ;
		}

		if (normX * minX + normY * minY + normZ * minZ > 0.0f) return false;

		if (normX * maxX + normY * maxY + normZ * maxZ >= -EPSILON) return true;

		return false;
	}

	public static final int POLY_MIN_X = 0;
	public static final int POLY_MAX_X = 1;
	public static final int POLY_MIN_Y = 2;
	public static final int POLY_MAX_Y = 3;
	public static final int POLY_MIN_Z = 4;
	public static final int POLY_MAX_Z = 5;
	public static final int POLY_V0_X = 6;
	public static final int POLY_V0_Y = 7;
	public static final int POLY_V0_Z = 8;
	public static final int POLY_V1_X = 9;
	public static final int POLY_V1_Y = 10;
	public static final int POLY_V1_Z = 11;
	public static final int POLY_V2_X = 12;
	public static final int POLY_V2_Y = 13;
	public static final int POLY_V2_Z = 14;
	public static final int POLY_NORM_X = 15;
	public static final int POLY_NORM_Y = 16;
	public static final int POLY_NORM_Z = 17;
	public static final int EDGE_0_X = 18;
	public static final int EDGE_0_Y = 19;
	public static final int EDGE_0_Z = 20;
	public static final int EDGE_1_X = 21;
	public static final int EDGE_1_Y = 22;
	public static final int EDGE_1_Z = 23;
	public static final int EDGE_2_X = 24;
	public static final int EDGE_2_Y = 25;
	public static final int EDGE_2_Z = 26;

	// pair-wise combinations of absolute edge lengths
	public static final int ASL_0_XY = 27;
	public static final int ASL_0_XZ = 28;
	public static final int ASL_0_YZ = 29;
	public static final int ASL_1_XY = 30;
	public static final int ASL_1_XZ = 31;
	public static final int ASL_1_YZ = 32;
	public static final int ASL_2_XY = 33;
	public static final int ASL_2_XZ = 34;
	public static final int ASL_2_YZ = 35;

	/**
	 * Packs data from Triangle vertices into array for use by
	 * {@link #triBoxOverlap(float, float, float, float, float[])}. Doing it this
	 * way enables reuse over many tests, minimizes call overhead and improves LOR.
	 * For low garbage, use a threadlocal array.
	 */
	public static void packPolyData(Vec3f v0, Vec3f v1, Vec3f v2, float[] polyData) {
		final float x0 = Mth.clamp(v0.x(), 0, 1);
		final float y0 = Mth.clamp(v0.y(), 0, 1);
		final float z0 = Mth.clamp(v0.z(), 0, 1);

		final float x1 = Mth.clamp(v1.x(), 0, 1);
		final float y1 = Mth.clamp(v1.y(), 0, 1);
		final float z1 = Mth.clamp(v1.z(), 0, 1);

		final float x2 = Mth.clamp(v2.x(), 0, 1);
		final float y2 = Mth.clamp(v2.y(), 0, 1);
		final float z2 = Mth.clamp(v2.z(), 0, 1);

		polyData[POLY_V0_X] = x0;
		polyData[POLY_V0_Y] = y0;
		polyData[POLY_V0_Z] = z0;
		polyData[POLY_V1_X] = x1;
		polyData[POLY_V1_Y] = y1;
		polyData[POLY_V1_Z] = z1;
		polyData[POLY_V2_X] = x2;
		polyData[POLY_V2_Y] = y2;
		polyData[POLY_V2_Z] = z2;

		// find min/max of three components with only 2 or 3 comparisons
		if (x0 > x1) {
			if (x0 > x2) {
				polyData[POLY_MAX_X] = x0;
				polyData[POLY_MIN_X] = x1 < x2 ? x1 : x2;
			} else {
				// x1 < x0 <= x2
				polyData[POLY_MAX_X] = x2;
				polyData[POLY_MIN_X] = x1;
			}
		} else {
			// x0 <= x1
			if (x1 > x2) {
				polyData[POLY_MAX_X] = x1;
				polyData[POLY_MIN_X] = x0 < x2 ? x0 : x2;
			} else {
				// x0 <= x1 && x1 <= x2
				polyData[POLY_MAX_X] = x2;
				polyData[POLY_MIN_X] = x0;
			}
		}

		if (y0 > y1) {
			if (y0 > y2) {
				polyData[POLY_MAX_Y] = y0;
				polyData[POLY_MIN_Y] = y1 < y2 ? y1 : y2;
			} else {
				// y1 < y0 <= y2
				polyData[POLY_MAX_Y] = y2;
				polyData[POLY_MIN_Y] = y1;
			}
		} else {
			// y0 <= y1
			if (y1 > y2) {
				polyData[POLY_MAX_Y] = y1;
				polyData[POLY_MIN_Y] = y0 < y2 ? y0 : y2;
			} else {
				// y0 <= y1 && y1 <= y2
				polyData[POLY_MAX_Y] = y2;
				polyData[POLY_MIN_Y] = y0;
			}
		}

		if (z0 > z1) {
			if (z0 > z2) {
				polyData[POLY_MAX_Z] = z0;
				polyData[POLY_MIN_Z] = z1 < z2 ? z1 : z2;
			} else {
				// z1 < z0 <= z2
				polyData[POLY_MAX_Z] = z2;
				polyData[POLY_MIN_Z] = z1;
			}
		} else {
			// z0 <= z1
			if (z1 > z2) {
				polyData[POLY_MAX_Z] = z1;
				polyData[POLY_MIN_Z] = z0 < z2 ? z0 : z2;
			} else {
				// z0 <= z1 && z1 <= z2
				polyData[POLY_MAX_Z] = z2;
				polyData[POLY_MIN_Z] = z0;
			}
		}

		/* pre-compute triangle edges */

		// local cuz needed for normal calc
		final float e0x = x1 - x0;
		final float e0y = y1 - y0;
		final float e0z = z1 - z0;
		final float e1x = x2 - x1;
		final float e1y = y2 - y1;
		final float e1z = z2 - z1;

		final float e2x = x0 - x2;
		final float e2y = y0 - y2;
		final float e2z = z0 - z2;
		polyData[EDGE_0_X] = e0x;
		polyData[EDGE_0_Y] = e0y;
		polyData[EDGE_0_Z] = e0z;
		polyData[EDGE_1_X] = e1x;
		polyData[EDGE_1_Y] = e1y;
		polyData[EDGE_1_Z] = e1z;
		polyData[EDGE_2_X] = e2x;
		polyData[EDGE_2_Y] = e2y;
		polyData[EDGE_2_Z] = e2z;

		/* pre-compute normal */
		polyData[POLY_NORM_X] = e0y * e1z - e0z * e1y;
		polyData[POLY_NORM_Y] = e0z * e1x - e0x * e1z;
		polyData[POLY_NORM_Z] = e0x * e1y - e0y * e1x;

		polyData[ASL_0_XY] = Math.abs(e0x) + Math.abs(e0y);
		polyData[ASL_0_XZ] = Math.abs(e0x) + Math.abs(e0z);
		polyData[ASL_0_YZ] = Math.abs(e0y) + Math.abs(e0z);

		polyData[ASL_1_XY] = Math.abs(e1x) + Math.abs(e1y);
		polyData[ASL_1_XZ] = Math.abs(e1x) + Math.abs(e1z);
		polyData[ASL_1_YZ] = Math.abs(e1y) + Math.abs(e1z);

		polyData[ASL_2_XY] = Math.abs(e2x) + Math.abs(e2y);
		polyData[ASL_2_XZ] = Math.abs(e2x) + Math.abs(e2z);
		polyData[ASL_2_YZ] = Math.abs(e2y) + Math.abs(e2z);
	}

	/**
	 * Per-axis bounding box test. Returns true if tri min/max outside box bounds.
	 * Exclude polys that merely touch an edge unless the poly is co-planar
	 */
	private static boolean isTriExcluded(float triMin, float triMax, float triNormal, float boxCenter, float boxHalfSize) {
		if (PolyHelper.epsilonEquals(triMin, triMax)) {
			// axis-aligned tri
			final float triVal = (triMin + triMax) * 0.5f;

			final float boxMax = boxCenter + boxHalfSize;
			final float dMax = triVal - boxMax;

			if (PolyHelper.epsilonZero(dMax)) {
				return triNormal < 0;
			} else if (dMax > 0) {
				return true;
			}

			final float boxMin = boxCenter - boxHalfSize;
			final float dMin = triVal - boxMin;

			if (PolyHelper.epsilonZero(dMin)) {
				return triNormal > 0;
			} else if (dMin < 0) {
				return true;
			}
		} else {
			if (triMin >= boxCenter + boxHalfSize - EPSILON) return true;
			if (triMax <= boxCenter - boxHalfSize + EPSILON) return true;
		}

		return false;
	}

	/**
	 * Assumes boxes are cubes and polygon info is pre-packed into array using
	 * {@link #packPolyData(Vertex, Vertex, Vertex, float[])}.
	 */
	public static boolean triBoxOverlap(float boxCenterX, float boxCenterY, float boxCenterZ, float boxHalfSize, float[] polyData) {
		// bounding box tests
		if (isTriExcluded(polyData[POLY_MIN_X], polyData[POLY_MAX_X], polyData[POLY_NORM_X], boxCenterX, boxHalfSize)) return false;

		if (isTriExcluded(polyData[POLY_MIN_Y], polyData[POLY_MAX_Y], polyData[POLY_NORM_Y], boxCenterY, boxHalfSize)) return false;

		if (isTriExcluded(polyData[POLY_MIN_Z], polyData[POLY_MAX_Z], polyData[POLY_NORM_Z], boxCenterZ, boxHalfSize)) return false;

		// offset coordinate so that the boxcenter is in (0,0,0)
		final float v0x = polyData[POLY_V0_X] - boxCenterX;
		final float v0y = polyData[POLY_V0_Y] - boxCenterY;
		final float v0z = polyData[POLY_V0_Z] - boxCenterZ;

		// do plane/box before rest of offsets - is relatively simple
		if (!planeBoxOverlap(polyData[POLY_NORM_X], polyData[POLY_NORM_Y], polyData[POLY_NORM_Z], v0x, v0y, v0z, boxHalfSize)) return false;

		// continue with offsets
		final float v1x = polyData[POLY_V1_X] - boxCenterX;
		final float v1y = polyData[POLY_V1_Y] - boxCenterY;
		final float v1z = polyData[POLY_V1_Z] - boxCenterZ;

		final float v2x = polyData[POLY_V2_X] - boxCenterX;
		final float v2y = polyData[POLY_V2_Y] - boxCenterY;
		final float v2z = polyData[POLY_V2_Z] - boxCenterZ;

		// Separating axis tests
		{
			final float ex = polyData[EDGE_0_X];
			final float ey = polyData[EDGE_0_Y];
			final float ez = polyData[EDGE_0_Z];

			{
				final float a = ez * v0y - ey * v0z;
				final float b = ez * v2y - ey * v2z;
				//                final float rad = (Math.abs(ez) + Math.abs(ey)) * boxHalfSize; //fez + fey;
				final float rad = polyData[ASL_0_YZ] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}

			{
				final float a = -ez * v0x + ex * v0z;
				final float b = -ez * v2x + ex * v2z;
				//                final float rad = (Math.abs(ez) + Math.abs(ex)) * boxHalfSize; //fez + fex;
				final float rad = polyData[ASL_0_XZ] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}

			{
				final float a = ey * v1x - ex * v1y;
				final float b = ey * v2x - ex * v2y;
				//                final float rad = (Math.abs(ey) + Math.abs(ex)) * boxHalfSize; //fey + fex;
				final float rad = polyData[ASL_0_XY] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}
		}

		{
			final float ex = polyData[EDGE_1_X];
			final float ey = polyData[EDGE_1_Y];
			final float ez = polyData[EDGE_1_Z];

			{
				final float a = ez * v0y - ey * v0z;
				final float b = ez * v2y - ey * v2z;
				final float rad = polyData[ASL_1_YZ] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}

			{
				final float a = -ez * v0x + ex * v0z;
				final float b = -ez * v2x + ex * v2z;
				final float rad = polyData[ASL_1_XZ] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}

			{
				final float a = ey * v0x - ex * v0y;
				final float b = ey * v1x - ex * v1y;
				final float rad = polyData[ASL_1_XY] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}
		}

		{
			final float ex = polyData[EDGE_2_X];
			final float ey = polyData[EDGE_2_Y];
			final float ez = polyData[EDGE_2_Z];

			{
				final float a = ez * v0y - ey * v0z;
				final float b = ez * v1y - ey * v1z;
				final float rad = polyData[ASL_2_YZ] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}

			{
				final float a = -ez * v0x + ex * v0z;
				final float b = -ez * v1x + ex * v1z;
				final float rad = polyData[ASL_2_XZ] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}

			{
				final float a = ey * v1x - ex * v1y;
				final float b = ey * v2x - ex * v2y;
				final float rad = polyData[ASL_2_XY] * boxHalfSize + EPSILON;

				if (a < b) {
					if (a > rad || b < -rad) return false;
				} else if (b > rad || a < -rad) {
					return false;
				}
			}
		}

		return true; /* box and triangle overlaps */
	}
}
