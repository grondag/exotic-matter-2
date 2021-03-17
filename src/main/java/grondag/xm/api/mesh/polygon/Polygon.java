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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import grondag.xm.Xm;
import grondag.xm.api.paint.PaintBlendMode;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;

@Experimental
public interface Polygon {
	int vertexCount();

	// PERF: use value types instead
	@Deprecated
	Vec3f getPos(int index);

	/**
	 * Wraps around if index out of range.
	 */
	default Vec3f getPosModulo(int index) {
		return getPos(index % vertexCount());
	}

	Vec3f faceNormal();

	default float faceNormalX() {
		return faceNormal().x();
	}

	default float faceNormalY() {
		return faceNormal().y();
	}

	default float faceNormalZ() {
		return faceNormal().z();
	}

	default Box bounds() {
		final Vec3f p0 = getPos(0);
		final Vec3f p1 = getPos(1);
		final Vec3f p2 = getPos(2);
		final Vec3f p3 = getPos(3);

		final double minX = Math.min(Math.min(p0.x(), p1.x()), Math.min(p2.x(), p3.x()));
		final double minY = Math.min(Math.min(p0.y(), p1.y()), Math.min(p2.y(), p3.y()));
		final double minZ = Math.min(Math.min(p0.z(), p1.z()), Math.min(p2.z(), p3.z()));

		final double maxX = Math.max(Math.max(p0.x(), p1.x()), Math.max(p2.x(), p3.x()));
		final double maxY = Math.max(Math.max(p0.y(), p1.y()), Math.max(p2.y(), p3.y()));
		final double maxZ = Math.max(Math.max(p0.z(), p1.z()), Math.max(p2.z(), p3.z()));

		return new Box(minX, minY, minZ, maxX, maxY, maxZ);
	}

	int VERTEX_NOT_FOUND = -1;

	/**
	 * Will return {@link #VERTEX_NOT_FOUND} (-1) if vertex is not found in this
	 * polygon.
	 */
	default int indexOf(Vec3f v) {
		final int limit = vertexCount();
		for (int i = 0; i < limit; i++) {
			if (v.equals(getPos(i))) {
				return i;
			}
		}
		return VERTEX_NOT_FOUND;
	}

	default boolean isConvex() {
		return PolyHelper.isConvex(this);
	}

	default boolean isOrthogonalTo(Direction face) {
		final Vec3i dv = face.getVector();
		final float dot = faceNormal().dotProduct(dv.getX(), dv.getY(), dv.getZ());
		return Math.abs(dot) <= PolyHelper.EPSILON;
	}

	default boolean isOnSinglePlane() {
		if (vertexCount() == 3) {
			return true;
		}

		final Vec3f fn = faceNormal();

		final float faceX = fn.x();
		final float faceY = fn.y();
		final float faceZ = fn.z();

		final Vec3f first = getPos(0);

		for (int i = 3; i < vertexCount(); i++) {
			final Vec3f v = getPos(i);

			final float dx = v.x() - first.x();
			final float dy = v.y() - first.y();
			final float dz = v.z() - first.z();

			if (Math.abs(faceX * dx + faceY * dy + faceZ * dz) > PolyHelper.EPSILON) {
				return false;
			}
		}

		return true;
	}

	default boolean isOnFace(Direction face, float tolerance) {
		if (face == null) {
			return false;
		}
		for (int i = 0; i < vertexCount(); i++) {
			if (!getPos(i).isOnFacePlane(face, tolerance)) {
				return false;
			}
		}
		return true;
	}

	default Vec3f computeFaceNormal() {
		try {
			final Vec3f v0 = getPos(0);
			final Vec3f v1 = getPos(1);
			final Vec3f v2 = getPos(2);
			final Vec3f v3 = getPos(3);

			final float x0 = v2.x() - v0.x();
			final float y0 = v2.y() - v0.y();
			final float z0 = v2.z() - v0.z();

			final float x1 = v3.x() - v1.x();
			final float y1 = v3.y() - v1.y();
			final float z1 = v3.z() - v1.z();

			final float x = y0 * z1 - z0 * y1;
			final float y = z0 * x1 - x0 * z1;
			final float z = x0 * y1 - y0 * x1;

			float mag = MathHelper.sqrt(x * x + y * y + z * z);
			if (mag < 1.0E-4F) {
				mag = 1f;
			}

			return Vec3f.create(x / mag, y / mag, z / mag);
		} catch (final Exception e) {
			assert false : "Bad polygon structure during face normal request.";
		return Vec3f.ZERO;
		}
	}

