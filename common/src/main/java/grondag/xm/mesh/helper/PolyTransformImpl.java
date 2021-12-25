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

package grondag.xm.mesh.helper;

import static grondag.xm.orientation.api.CubeRotation.DOWN_EAST;
import static grondag.xm.orientation.api.CubeRotation.DOWN_NORTH;
import static grondag.xm.orientation.api.CubeRotation.DOWN_SOUTH;
import static grondag.xm.orientation.api.CubeRotation.DOWN_WEST;
import static grondag.xm.orientation.api.CubeRotation.EAST_DOWN;
import static grondag.xm.orientation.api.CubeRotation.EAST_NORTH;
import static grondag.xm.orientation.api.CubeRotation.EAST_SOUTH;
import static grondag.xm.orientation.api.CubeRotation.EAST_UP;
import static grondag.xm.orientation.api.CubeRotation.NORTH_DOWN;
import static grondag.xm.orientation.api.CubeRotation.NORTH_EAST;
import static grondag.xm.orientation.api.CubeRotation.NORTH_UP;
import static grondag.xm.orientation.api.CubeRotation.NORTH_WEST;
import static grondag.xm.orientation.api.CubeRotation.SOUTH_DOWN;
import static grondag.xm.orientation.api.CubeRotation.SOUTH_EAST;
import static grondag.xm.orientation.api.CubeRotation.SOUTH_UP;
import static grondag.xm.orientation.api.CubeRotation.SOUTH_WEST;
import static grondag.xm.orientation.api.CubeRotation.UP_EAST;
import static grondag.xm.orientation.api.CubeRotation.UP_NORTH;
import static grondag.xm.orientation.api.CubeRotation.UP_SOUTH;
import static grondag.xm.orientation.api.CubeRotation.UP_WEST;
import static grondag.xm.orientation.api.CubeRotation.WEST_DOWN;
import static grondag.xm.orientation.api.CubeRotation.WEST_NORTH;
import static grondag.xm.orientation.api.CubeRotation.WEST_SOUTH;
import static grondag.xm.orientation.api.CubeRotation.WEST_UP;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.orientation.api.CubeCorner;
import grondag.xm.orientation.api.CubeEdge;
import grondag.xm.orientation.api.CubeRotation;
import grondag.xm.orientation.api.HorizontalEdge;
import grondag.xm.orientation.api.HorizontalFace;
import grondag.xm.orientation.api.OrientationType;

@Internal
@SuppressWarnings("rawtypes")
public class PolyTransformImpl implements PolyTransform {
	private final float m00, m10, m20, m30;
	private final float m01, m11, m21, m31;
	private final float m02, m12, m22, m32;

	private PolyTransformImpl(float... matrix) {
		m00 = matrix[0];
		m10 = matrix[1];
		m20 = matrix[2];
		m30 = matrix[3];

		m01 = matrix[4];
		m11 = matrix[5];
		m21 = matrix[6];
		m31 = matrix[7];

		m02 = matrix[8];
		m12 = matrix[9];
		m22 = matrix[10];
		m32 = matrix[11];
	}

	@Override
	public void accept(MutablePolygon poly) {
		final int vertexCount = poly.vertexCount();

		// transform vertices
		for (int i = 0; i < vertexCount; i++) {
			final float x = poly.x(i) - 0.5f;
			final float y = poly.y(i) - 0.5f;
			final float z = poly.z(i) - 0.5f;

			poly.pos(i,
					m00 * x + m10 * y + m20 * z + m30 + 0.5f,
					m01 * x + m11 * y + m21 * z + m31 + 0.5f,
					m02 * x + m12 * y + m22 * z + m32 + 0.5f);

			if (poly.hasNormal(i)) {
				final float nx = poly.normalX(i);
				final float ny = poly.normalY(i);
				final float nz = poly.normalZ(i);

				final float rnx = m00 * nx + m10 * ny + m20 * nz;
				final float rny = m01 * nx + m11 * ny + m21 * nz;
				final float rnz = m02 * nx + m12 * ny + m22 * nz;

				final float invMagnitude = (float) (1.0 / (float) Math.sqrt(rnx * rnx + rny * rny + rnz * rnz));

				poly.normal(i, rnx * invMagnitude, rny * invMagnitude, rnz * invMagnitude);
			}
		}

		// transform nominal face
		{
			final Vec3i oldVec = poly.nominalFace().getNormal();
			final float nx = oldVec.getX();
			final float ny = oldVec.getY();
			final float nz = oldVec.getZ();

			final float rnx = m00 * nx + m10 * ny + m20 * nz;
			final float rny = m01 * nx + m11 * ny + m21 * nz;
			final float rnz = m02 * nx + m12 * ny + m22 * nz;

			poly.nominalFace(PolyHelper.faceForNormal(rnx, rny, rnz));
		}

		// transform cull face
		final Direction cullFace = poly.cullFace();

		if (cullFace != null) {
			final Vec3i oldVec = cullFace.getNormal();
			final float nx = oldVec.getX();
			final float ny = oldVec.getY();
			final float nz = oldVec.getZ();

			final float rnx = m00 * nx + m10 * ny + m20 * nz;
			final float rny = m01 * nx + m11 * ny + m21 * nz;
			final float rnz = m02 * nx + m12 * ny + m22 * nz;

			poly.cullFace(PolyHelper.faceForNormal(rnx, rny, rnz));
		}
	}

