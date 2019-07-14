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

package grondag.xm2.mesh.stream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import grondag.xm2.mesh.polygon.IMutablePolygon;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.mesh.vertex.IVec3f;
import grondag.xm2.mesh.vertex.Vec3f;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;

//TODO:  make this no-alloc.  XM has an uncommitted start
public class CsgPolyRecombinator {
    private static final ThreadLocal<CsgPolyRecombinator> INSTANCES = new ThreadLocal<CsgPolyRecombinator>() {
        @Override
        protected CsgPolyRecombinator initialValue() {
            return new CsgPolyRecombinator();
        }
    };

    /**
     * Implementation of
     * {@link CsgPolyStream#outputRecombinedQuads(IWritablePolyStream)}
     */
    public static void outputRecombinedQuads(CsgPolyStream input, IWritablePolyStream output) {
        INSTANCES.get().doRecombine(input, output);
    }

    private static final int TAG_SHIFT = 32;
    private static final long TAG_MASK = 0xFFFFFFFF00000000L;
    private static final long POLY_MASK = 0x00000000FFFFFFFFL;

    private final LongArrayList tagPolyPairs = new LongArrayList();
    private final IntArrayList polys = new IntArrayList();
    private final IntArrayList joinedVertex = new IntArrayList();

    private CsgPolyRecombinator() {
        // make constructor private
    }

    /**
     * Applies inverted flag and splits higher-order polys before appending to
     * output.
     */
    private void handleOutput(CsgPolyStream input, int polyAddress, IWritablePolyStream output) {
        IPolygon polyA = input.polyA(polyAddress);
        final int vCount = polyA.vertexCount();
        final boolean isFlipped = input.isInverted();
        boolean needsSplit = vCount > 4
                || (vCount == 4
                        && !polyA.isConvex());

        if (!(isFlipped
                || needsSplit)) {
            output.appendCopy(polyA);
            return;
        }

        // invert if needed
        if (isFlipped) {
            polyAddress = input.writerAddress();
            input.setVertexCount(polyA.vertexCount());
            final IMutablePolygon writer = input.writer();
            writer.copyFrom(polyA, true);
            writer.flip();
            input.appendRaw();

            // output recombined should be terminal op, but delete original for consistency
            polyA.setDeleted();

            input.movePolyA(polyAddress);
        }

        // Done unless split to quads or tris is needed
        if (!needsSplit) {
            output.appendCopy(polyA);
            return;
        }

        // if need split and 4 vertices, implies convex, thus need to split to tris
        if (vCount == 4) {
            handleConvex(polyA, output);
            return;
        }

        // higher order poly - split to quads or tris
        int head = vCount - 1;
        int tail = 2;
        IMutablePolygon writer = output.writer();
        output.setVertexCount(4);
        writer.copyFrom(polyA, false);
        writer.copyVertexFrom(0, polyA, head);
        writer.copyVertexFrom(1, polyA, 0);
        writer.copyVertexFrom(2, polyA, 1);
        writer.copyVertexFrom(3, polyA, tail);
        output.append();

        while (head - tail > 1) {
            int size = head - tail == 2 ? 3 : 4;
            output.setVertexCount(size);
            writer.copyFrom(polyA, false);
            writer.copyVertexFrom(0, polyA, head);
            writer.copyVertexFrom(1, polyA, tail);
            writer.copyVertexFrom(2, polyA, ++tail);
            if (size == 4) {
                writer.copyVertexFrom(3, polyA, --head);
                if (writer.isConvex()) {
                    // Oops - output is convex so backtrack and do two tris instead.
                    // Can't call handleConvex because already using writer
                    tail--;
                    head++;

                    output.setVertexCount(3);
                    writer.copyFrom(polyA, false);
                    writer.copyVertexFrom(0, polyA, head);
                    writer.copyVertexFrom(1, polyA, tail);
                    writer.copyVertexFrom(2, polyA, ++tail);
                    output.append();

                    output.setVertexCount(3);
                    writer.copyFrom(polyA, false);
                    writer.copyVertexFrom(0, polyA, head);
                    writer.copyVertexFrom(1, polyA, tail);
                    writer.copyVertexFrom(2, polyA, --head);
                    output.append();
                } else
                    output.append();
            }
        }
    }

