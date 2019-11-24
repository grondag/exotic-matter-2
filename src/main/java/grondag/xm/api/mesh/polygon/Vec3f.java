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
package grondag.xm.api.mesh.polygon;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fermion.varia.Useful;
import grondag.xm.mesh.vertex.Vec3fFactory;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

@API(status = EXPERIMENTAL)
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
		return MathHelper.sqrt(lengthSquared());
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

	/**
	 * True if point i,j,k is on line formed by x0,y0,z0 and x1, y1, z1.
	 * <p>
	 *
	 * Will return false for points that are "very close" to each other because
	 * there essentially isn't enough resolution to make a firm determination of
	 * what the line is.
	 */
	static boolean isPointOnLine(float cx, float cy, float cz, float ax, float ay, float az, float bx, float by, float bz) {
		// points have to be far enough apart to form a line
		final float ab = Useful.distance(ax, ay, az, bx, by, bz);
		if (ab < PolyHelper.EPSILON * 5)
			return false;
		else {
			final float bThis = Useful.distance(cx, cy, cz, bx, by, bz);
			final float aThis = Useful.distance(ax, ay, az, cx, cy, cz);
			return Math.abs(ab - bThis - aThis) < PolyHelper.EPSILON;
		}
	}

	// PERF: is this way faster?
	@API(status = Status.INTERNAL)
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


		if(PolyHelper.epsilonZero(xx) && PolyHelper.epsilonZero(xy) && PolyHelper.epsilonZero(xz)) {
			// on the line, check if in segment

			// must be on same side
			final float dot = dotProduct(abx, aby, abz, acx, acy, acz);
			if(dot < -PolyHelper.EPSILON)
				return false;
			else {
				final float abm = abx * abx + aby * aby + abz * abz;
				final float acm = acx * acx + acy * acy + acz * acz;
				if(acm - abm > PolyHelper.EPSILON)
					return false;
				else
					return true;
			}
		} else
			return false;
	}

	/**
	 * True if this point is on the line formed by the two given points.
	 * <p>
	 *
	 * Will return false for points that are "very close" to each other because
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