	private static final PolyTransformImpl[][] LOOKUP = new PolyTransformImpl[OrientationType.values().length][];
	private static final PolyTransformImpl[] EXACT = new PolyTransformImpl[CubeRotation.COUNT];
	private static final PolyTransformImpl[] EDGE = new PolyTransformImpl[CubeEdge.COUNT];
	private static final PolyTransformImpl[] CORNER = new PolyTransformImpl[CubeCorner.COUNT];
	private static final PolyTransformImpl[] FACE = new PolyTransformImpl[6];
	private static final PolyTransformImpl[] HORIZONTAL_EDGE = new PolyTransformImpl[HorizontalEdge.COUNT];
	private static final PolyTransformImpl[] HORIZONTAL_FACE = new PolyTransformImpl[HorizontalFace.COUNT];
	private static final PolyTransformImpl[] AXIS = new PolyTransformImpl[3];

	// mainly for run-time testing
	public static void invalidateCache() {
		populateLookups();
	}

	static {
		LOOKUP[OrientationType.ROTATION.ordinal()] = EXACT;
		LOOKUP[OrientationType.EDGE.ordinal()] = EDGE;
		LOOKUP[OrientationType.CORNER.ordinal()] = CORNER;
		LOOKUP[OrientationType.FACE.ordinal()] = FACE;
		LOOKUP[OrientationType.HORIZONTAL_FACE.ordinal()] = HORIZONTAL_FACE;
		LOOKUP[OrientationType.HORIZONTAL_EDGE.ordinal()] = HORIZONTAL_EDGE;
		LOOKUP[OrientationType.AXIS.ordinal()] = AXIS;
		LOOKUP[OrientationType.NONE.ordinal()] = new PolyTransformImpl[1];
		populateLookups();
	}

