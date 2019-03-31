package grondag.brocade.mesh;
/**
 * Portions reproduced or adapted from JCSG.
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */

import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.CsgPolyStream;
import grondag.brocade.primitives.stream.IPolyStream;
import grondag.brocade.primitives.stream.IWritablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;

/**
 * Access point for CSG operations.<br>
 * <em>Heavily</em> modified from original source.<br>
 * Uses polystreams and threadlocals to minimize garbage collection.
 */
public abstract class CSG {
    /**
     * Output a new mesh solid representing the difference of the two input meshes.
     *
     * <blockquote>
     * 
     * <pre>
     * A.difference(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |    +--+
     * +----+--+    |       +----+
     *      |   B   |
     *      |       |
     *      +-------+
     * </pre>
     * 
     * </blockquote>
     */
    public static void difference(IPolyStream a, IPolyStream b, IWritablePolyStream output) {
        CsgPolyStream aCSG = PolyStreams.claimCSG(a);
        CsgPolyStream bCSG = PolyStreams.claimCSG(b);

        difference(aCSG, bCSG, output);

        aCSG.release();
        bCSG.release();
    }

    /**
     * Version of {@link #difference(IPolyStream, IPolyStream, IWritablePolyStream)}
     * to use when you've already built CSG streams. Marks the streams complete but
     * does not release them. Both input streams are modified.
     */
    public static void difference(CsgPolyStream a, CsgPolyStream b, IWritablePolyStream output) {
        a.complete();
        b.complete();

        // A outside of B bounds can be passed directly to output
        if (outputDisjointA(a, b, output))
            // if A is empty there is nothing to subtract from
            return;

        // add portions of A within B bounds but not inside B mesh
        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        a.invert();

        a.outputRecombinedQuads(output);
        b.outputRecombinedQuads(output);
    }

    /**
     * Output a new mesh representing the intersection of two input meshes.
     *
     * <blockquote>
     * 
     * <pre>
     *     A.intersect(B)
     *
     *     +-------+
     *     |       |
     *     |   A   |
     *     |    +--+----+   =   +--+
     *     +----+--+    |       +--+
     *          |   B   |
     *          |       |
     *          +-------+
     * </pre>
     * 
     * </blockquote>
     */
    public static void intersect(IPolyStream a, IPolyStream b, IWritablePolyStream output) {
        CsgPolyStream aCSG = PolyStreams.claimCSG(a);
        CsgPolyStream bCSG = PolyStreams.claimCSG(b);

        intersect(aCSG, bCSG, output);

        aCSG.release();
        bCSG.release();
    }

    /**
     * Version of {@link #intersect(IPolyStream, IPolyStream, IWritablePolyStream)}
     * to use when you've already built CSG streams. Marks the streams complete but
     * does not release them. Both input streams are modified.
     */
    public static void intersect(CsgPolyStream a, CsgPolyStream b, IWritablePolyStream output) {
        a.complete();
        b.complete();

        a.complete();
        b.complete();

        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);

        a.invert();
        b.invert();

