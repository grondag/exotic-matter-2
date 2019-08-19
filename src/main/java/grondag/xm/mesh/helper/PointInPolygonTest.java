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
package grondag.xm.mesh.helper;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.joml.Vector2f;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Vec3f;

/**
 * Adapted from code that bears the notice reproduced below and which can be
 * checked at http://geomalgorithms.com/a03-_inclusion.html
 *
 * // Copyright 2000 softSurfer, 2012 Dan Sunday // This code may be freely used
 * and modified for any purpose // providing that this copyright notice is
 * included with it. // SoftSurfer makes no warranty for this code, and cannot
 * be held // liable for any real or imagined damage resulting from its use. //
 * Users of this code must verify correctness for their application.
 * 
 * 
 */
@API(status = INTERNAL)
public class PointInPolygonTest {

    /**
     * Tests if a point is Left|On|Right of an infinite line
     * 
     * @param lineStart
     * @param lineEnd
     * @param point
     * @return >0 for point left of the line <br>
     *         =0 for point on the line <br>
     *         <0 for point right of the line
     */
    private static double isLeft(Vector2f lineStart, Vector2f lineEnd, Vector2f point) {
        return (lineEnd.x - lineStart.x) * (point.y - lineStart.y) - (point.x - lineStart.x) * (lineEnd.y - lineStart.y);
    }

    /**
     * Tests if a point is Left|On|Right of an infinite line
     * 
     * @param lineStart
     * @param lineEnd
     * @param point
     * @return >0 for point left of the line <br>
     *         =0 for point on the line <br>
     *         <0 for point right of the line
     */
    private static float isLeft(float xStart, float yStart, float xEnd, float yEnd, float x, float y) {
        return (xEnd - xStart) * (y - yStart) - (x - xStart) * (yEnd - yStart);

    }

    /**
     * Crossing number test for a point in a polygon. This code is patterned after
     * [Franklin, 2000]
     * 
     * @param point    point to be tested
     * @param vertices vertex points of a closed polygon V[n+1] with V[n]=V[0]
     * @return true if inside
     */
    public static boolean isPointInPolyCrossingNumber(Vector2f point, Vector2f[] vertices) {
        int cn = 0; // the crossing number counter

        // number of vertices is one less due to wrapped input array
        int size = vertices.length - 1;

        // loop through all edges of the polygon
        for (int i = 0; i < size; i++) { // edge from V[i] to V[i+1]
            if (((vertices[i].y <= point.y) && (vertices[i + 1].y > point.y)) // an upward crossing
                    || ((vertices[i].y > point.y) && (vertices[i + 1].y <= point.y))) // a downward crossing
            {
                // compute the actual edge-ray intersect x-coordinate
                double vt = (point.y - vertices[i].y) / (vertices[i + 1].y - vertices[i].y);
                if (point.x < vertices[i].x + vt * (vertices[i + 1].x - vertices[i].x)) // P.x < intersect
                    ++cn; // a valid crossing of y=P.y right of P.x
            }
        }
        return (cn & 1) == 1; // 0 if even (out), and 1 if odd (in)
    }

    /**
     * Winding number test for a point in a polygon
     * 
     * @param point    point to be tested
     * @param vertices vertex points of a closed polygon V[n+1] with V[n]=V[0]
     * @return true if inside
     */
    public static boolean isPointInPolyWindingNumber(Vector2f point, Vector2f[] vertices) {
        int wn = 0; // the winding number counter

        // number of vertices is one less due to wrapped input array
        int size = vertices.length - 1;

        // loop through all edges of the polygon
        for (int i = 0; i < size; i++) // edge from V[i] to V[i+1]
        {
            if (vertices[i].y <= point.y) // start y <= P.y
            {
                if (vertices[i + 1].y > point.y) // an upward crossing
                    if (isLeft(vertices[i], vertices[i + 1], point) > 0) // P left of edge
                        ++wn; // have a valid up intersect
            } else // start y > P.y (no test needed)
            {
                if (vertices[i + 1].y <= point.y) // a downward crossing
                    if (isLeft(vertices[i], vertices[i + 1], point) < 0) // P right of edge
                        --wn; // have a valid down intersect
            }
        }
        return wn != 0;
    }

    // FIX: this appears to have a 6% false negative rate but isn't really used
    // right now
    public static boolean isPointInPolygonAny(Vec3f point, MutablePolygon quad) {
        // faster to check in 2 dimensions, so throw away the orthogonalAxis
        // that is most orthogonal to our plane
        final DiscardAxis d = DiscardAxis.get(quad.faceNormal());
        final float x = d.x(point);
        final float y = d.y(point);
        final int size = quad.vertexCount();

        int wn = 0; // the winding number counter

        Vec3f v = quad.getPos(size - 1);
        float x0 = d.x(v);
        float y0 = d.y(v);

        float x1, y1;
        // loop through all edges of the polygon
        for (int i = 0; i < size; i++) {
            v = quad.getPos(i);
            x1 = d.x(v);
            y1 = d.y(v);
            wn += windingNumber(x0, y0, x1, y1, x, y);
            x0 = x1;
            y0 = y1;
        }
        return wn != 0;
    }