    private void handleConvex(IPolygon quad, IWritablePolyStream output) {
        assert quad.vertexCount() == 4;

        assert !quad.isConvex();

        IMutablePolygon writer = output.writer();
        output.setVertexCount(3);
        writer.copyFrom(quad, false);
        writer.copyVertexFrom(0, quad, 3);
        writer.copyVertexFrom(1, quad, 0);
        writer.copyVertexFrom(2, quad, 1);
        output.append();

        output.setVertexCount(3);
        writer.copyFrom(quad, false);
        writer.copyVertexFrom(0, quad, 3);
        writer.copyVertexFrom(1, quad, 1);
        writer.copyVertexFrom(2, quad, 2);
        output.append();
    }

    /**
     * Concise pass-through handler.
     */
    private void handleOutput(CsgPolyStream input, IWritablePolyStream output) {
        if (input.origin()) {
            // output routine can add polys, so need to stop when we get to current end
            final int limit = input.writerAddress();
            IPolygon reader = input.reader();
            do
                handleOutput(input, reader.streamAddress(), output);
            while (input.next()
                    && reader.streamAddress() < limit);
        }
    }

    private void doRecombine(CsgPolyStream input, IWritablePolyStream output) {
        if (!input.origin())
            return;

        tagPolyPairs.clear();
        IPolygon reader = input.reader();
        do {
            long tag = reader.tag();
            assert tag > 0;
            assert !reader.isDeleted();
            tagPolyPairs.add((tag << TAG_SHIFT) | reader.streamAddress());
        } while (input.next());

        final int count = tagPolyPairs.size();

        // if no poly or a single poly at this node, nothing to combine
        // so simply pass the input to the output
        if (count < 2) {
            handleOutput(input, output);
            return;
        }

        if (count == 2) {
            // if only two polys and have same tag, try to combine
            // if not, simply pass through to output
            long pair0 = tagPolyPairs.getLong(0);
            long pair1 = tagPolyPairs.getLong(1);

            assert !input.isDeleted((int) (pair0 & POLY_MASK));
            assert !input.isDeleted((int) (pair1 & POLY_MASK));

            if ((pair0 & TAG_MASK) == (pair1 & TAG_MASK))
                combineTwoPolys(input, output, (int) (POLY_MASK & pair0), (int) (POLY_MASK & pair1));
            else
                handleOutput(input, output);

            return;
        }

        // three or more polys
        // determine if any have the same tags and try to combine if so
        tagPolyPairs.sort(LongComparators.NATURAL_COMPARATOR);

        long pair = tagPolyPairs.getLong(0);
        long lastTag = pair & TAG_MASK;
        assert !input.isDeleted((int) (pair & POLY_MASK));
        polys.add((int) (pair & POLY_MASK));

        for (int i = 1; i < count; i++) {
            pair = tagPolyPairs.getLong(i);

            final long tag = pair & TAG_MASK;
            if (tag != lastTag) {
                combinePolys(input, output);
                polys.clear();
                lastTag = tag;
            }

            assert !input.isDeleted((int) (pair & POLY_MASK));
            polys.add((int) (pair & POLY_MASK));
        }

        combinePolys(input, output);
        polys.clear();

    }

    /**
     * Handles special case when there are only 2 polygons to join - avoids building
     * a map. For volcano terrain, this is about 15% of the cases involving a
     * potential join (more than 1 poly).
     * <p>
     * 
     * Assumes both polys have the same tag.
     */
    private void combineTwoPolys(CsgPolyStream input, IWritablePolyStream output, int polyAddress0, int polyAddress1) {
        IPolygon polyA = input.polyA(polyAddress0);
        IPolygon polyB = input.polyB(polyAddress1);

        final int aLimit = polyA.vertexCount();
        final int bLimit = polyB.vertexCount();

        assert !polyA.isDeleted();
        assert !polyB.isDeleted();

        for (int a = 0; a < aLimit; a++) {
            final float aX = polyA.x(a);
            final float aY = polyA.y(a);
            final float aZ = polyA.z(a);

            for (int b = 0; b < bLimit; b++) {
                final float bX = polyB.x(b);
                final float bY = polyB.y(b);
                final float bZ = polyB.z(b);

                if (aX == bX
                        && aY == bY
                        && aZ == bZ) {

                    final int newPolyAddress = joinAtVertex(input, polyA, a, polyB, b);
                    if (newPolyAddress == IPolygon.NO_LINK_OR_TAG) {
                        // join failed
                        assert !polyA.isDeleted();
                        assert !polyB.isDeleted();
                        break;
                    } else {
                        assert polyA.isDeleted();
                        assert polyB.isDeleted();
                        handleOutput(input, newPolyAddress, output);
                        return;
                    }
                }
            }
        }

        // if get here, no matching vertex or join failed
        handleOutput(input, polyAddress0, output);
        handleOutput(input, polyAddress1, output);

    }

