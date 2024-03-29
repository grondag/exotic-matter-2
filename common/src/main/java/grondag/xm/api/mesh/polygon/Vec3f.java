/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.api.mesh.polygon;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

import grondag.xm.mesh.vertex.Vec3fFactory;

@Experimental
public interface Vec3f {
	static float dotProduct(final float x0, final float y0, final float z0, final float x1, final float y1, final float z1) {
		return x0 * x1 + y0 * y1 + z0 * z1;
	}

	default float dotProduct(float xIn, float yIn, float zIn) {
		return dotProduct(x(), y(), z(), xIn, yIn, zIn);
	}

	default float dotProduct(Vec3f vec) {
		return dotProduct(vec.x(), vec.y(), vec.z());
	}

	/**
	 * Returns a new vector with the result of this vector x the specified vector.
	 */
	default Vec3f crossProduct(Vec3f vec) {
		return Vec3f.create(y() * vec.z() - z() * vec.y(), z() * vec.x() - x() * vec.z(), x() * vec.y() - y() * vec.x());
	}

	default float length() {
		return Mth.sqrt(lengthSquared());
	}

	default float lengthSquared() {
		final float x = x();
		final float y = y();
		final float z = z();
		return x * x + y * y + z * z;
	}

	float x();

	float y();

	float z();

	/**
	 * Returns a signed distance to the plane of the given face. Positive numbers
	 * mean in front of face, negative numbers in back.
	 */
	default float distanceToFacePlane(Direction face) {
		// could use dot product, but exploiting special case for less math
		switch (face) {
			case UP:
				return y() - 1;

			case DOWN:
				return -y();

			case EAST:
				return x() - 1;

			case WEST:
				return -x();

			case NORTH:
				return -z();

			case SOUTH:
				return z() - 1;

			default:
				// make compiler shut up about unhandled case
				return 0;
		}
	}

	default boolean isOnFacePlane(Direction face, float tolerance) {
		return Math.abs(distanceToFacePlane(face)) < tolerance;
	}

	/**
	 * True if both vertices are at the same point.
	 */
	default boolean isCsgEqual(Vec3f vertexIn) {
		final float x = vertexIn.x() - x();
		final float y = vertexIn.y() - y();
		final float z = vertexIn.z() - z();
		return x * x + y * y + z * z < PolyHelper.EPSILON * PolyHelper.EPSILON;
	}

	private static float distance(float x0, float y0, float z0, float x1, float y1, float z1) {
		final float d0 = x0 - x1;
		final float d1 = y0 - y1;
		final float d2 = z0 - z1;
		return (float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	/**
	 * True if point i,j,k is on line formed by x0,y0,z0 and x1, y1, z1.
	 *
	 * <p>Will return false for points that are "very close" to each other because
	 * there essentially isn't enough resolution to make a firm determination of
	 * what the line is.
	 */
	static boolean isPointOnLine(float cx, float cy, float cz, float ax, float ay, float az, float bx, float by, float bz) {
		// points have to be far enough apart to form a line
		final float ab = distance(ax, ay, az, bx, by, bz);

		if (ab < PolyHelper.EPSILON * 5) {
			return false;
		} else {
			final float bThis = distance(cx, cy, cz, bx, by, bz);
			final float aThis = distance(ax, ay, az, cx, cy, cz);
			return Math.abs(ab - bThis - aThis) < PolyHelper.EPSILON;
		}
	}

	// PERF: is this way faster?
	@Internal
	static boolean isPointOnLine2(float cx, float cy, float cz, float ax, float ay, float az, float bx, float by, float bz) {
		//AB and AC must have same normal
		final float abx = bx - ax;
		final float aby = by - ay;
		final float abz = bz - az;

		final float acx = cx - ax;
		final float acy = cy - ay;
		final float acz = cz - az;

		// check cross product
		final float xx = aby * acz - abz * acy;
		final float xy = abz * acx - abx * acz;
		final float xz = abx * acy - aby * acx;

		if (PolyHelper.epsilonZero(xx) && PolyHelper.epsilonZero(xy) && PolyHelper.epsilonZero(xz)) {
			// on the line, check if in segment

			// must be on same side
			final float dot = dotProduct(abx, aby, abz, acx, acy, acz);

			if (dot < -PolyHelper.EPSILON) {
				return false;
			} else {
				final float abm = abx * abx + aby * aby + abz * abz;
				final float acm = acx * acx + acy * acy + acz * acz;
				return !(acm - abm > PolyHelper.EPSILON);
			}
		} else {
			return false;
		}
	}

	/**
	 * True if this point is on the line formed by the two given points.
	 *
	 * <p>Will return false for points that are "very close" to each other because
	 * there essentially isn't enough resolution to make a firm determination of
	 * what the line is.
	 */
	default boolean isOnLine(float x0, float y0, float z0, float x1, float y1, float z1) {
		return isPointOnLine(x(), y(), z(), x0, y0, z0, x1, y1, z1);
	}

	default boolean isOnLine(Vec3f v0, Vec3f v1) {
		return this.isOnLine(v0.x(), v0.y(), v0.z(), v1.x(), v1.y(), v1.z());
	}

	/**
	 * Loads our x, y, z values into the provided array.
	 */
	default void toArray(float[] data) {
		data[0] = x();
		data[1] = y();
		data[2] = z();
	}

	Vec3f ZERO = Vec3fFactory.ZERO;

	static Vec3f forFace(Direction face) {
		return Vec3fFactory.forFace(face);
	}

	static Vec3f create(Vec3i vec) {
		return Vec3fFactory.create(vec);
	}

	static Vec3f create(float x, float y, float z) {
		return Vec3fFactory.create(x, y, z);
	}
}