	private static void populateLookups() {
		EXACT[DOWN_SOUTH.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		EXACT[DOWN_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[DOWN_NORTH.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[DOWN_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[UP_NORTH.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[UP_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[UP_SOUTH.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		EXACT[UP_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[NORTH_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[NORTH_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[SOUTH_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[SOUTH_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[SOUTH_DOWN.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[WEST_DOWN.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[NORTH_DOWN.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[EAST_DOWN.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[NORTH_UP.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[EAST_UP.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[SOUTH_UP.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[WEST_UP.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[EAST_NORTH.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[WEST_NORTH.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[EAST_SOUTH.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		EXACT[WEST_SOUTH.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		EXACT[DOWN_SOUTH.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		EXACT[DOWN_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[DOWN_NORTH.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[DOWN_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[UP_NORTH.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[UP_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[UP_SOUTH.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		EXACT[UP_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[NORTH_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[NORTH_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[SOUTH_EAST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[SOUTH_WEST.ordinal()] = new PolyTransformImpl(0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[SOUTH_DOWN.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[WEST_DOWN.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[NORTH_DOWN.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[EAST_DOWN.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[NORTH_UP.ordinal()] = new PolyTransformImpl(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		EXACT[EAST_UP.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[SOUTH_UP.ordinal()] = new PolyTransformImpl(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f);
		EXACT[WEST_UP.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
		EXACT[EAST_NORTH.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[WEST_NORTH.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
		EXACT[EAST_SOUTH.ordinal()] = new PolyTransformImpl(0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		EXACT[WEST_SOUTH.ordinal()] = new PolyTransformImpl(0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

		LOOKUP[OrientationType.NONE.ordinal()][0] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];

		AXIS[Axis.Y.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];
		AXIS[Axis.X.ordinal()] = EXACT[CubeRotation.EAST_UP.ordinal()];
		AXIS[Axis.Z.ordinal()] = EXACT[CubeRotation.NORTH_UP.ordinal()];

		HORIZONTAL_FACE[HorizontalFace.NORTH.ordinal()] = EXACT[CubeRotation.NORTH_EAST.ordinal()];
		HORIZONTAL_FACE[HorizontalFace.EAST.ordinal()] = EXACT[CubeRotation.EAST_SOUTH.ordinal()];
		HORIZONTAL_FACE[HorizontalFace.SOUTH.ordinal()] = EXACT[CubeRotation.SOUTH_WEST.ordinal()];
		HORIZONTAL_FACE[HorizontalFace.WEST.ordinal()] = EXACT[CubeRotation.WEST_SOUTH.ordinal()];

		HORIZONTAL_EDGE[HorizontalEdge.NORTH_EAST.ordinal()] = EXACT[CubeRotation.NORTH_EAST.ordinal()];
		HORIZONTAL_EDGE[HorizontalEdge.NORTH_WEST.ordinal()] = EXACT[CubeRotation.NORTH_WEST.ordinal()];
		HORIZONTAL_EDGE[HorizontalEdge.SOUTH_EAST.ordinal()] = EXACT[CubeRotation.SOUTH_EAST.ordinal()];
		HORIZONTAL_EDGE[HorizontalEdge.SOUTH_WEST.ordinal()] = EXACT[CubeRotation.SOUTH_WEST.ordinal()];

		FACE[Direction.DOWN.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];
		FACE[Direction.UP.ordinal()] = EXACT[CubeRotation.UP_SOUTH.ordinal()];
		FACE[Direction.NORTH.ordinal()] = EXACT[CubeRotation.NORTH_EAST.ordinal()];
		FACE[Direction.SOUTH.ordinal()] = EXACT[CubeRotation.SOUTH_WEST.ordinal()];
		FACE[Direction.EAST.ordinal()] = EXACT[CubeRotation.EAST_SOUTH.ordinal()];
		FACE[Direction.WEST.ordinal()] = EXACT[CubeRotation.WEST_SOUTH.ordinal()];

		CORNER[CubeCorner.UP_NORTH_EAST.ordinal()] = EXACT[CubeRotation.NORTH_UP.ordinal()];
		CORNER[CubeCorner.UP_NORTH_WEST.ordinal()] = EXACT[CubeRotation.UP_NORTH.ordinal()];
		CORNER[CubeCorner.UP_SOUTH_EAST.ordinal()] = EXACT[CubeRotation.UP_SOUTH.ordinal()];
		CORNER[CubeCorner.UP_SOUTH_WEST.ordinal()] = EXACT[CubeRotation.SOUTH_UP.ordinal()];
		CORNER[CubeCorner.DOWN_NORTH_EAST.ordinal()] = EXACT[CubeRotation.DOWN_NORTH.ordinal()];
		CORNER[CubeCorner.DOWN_NORTH_WEST.ordinal()] = EXACT[CubeRotation.NORTH_DOWN.ordinal()];
		CORNER[CubeCorner.DOWN_SOUTH_EAST.ordinal()] = EXACT[CubeRotation.DOWN_EAST.ordinal()];
		CORNER[CubeCorner.DOWN_SOUTH_WEST.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];

		EDGE[CubeEdge.DOWN_SOUTH.ordinal()] = EXACT[CubeRotation.DOWN_SOUTH.ordinal()];
		EDGE[CubeEdge.DOWN_WEST.ordinal()] = EXACT[CubeRotation.DOWN_WEST.ordinal()];
		EDGE[CubeEdge.DOWN_NORTH.ordinal()] = EXACT[CubeRotation.DOWN_NORTH.ordinal()];
		EDGE[CubeEdge.DOWN_EAST.ordinal()] = EXACT[CubeRotation.DOWN_EAST.ordinal()];
		EDGE[CubeEdge.UP_NORTH.ordinal()] = EXACT[CubeRotation.UP_NORTH.ordinal()];
		EDGE[CubeEdge.UP_EAST.ordinal()] = EXACT[CubeRotation.UP_EAST.ordinal()];
		EDGE[CubeEdge.UP_SOUTH.ordinal()] = EXACT[CubeRotation.UP_SOUTH.ordinal()];
		EDGE[CubeEdge.UP_WEST.ordinal()] = EXACT[CubeRotation.UP_WEST.ordinal()];
		EDGE[CubeEdge.NORTH_EAST.ordinal()] = EXACT[CubeRotation.NORTH_EAST.ordinal()];
		EDGE[CubeEdge.NORTH_WEST.ordinal()] = EXACT[CubeRotation.NORTH_WEST.ordinal()];
		EDGE[CubeEdge.SOUTH_EAST.ordinal()] = EXACT[CubeRotation.SOUTH_EAST.ordinal()];
		EDGE[CubeEdge.SOUTH_WEST.ordinal()] = EXACT[CubeRotation.SOUTH_WEST.ordinal()];
	}

	public static PolyTransform get(BaseModelState modelState) {
		return LOOKUP[modelState.orientationType().ordinal()][modelState.orientationIndex()];
	}

	public static PolyTransform forEdgeRotation(int ordinal) {
		return EXACT[ordinal];
	}

	public static PolyTransform get(CubeRotation corner) {
		return EXACT[corner.ordinal()];
	}

	public static PolyTransform get(Axis axis) {
		return AXIS[axis.ordinal()];
	}

	public static PolyTransform get(Direction face) {
		return FACE[face.ordinal()];
	}
}