    private int joinAtVertex(CsgPolyStream input, int addressA, int addressB, Vec3f v) {
        IPolygon polyA = input.polyA(addressA);
        IPolygon polyB = input.polyB(addressB);

        final int aTargetIndex = polyA.indexForVertex(v);
        // shouldn't happen, but won't work if does
        if (aTargetIndex == IPolygon.VERTEX_NOT_FOUND)
            return IPolygon.NO_LINK_OR_TAG;

        final int bTargetIndex = polyB.indexForVertex(v);
        // shouldn't happen, but won't work if does
        if (bTargetIndex == IPolygon.VERTEX_NOT_FOUND)
            return IPolygon.NO_LINK_OR_TAG;

        return joinAtVertex(input, polyA, aTargetIndex, polyB, bTargetIndex);
    }

    private int joinAtVertex(CsgPolyStream input, IPolygon polyA, int aTargetIndex, IPolygon polyB, int bTargetIndex) {
        assert polyA.x(aTargetIndex) == polyB.x(bTargetIndex);
        assert polyA.y(aTargetIndex) == polyB.y(bTargetIndex);
        assert polyA.z(aTargetIndex) == polyB.z(bTargetIndex);

        final int aSize = polyA.vertexCount();
        final int bSize = polyB.vertexCount();
        final int aMaxIndex = aSize - 1;
        final int bMaxIndex = bSize - 1;

        final int aAfterTargetIndex = aTargetIndex == aMaxIndex ? 0 : aTargetIndex + 1;
        final int aBeforeTargetIndex = aTargetIndex == 0 ? aMaxIndex : aTargetIndex - 1;

        final int bAfterTargetIndex = bTargetIndex == bMaxIndex ? 0 : bTargetIndex + 1;
        final int bBeforeTargetIndex = bTargetIndex == 0 ? bMaxIndex : bTargetIndex - 1;

        /** Shared vertex that comes first on A polygon, is second shared vertex on B */
        int aFirstSharedIndex;

        /** Shared vertex that comes first on B polygon, is second shared vertex on A */
        int bFirstSharedIndex;

        /**
         * Shared vertex that comes second on A polygon, is first shared vertex on B.
         */
        int aSecondSharedIndex;

        /**
         * Shared vertex that comes second on B polygon, is first shared vertex on A.
         */
        int bSecondSharedIndex;

        /** Vertex on A polygon before the first shared A vertex */
        int aBeforeSharedIndex;

        /** Vertex on B polygon before the first shared B vertex */
        int bBeforeSharedIndex;

        /** Vertex on A polygon after the second shared A vertex */
        int aAfterSharedIndex;

        /** Vertex on B polygon after the second shared B vertex */
        int bAfterSharedIndex;

        // look for a second matching vertex on either side of known shared vertex
        if (polyA.x(aAfterTargetIndex) == polyB.x(bBeforeTargetIndex)
                && polyA.y(aAfterTargetIndex) == polyB.y(bBeforeTargetIndex)
                && polyA.z(aAfterTargetIndex) == polyB.z(bBeforeTargetIndex)) {
            aFirstSharedIndex = aTargetIndex;
            aSecondSharedIndex = aAfterTargetIndex;
            bFirstSharedIndex = bBeforeTargetIndex;
            bSecondSharedIndex = bTargetIndex;
            aBeforeSharedIndex = aBeforeTargetIndex;
            bBeforeSharedIndex = bFirstSharedIndex == 0 ? bMaxIndex : bFirstSharedIndex - 1;
            aAfterSharedIndex = aSecondSharedIndex == aMaxIndex ? 0 : aSecondSharedIndex + 1;
            bAfterSharedIndex = bAfterTargetIndex;
        } else if (polyA.x(aBeforeTargetIndex) == polyB.x(bAfterTargetIndex)
                && polyA.y(aBeforeTargetIndex) == polyB.y(bAfterTargetIndex)
                && polyA.z(aBeforeTargetIndex) == polyB.z(bAfterTargetIndex)) {
            aFirstSharedIndex = aBeforeTargetIndex;
            aSecondSharedIndex = aTargetIndex;
            bFirstSharedIndex = bTargetIndex;
            bSecondSharedIndex = bAfterTargetIndex;
            aBeforeSharedIndex = aFirstSharedIndex == 0 ? aMaxIndex : aFirstSharedIndex - 1;
            bBeforeSharedIndex = bBeforeTargetIndex;
            aAfterSharedIndex = aAfterTargetIndex;
            bAfterSharedIndex = bSecondSharedIndex == bMaxIndex ? 0 : bSecondSharedIndex + 1;
        } else {
            return IPolygon.NO_LINK_OR_TAG;
        }

        /**
         * positive values are A poly vertex index + 1 negative values are negative (B
         * poly vertex index + 1) zero values have not been populated
         */
        joinedVertex.clear();

        for (int a = 0; a < aSize; a++) {

            if (a == aFirstSharedIndex) {
                // if vertex is on the same line as prev and next vertex, leave it out.
                if (!IVec3f.isPointOnLine(polyA.x(aFirstSharedIndex), polyA.y(aFirstSharedIndex),
                        polyA.z(aFirstSharedIndex), polyA.x(aBeforeSharedIndex), polyA.y(aBeforeSharedIndex),
                        polyA.z(aBeforeSharedIndex), polyB.x(bAfterSharedIndex), polyB.y(bAfterSharedIndex),
                        polyB.z(bAfterSharedIndex))) {
                    joinedVertex.add(a + 1);
                }

                // add b vertexes except two bQuad vertexes in common with A
                for (int b = 0; b < bSize - 2; b++) {
                    int bIndex = bAfterSharedIndex + b;
                    if (bIndex > bMaxIndex)
                        bIndex -= bSize;
                    joinedVertex.add(-(bIndex + 1));
                }
            } else if (a == aSecondSharedIndex) {
                // if vertex is on the same line as prev and next vertex, leave it out
                if (!IVec3f.isPointOnLine(polyA.x(aSecondSharedIndex), polyA.y(aSecondSharedIndex),
                        polyA.z(aSecondSharedIndex), polyA.x(aAfterSharedIndex), polyA.y(aAfterSharedIndex),
                        polyA.z(aAfterSharedIndex), polyB.x(bBeforeSharedIndex), polyB.y(bBeforeSharedIndex),
                        polyB.z(bBeforeSharedIndex))) {
                    joinedVertex.add(a + 1);
                }
            } else {
                joinedVertex.add(a + 1);
            }
        }

        final int size = joinedVertex.size();

        if (size < 3) {
            assert false : "Bad polygon formation during CSG recombine.";
            return IPolygon.NO_LINK_OR_TAG;
        }

        // actually build the new quad
        final int result = input.writerAddress();
        IMutablePolygon writer = input.writer();
        input.setVertexCount(size);
        writer.copyFrom(polyA, false);
        writer.tag(polyA.tag());
        for (int i = 0; i < size; i++) {
            int j = joinedVertex.getInt(i);
            if (j > 0)
                writer.copyVertexFrom(i, polyA, j - 1);
            else
                writer.copyVertexFrom(i, polyB, -j - 1);
        }

        input.appendRaw();

        // mark inputs deleted
        polyA.setDeleted();
        polyB.setDeleted();

        return result;

    }