    private static int windingNumber(float x0, float y0, float x1, float y1, float x, float y) {
        if (y0 <= y) // start y <= P.y
        {
            if (y1 > y) // an upward crossing
                if (isLeft(x0, y0, x1, y1, x, y) > 0) // P left of edge
                    return 1; // have a valid up intersect
        } else // start y > P.y
        {
            if (y1 <= y) // a downward crossing
                if (isLeft(x0, y0, x1, y1, x, y) < 0) // P right of edge
                    return -1; // have a valid down intersect
        }
        return 0;
    }

    public static boolean isPointInPolygon(Vec3f point, MutablePolygon quad) {
        final int size = quad.vertexCount();
        if (size == 3)
            return isPointInPolygonTri(point, quad);
        else if (size == 4)
            return isPointInPolygonQuad(point, quad);
        else
            return isPointInPolygonAny(point, quad);

    }

    public static boolean isPointInPolygonQuad(Vec3f point, MutablePolygon quad) {
        // faster to check in 2 dimensions, so throw away the axis
        // that is most orthogonal to our plane
        final DiscardAxis d = DiscardAxis.get(quad.faceNormal());
        final float x = d.x(point);
        final float y = d.y(point);
        Vec3f v = quad.getPos(0);
        final float x0 = d.x(v);
        final float y0 = d.y(v);
        v = quad.getPos(1);
        final float x1 = d.x(v);
        final float y1 = d.y(v);
        v = quad.getPos(2);
        final float x2 = d.x(v);
        final float y2 = d.y(v);
        v = quad.getPos(3);
        final float x3 = d.x(v);
        final float y3 = d.y(v);

        return isPointInPolygonTri(x, y, x0, y0, x1, y1, x2, y2) || isPointInPolygonTri(x, y, x0, y0, x2, y2, x3, y3);
    }

    public static boolean isPointInPolygonTri(Vec3f point, MutablePolygon quad) {
        // faster to check in 2 dimensions, so throw away the axis
        // that is most orthogonal to our plane
        final DiscardAxis d = DiscardAxis.get(quad.faceNormal());
        final Vec3f v0 = quad.getPos(0);
        final Vec3f v1 = quad.getPos(1);
        final Vec3f v2 = quad.getPos(2);

        return isPointInPolygonTri(d.x(point), d.y(point), d.x(v0), d.y(v0), d.x(v1), d.y(v1), d.x(v2), d.y(v2));

    }

    public static boolean isPointInPolygonTri(float x, float y, float x0, float y0, float x1, float y1, float x2, float y2) {
        return (y1 - y0) * (x - x0) + (-x1 + x0) * (y - y0) >= 0 && (y2 - y1) * (x - x1) + (-x2 + x1) * (y - y1) >= 0
                && (y0 - y2) * (x - x2) + (-x0 + x2) * (y - y2) >= 0;
    }

    static enum DiscardAxis {
        X() {
            @Override
            protected final float x(Vec3f pointIn) {
                return pointIn.y();
            }

            @Override
            protected final float y(Vec3f pointIn) {
                return pointIn.z();
            }
        },

        Y() {
            @Override
            protected final float x(Vec3f pointIn) {
                return pointIn.x();
            }

            @Override
            protected final float y(Vec3f pointIn) {
                return pointIn.z();
            }
        },

        Z() {
            @Override
            protected final float x(Vec3f pointIn) {
                return pointIn.x();
            }

            @Override
            protected final float y(Vec3f pointIn) {
                return pointIn.y();
            }
        };

        /**
         * Returns the orthogonalAxis that is most orthogonal to the plane identified by
         * the given normal and thus should be ignored for PnP testing.
         */
        static DiscardAxis get(Vec3f normal) {
            final float absX = Math.abs(normal.x());
            final float absY = Math.abs(normal.y());
            if (absX > absY)
                return absX > Math.abs(normal.z()) ? X : Z;
            else // y >= x
                return absY > Math.abs(normal.z()) ? Y : Z;
        }

        /**
         * Returns a 2d point with this orthogonalAxis discarded.
         */
        protected float x(Vec3f pointIn) {
            return pointIn.x();
        }

        protected float y(Vec3f pointIn) {
            return pointIn.y();
        }
    }
}