	// adapted from http://geomalgorithms.com/a01-_area.html
	// Copyright 2000 softSurfer, 2012 Dan Sunday
	// This code may be freely used and modified for any purpose
	// providing that this copyright notice is included with it.
	// iSurfer.org makes no warranty for this code, and cannot be held
	// liable for any real or imagined damage resulting from its use.
	// Users of this code must verify correctness for their application.
	default float area() {
		float area = 0;
		float an, ax, ay, az; // abs value of normal and its coords
		int coord; // coord to ignore: 1=x, 2=y, 3=z
		int i, j, k; // loop indices
		final int n = vertexCount();
		final Vec3f N = faceNormal();

		if (n < 3)
		{
			return 0; // a degenerate polygon
		}

		// select largest abs coordinate to ignore for projection
		ax = (N.x() > 0 ? N.x() : -N.x()); // abs x-coord
		ay = (N.y() > 0 ? N.y() : -N.y()); // abs y-coord
		az = (N.z() > 0 ? N.z() : -N.z()); // abs z-coord

		coord = 3; // ignore z-coord
		if (ax > ay) {
			if (ax > az)
			{
				coord = 1; // ignore x-coord
			}
		} else if (ay > az)
		{
			coord = 2; // ignore y-coord
		}

		// compute area of the 2D projection
		switch (coord) {
		case 1:
			for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
				area += (getPosModulo(i)).y() * (getPosModulo(j).z() - getPosModulo(k).z());
			}
			break;
		case 2:
			for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
				area += (getPosModulo(i).z() * (getPosModulo(j).x() - getPosModulo(k).x()));
			}
			break;
		case 3:
			for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
				area += (getPosModulo(i).x() * (getPosModulo(j).y() - getPosModulo(k).y()));
			}
			break;
		}

		switch (coord) { // wrap-around term
		case 1:
			area += (getPosModulo(n).y() * (getPosModulo(1).z() - getPosModulo(n - 1).z()));
			break;
		case 2:
			area += (getPosModulo(n).z() * (getPosModulo(1).x() - getPosModulo(n - 1).x()));
			break;
		case 3:
			area += (getPosModulo(n).x() * (getPosModulo(1).y() - getPosModulo(n - 1).y()));
			break;
		}

		// scale to get area before projection
		an = MathHelper.sqrt(ax * ax + ay * ay + az * az); // length of normal vector
		switch (coord) {
		case 1:
			area *= (an / (2 * N.x()));
			break;
		case 2:
			area *= (an / (2 * N.y()));
			break;
		case 3:
			area *= (an / (2 * N.z()));
		}
		return area;
	}

	XmSurface surface();

	/**
	 * Returns computed face normal if no explicit normal assigned.
	 */
	Vec3f vertexNormal(int vertexIndex);

	/**
	 * Face to use for shading testing. Based on which way face points. Never null
	 */
	default Direction lightFace() {
		return PolyHelper.faceForNormal(faceNormal());
	}

	Direction nominalFace();

	/**
	 * Face to use for occlusion testing. Null if not fully on one of the faces.
	 * Fudges a bit because painted quads can be slightly offset from the plane.
	 */
	Direction cullFace();

	default Direction computeCullFace() {
		final Direction nominalFace = nominalFace();

		// semantic face will be right most of the time
		if (isOnFace(nominalFace, PolyHelper.EPSILON)) {
			return nominalFace;
		}

		for (int i = 0; i < 6; i++) {
			final Direction f = Direction.byId(i);
			if (f != nominalFace && isOnFace(f, PolyHelper.EPSILON)) {
				return f;
			}
		}

		return null;
	}

	float maxU(int layerIndex);

	float maxV(int layerIndex);

	float minU(int layerIndex);

	float minV(int layerIndex);

	/**
	 * The maximum wrapping uv distance for either dimension on this surface.
	 * <p>
	 *
	 * Must be zero or positive. Setting to zero disable uvWrapping - painter will
	 * use a 1:1 scale.
	 * <p>
	 *
	 * If the surface is painted with a texture larger than this distance, the
	 * texture will be scaled down to fit in order to prevent visible seams. A scale
	 * of 4, for example, would force a 32x32 texture to be rendered at 1/8 scale.
	 * <p>
	 *
	 * If the surface is painted with a texture smaller than this distance, then the
	 * texture will be zoomed tiled to fill the surface.
	 * <p>
	 *
	 * Default is 0 and generally only comes into play for non-cubic surface
	 * painters.
	 * <p>
	 *
	 * See also {@link SurfaceTopology#TILED}
	 */
	float uvWrapDistance();

	int spriteDepth();

	String spriteName(int layerIndex);

	boolean shouldContractUVs(int layerIndex);

	TextureOrientation rotation(int layerIndex);

	/**
	 * Will return quad color if vertex color not set.
	 */
	int color(int vertexIndex, int layerIndex);

	/**
	 * Will return zero if vertex color not set.
	 */
	int glow(int vertexIndex);

	int textureSalt();

	boolean lockUV(int layerIndex);

	@Deprecated
	PaintBlendMode blendMode(int layerIndex);

	PaintBlendMode blendMode();

	boolean emissive(int layerIndex);

	boolean disableAo(int layerIndex);

	boolean disableDiffuse(int layerIndex);

	// Use materials instead
	//    int getPipelineIndex();
	//
	//    @Override
	//    default IRenderPipeline getPipeline()
	//    {
	//        return ClientProxy.acuityPipeline(getPipelineIndex());
	//    }

	/**
	 * Should be called by when the original reference or another reference created
	 * via {@link #retain()} is no longer held.
	 * <p>
	 *
	 * When retain count is 0 the object will be returned to its allocation pool if
	 * it has one.
	 */
	default void release() {

	}

	default int tag() {
		return NO_LINK_OR_TAG;
	}

	float x(int vertexIndex);

	float y(int vertexIndex);

	float z(int vertexIndex);

	float u(int vertexIndex, int layerIndex);

	float v(int vertexIndex, int layerIndex);

	boolean hasNormal(int vertexIndex);

	float normalX(int vertexIndex);

	float normalY(int vertexIndex);

	float normalZ(int vertexIndex);

	boolean isDeleted();

	void delete();

	/**
	 * Improbable non-zero value that signifies no link set or link not supported.
	 */
	int NO_LINK_OR_TAG = Integer.MIN_VALUE;

	int link();

	void link(int link);

	default boolean hasLink() {
		return link() != Polygon.NO_LINK_OR_TAG;
	}

	void moveTo(int address);

	/**
	 * False if reader at end of stream in build order. <br>
	 * By extension, also false if stream is empty.<br>
	 * Note that WIP is not considered part of stream.
	 */
	boolean hasValue();

	/**
	 * Moves reader to next poly in this stream. Returns false if already at
	 * end.<br>
	 * Note that WIP is not considered part of stream.
	 */
	boolean next();

	boolean nextLink();

	/**
	 * Moves reader to start of stream.<br>
	 * Note that WIP is not considered part of stream.
	 * <p>
	 *
	 * Returns true if reader has a value, meaning stream not empty & not all polys
	 * are deleted.
	 */
	boolean origin();

	/**
	 * Address of current reader location. Can be used with {@link #moveTo(int)} to
	 * return. Addresses are immutable within this stream instance.
	 * <p>
	 */
	int address();

	void clearLink();

	default void toLog() {
		Xm.LOG.debug("Polygon @ mesh address " + address());
		final int limit = vertexCount();
		for(int i = 0; i < limit ; i++) {
			Xm.LOG.info(String.format("   %d = %f  %f  %f", i, x(i), y(i), z(i)));
		}
	}
}