    /**
     * Assumes polys in list all have same tag.
     */
    private void combinePolys(CsgPolyStream input, IWritablePolyStream output) {
        assert !polys.isEmpty();

        final int count = polys.size();

        if (count == 1)
            handleOutput(input, polys.getInt(0), output);
        else if (count == 2)
            combineTwoPolys(input, output, polys.getInt(0), polys.getInt(1));
        else
            combinePolysInner(input, output);
    }

    private static void addPolyToVertexMap(HashMap<Vec3f, IntArrayList> vertexMap, IPolygon poly) {
        final int limit = poly.vertexCount();
        for (int i = 0; i < limit; i++) {
            Vec3f v = poly.getPos(i);
            IntArrayList bucket = vertexMap.get(v);
            if (bucket == null) {
                bucket = new IntArrayList();
                vertexMap.put(v, bucket);
            }
            bucket.add(poly.streamAddress());
        }
    }

    /**
     * For use during second phase of combined - will not create buckets that are
     * not found. Assumes these have been deleted because only had a single poly in
     * them.
     */
    private static void addPolyToVertexMapGently(HashMap<Vec3f, IntArrayList> vertexMap, IPolygon poly) {
        final int limit = poly.vertexCount();
        for (int i = 0; i < limit; i++) {
            Vec3f v = poly.getPos(i);
            IntArrayList bucket = vertexMap.get(v);
            if (bucket != null)
                bucket.add(poly.streamAddress());
        }
    }