        a.outputRecombinedQuads(output);
        b.outputRecombinedQuads(output);
    }

    /**
     * Output a new mesh representing the union of the input meshes.
     *
     * <blockquote>
     * 
     * <pre>
     *    A.union(B)
     *
     *    +-------+            +-------+
     *    |       |            |       |
     *    |   A   |            |       |
     *    |    +--+----+   =   |       +----+
     *    +----+--+    |       +----+       |
     *         |   B   |            |       |
     *         |       |            |       |
     *         +-------+            +-------+
     * </pre>
     * 
     * </blockquote>
     *
     */
    public static void union(IPolyStream a, IPolyStream b, IWritablePolyStream output) {
        CsgPolyStream aCSG = PolyStreams.claimCSG(a);
        CsgPolyStream bCSG = PolyStreams.claimCSG(b);

        union(aCSG, bCSG, output);

        aCSG.release();
        bCSG.release();
    }

    /**
     * Version of {@link #union(IPolyStream, IPolyStream, IWritablePolyStream)} to
     * use when you've already built CSG streams. Marks the streams complete but
     * does not release them. Both input streams are modified.
     */
    public static void union(CsgPolyStream a, CsgPolyStream b, IWritablePolyStream output) {
        a.complete();
        b.complete();

        // A outside of B bounds can be passed directly to output
        if (outputDisjointA(a, b, output)) {
            // A and B bounds don't overlap, so output all of original b
            output.appendAll(b);
        } else {
            // some potential overlap
            // add union of the overlapping bits,
            // which will include any parts of B that need to be included
            a.clipTo(b);
            b.clipTo(a);
            b.invert();
            b.clipTo(a);
            b.invert();

            a.outputRecombinedQuads(output);
            b.outputRecombinedQuads(output);
        }
    }

    /**
     * Polygons in A that do not intersect with B are sent to output and then
     * deleted. Returns true if A is empty, either because it was empty at the
     * start, or because all A polygons have been deleted.
     */
    private static boolean outputDisjointA(CsgPolyStream a, CsgPolyStream b, IWritablePolyStream output) {
        if (a.origin()) {
            if (b.origin())
                // nominal case
                return outputDisjointAInner(a, b, output);
            else {
                // B is empty, A is not, therefore output all of A and return false
                final IPolygon p = a.reader();
                do {
                    output.appendCopy(p);
                    p.setDeleted();
                } while (a.next());
                return false;
            }
        } else
            // A already empty
            return false;
    }

    /**
     * Handles nominal case when both A and B are non-empty. Assumes A and B are at
     * origin.
     */
    private static boolean outputDisjointAInner(CsgPolyStream a, CsgPolyStream b, IWritablePolyStream output) {
        boolean aIsEmpty = true;

        // compute B mesh bounds
        float bMinX = Float.MAX_VALUE;
        float bMinY = Float.MAX_VALUE;
        float bMinZ = Float.MAX_VALUE;
        float bMaxX = Float.MIN_VALUE;
        float bMaxY = Float.MIN_VALUE;
        float bMaxZ = Float.MIN_VALUE;

        // scoping
        {
            final IPolygon p = b.reader();
            do {
                final int vCount = p.vertexCount();
                for (int i = 1; i < vCount; i++) {
                    final float x = p.getVertexX(i);
                    if (x < bMinX)
                        bMinX = x;
                    else if (x > bMaxX)
                        bMaxX = x;

                    final float y = p.getVertexY(i);
                    if (y < bMinY)
                        bMinY = y;
                    else if (y > bMaxY)
                        bMaxY = y;

                    final float z = p.getVertexZ(i);
                    if (z < bMinZ)
                        bMinZ = z;
                    else if (z > bMaxZ)
                        bMaxZ = z;
                }
            } while (b.next());
        }

        final IPolygon p = a.reader();
        do {
            // Note we don't do a point-by-point test here
            // and instead compute a bounding box for the polygon.
            // This correctly handles case when all poly points
            // are outside the mesh box but plane of poly intersects.
            // Considered using SAT tests developed for collisions boxes
            // but those require tris and re-packing of vertex data. Not worth.

            float pMinX = p.getVertexX(0);
            float pMinY = p.getVertexY(0);
            float pMinZ = p.getVertexZ(0);
            float pMaxX = pMinX;
            float pMaxY = pMinY;
            float pMaxZ = pMinZ;

            final int vCount = p.vertexCount();
            for (int i = 1; i < vCount; i++) {
                final float x = p.getVertexX(i);
                if (x < pMinX)
                    pMinX = x;
                else if (x > pMaxX)
                    pMaxX = x;

                final float y = p.getVertexY(i);
                if (y < pMinY)
                    pMinY = y;
                else if (y > pMaxY)
                    pMaxY = y;

                final float z = p.getVertexZ(i);
                if (z < pMinZ)
                    pMinZ = z;
                else if (z > pMaxZ)
                    pMaxZ = z;
            }

            // For CSG operations we consider a point on the edge to be intersecting.
            if (bMinX <= pMaxX && bMaxX >= pMinX && bMinY <= pMaxY && bMaxY >= pMinY && bMinZ <= pMaxZ
                    && bMaxZ >= pMinZ)
                // potentially intersecting
                aIsEmpty = false;
            else {
                // disjoint
                output.appendCopy(p);
                p.setDeleted();
            }
        } while (a.next());

        return aIsEmpty;
    }
}
