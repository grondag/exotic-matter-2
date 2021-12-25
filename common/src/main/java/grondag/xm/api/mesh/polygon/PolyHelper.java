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

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

@Experimental
public class PolyHelper {
	public static final float EPSILON = 1.0E-5F;

	public static boolean epsilonEquals(float first, float second) {
		return Math.abs(first - second) < EPSILON;
	}

	public static boolean epsilonZero(float value) {
		return Math.abs(value) < EPSILON;
	}

	private static final Direction[] FACES = Direction.values();

	public static Direction faceForNormal(final float x, final float y, final float z) {
		Direction result = null;

		double minDiff = 0.0F;

		for (int i = 0; i < 6; i++) {
			final Direction f = FACES[i];
			final Vec3i faceNormal = f.getNormal();
			final float diff = Vec3f.dotProduct(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ(), x, y, z);

			if (diff >= 0.0 && diff > minDiff) {
				minDiff = diff;
				result = f;
			}
		}

		return result == null ? Direction.UP : result;
	}

	public static Direction faceForNormal(Vec3f normal) {
		return faceForNormal(normal.x(), normal.y(), normal.z());
	}

	/** Returns the face that is normally the "top" of the given face. */
	public static Direction defaultTopOf(Direction faceIn) {
		switch (faceIn) {
			case UP:
				return Direction.NORTH;
			case DOWN:
				return Direction.SOUTH;
			default:
				return Direction.UP;
		}
	}

	public static Direction bottomOf(Direction faceIn, Direction topFace) {
		return topFace.getOpposite();
	}

	public static Direction positiveDirection(Direction.Axis axis) {
		switch (axis) {
			case Y:
				return Direction.UP;
			case X:
				return Direction.EAST;
			default:
				return Direction.NORTH;
		}
	}

	public static Direction leftOf(Direction faceIn, Direction topFace) {
		return PolyHelper.rightOf(faceIn, topFace).getOpposite();
	}

	public static Direction rightOf(Direction faceIn, Direction topFace) {
		switch (faceIn) {
			case NORTH:
				switch (topFace) {
					case UP:
						return Direction.WEST;
					case EAST:
						return Direction.UP;
					case DOWN:
						return Direction.EAST;
					case WEST:
					default:
						return Direction.DOWN;
				}
			case SOUTH:
				switch (topFace) {
					case UP:
						return Direction.EAST;
					case EAST:
						return Direction.DOWN;
					case DOWN:
						return Direction.WEST;
					case WEST:
					default:
						return Direction.UP;
				}
			case EAST:
				switch (topFace) {
					case UP:
						return Direction.NORTH;
					case NORTH:
						return Direction.DOWN;
					case DOWN:
						return Direction.SOUTH;
					case SOUTH:
					default:
						return Direction.UP;
				}
			case WEST:
				switch (topFace) {
					case UP:
						return Direction.SOUTH;
					case NORTH:
						return Direction.UP;
					case DOWN:
						return Direction.NORTH;
					case SOUTH:
					default:
						return Direction.DOWN;
				}
			case UP:
				switch (topFace) {
					case NORTH:
						return Direction.EAST;
					case EAST:
						return Direction.SOUTH;
					case SOUTH:
						return Direction.WEST;
					case WEST:
					default:
						return Direction.NORTH;
				}
			case DOWN:
			default:
				switch (topFace) {
					case NORTH:
						return Direction.WEST;
					case EAST:
						return Direction.NORTH;
					case SOUTH:
						return Direction.EAST;
					case WEST:
					default:
						return Direction.SOUTH;
				}
		}
	}

	public static boolean isConvex(Polygon poly) {
		final int vertexCount = poly.vertexCount();
		if (vertexCount == 3) return true;

		float testX = 0;
		float testY = 0;
		float testZ = 0;
		boolean needTest = true;

		Vec3f priorVertex = poly.getPos(vertexCount - 2);
		Vec3f thisVertex = poly.getPos(vertexCount - 1);

		for (int nextIndex = 0; nextIndex < vertexCount; nextIndex++) {
			final Vec3f nextVertex = poly.getPos(nextIndex);

			final float ax = thisVertex.x() - priorVertex.x();
			final float ay = thisVertex.y() - priorVertex.y();
			final float az = thisVertex.z() - priorVertex.z();

			final float bx = nextVertex.x() - thisVertex.x();
			final float by = nextVertex.y() - thisVertex.y();
			final float bz = nextVertex.z() - thisVertex.z();

			final float crossX = ay * bz - az * by;
			final float crossY = az * bx - ax * bz;
			final float crossZ = ax * by - ay * bx;

			if (needTest) {
				needTest = false;
				testX = crossX;
				testY = crossY;
				testZ = crossZ;
			} else if (testX * crossX + testY * crossY + testZ * crossZ < 0) {
				return false;
			}

			priorVertex = thisVertex;
			thisVertex = nextVertex;
		}

		return true;
	}
}