    private static void removePolyFromVertexMap(HashMap<Vec3f, IntArrayList> vertexMap, IPolygon poly, Vec3f excludingVertex) {
        final int limit = poly.vertexCount();
        for (int i = 0; i < limit; i++) {
            Vec3f v = poly.getPos(i);
            if (excludingVertex.equals(v))
                continue;

            IntArrayList bucket = vertexMap.get(v);

            if (bucket == null)
                continue;

            boolean check = bucket.rem(poly.streamAddress());
            assert check;
        }
    }

    /**
     * For three or more polys with same tag.
     *
     * PERF: consider making polysIn a set in the caller
     */
    private void combinePolysInner(CsgPolyStream input, IWritablePolyStream output) {
        /**
         * Index of all polys by vertex
         */
        HashMap<Vec3f, IntArrayList> vertexMap = new HashMap<>();

        {
            final int limit = polys.size();
            for (int i = 0; i < limit; i++)
                addPolyToVertexMap(vertexMap, input.polyA(polys.getInt(i)));
        }

        /**
         * Cleared at top of each loop and set to true if and only if new polys are
         * created due to joins AND the line/quad/vertex map has at least one new value
         * added to it.
         * <p>
         * 
         * The second condition avoids making another pass when all the joined polys
         * have edges that are outside edges (and thus can't be joined) or the edge is
         * no longer being tracked because fewer than two polys reference it.
         */
        boolean potentialMatchesRemain = true;

        while (potentialMatchesRemain) {
            potentialMatchesRemain = false;

            Iterator<Entry<Vec3f, IntArrayList>> it = vertexMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Vec3f, IntArrayList> entry = it.next();
                IntArrayList bucket = entry.getValue();
                if (bucket.size() < 2) {
                    // nothing to simplify here
                    it.remove();
                } else if (bucket.size() == 2) {
                    // eliminate T junctions
                    Vec3f v = entry.getKey();
                    int first = bucket.getInt(0);
                    int second = bucket.getInt(1);
                    int newPoly = joinAtVertex(input, first, second, v);
                    if (newPoly != IPolygon.NO_LINK_OR_TAG) {
                        potentialMatchesRemain = true;
                        // we won't see a CME because not removing any vertices at this point except via
                        // the iterator
                        it.remove();

                        boolean check = polys.rem(first);
                        assert check;
                        removePolyFromVertexMap(vertexMap, input.polyA(first), v);

                        check = polys.rem(second);
                        assert check;
                        removePolyFromVertexMap(vertexMap, input.polyA(second), v);

                        polys.add(newPoly);
                        addPolyToVertexMapGently(vertexMap, input.polyA(newPoly));
                    }
                }
            }
        }

        final int limit = polys.size();
        for (int i = 0; i < limit; i++)
            handleOutput(input, polys.getInt(i), output);
    }
}
