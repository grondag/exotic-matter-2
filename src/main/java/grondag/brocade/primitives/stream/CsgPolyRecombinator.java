package grondag.brocade.primitives.stream;

import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.vertex.IVec3f;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;

public class CsgPolyRecombinator
{
    private static final ThreadLocal<CsgPolyRecombinator> INSTANCES = new ThreadLocal<CsgPolyRecombinator>()
    {
        @Override
        protected CsgPolyRecombinator initialValue()
        {
            return new CsgPolyRecombinator();
        }
    };
    
    /**
     * Implementation of {@link CsgPolyStream#outputRecombinedQuads(IWritablePolyStream)}
     */
    public static void outputRecombinedQuads(CsgPolyStream input, IWritablePolyStream output)
    {
        INSTANCES.get().doRecombine(input, output);
    }
    
    private static final int TAG_SHIFT = 32;
    private static final long TAG_MASK = 0xFFFFFFFF00000000L;
    private static final long POLY_MASK = 0x00000000FFFFFFFFL;
    
    private final LongArrayList tagPolyPairs = new LongArrayList();
    private final IntArrayList polys = new IntArrayList();
    private final IntArrayList joinedVertex = new IntArrayList();
    private final PolyVertexMap vertexMap = new PolyVertexMap();
    
    private CsgPolyRecombinator()
    {
        // make constructor private
    }
    
    /**
     * Applies inverted flag and splits higher-order polys before appending to output.
     */
    private void handleOutput(CsgPolyStream input, int polyAddress, IWritablePolyStream output)
    {
        IPolygon polyA = input.polyA(polyAddress);
        final int vCount = polyA.vertexCount();
        final boolean isFlipped = input.isInverted();
        boolean needsSplit  = vCount > 4 || (vCount == 4 && !polyA.isConvex());
        
        if(!(isFlipped || needsSplit))
        {
            output.appendCopy(polyA);
            return;
        }
        
        // invert if needed
        if(isFlipped)
        {
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
        if(!needsSplit)
        {
            output.appendCopy(polyA);
            return;
        }

        // if need split and 4 vertices, implies convex, thus need to split to tris
        if(vCount == 4)
        {
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

        while(head - tail > 1)
        {
            int size =  head - tail == 2 ? 3 : 4;
            output.setVertexCount(size);
            writer.copyFrom(polyA, false);
            writer.copyVertexFrom(0, polyA, head);
            writer.copyVertexFrom(1, polyA, tail);
            writer.copyVertexFrom(2, polyA, ++tail);
            if(size == 4)
            {
                writer.copyVertexFrom(3, polyA,--head);
                if(writer.isConvex())
                {
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
                }
                else
                    output.append();
            }
        }
    }
    
    private void handleConvex(IPolygon quad, IWritablePolyStream output)
    {
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
    private void handleOutput(CsgPolyStream input, IWritablePolyStream output)
    {
        if(input.origin())
        {
            // output routine can add polys, so need to stop when we get to current end
            final int limit = input.writerAddress();
            IPolygon reader = input.reader();
            do 
                handleOutput(input, reader.streamAddress(), output);
            while(input.next() && reader.streamAddress() < limit);
        }
    }
    
    private void doRecombine(CsgPolyStream input, IWritablePolyStream output)
    {
        if(!input.origin())
            return;
        
        tagPolyPairs.clear();
        IPolygon reader = input.reader();
        do 
        {
            long tag = reader.getTag();
            assert tag > 0;
            assert !reader.isDeleted();
            tagPolyPairs.add((tag << TAG_SHIFT) | reader.streamAddress());
        } while(input.next());
        
        final int count = tagPolyPairs.size();
        
        // if no poly or a single poly at this node, nothing to combine
        // so simply pass the input to the output
        if(count < 2)
        {
            handleOutput(input, output);
            return;
        }
        
        if(count == 2)
        {
            // if only two polys and have same tag, try to combine
            // if not, simply pass through to output
            long pair0 = tagPolyPairs.getLong(0);
            long pair1 = tagPolyPairs.getLong(1);
            
            assert !input.isDeleted((int)(pair0 & POLY_MASK));
            assert !input.isDeleted((int)(pair1 & POLY_MASK));
            
            if ((pair0 & TAG_MASK) == (pair1 & TAG_MASK))
                combineTwoPolys(input, output, (int)(POLY_MASK & pair0), (int)(POLY_MASK & pair1));
            else
                handleOutput(input, output);
            
            return;
        }
        
        // three or more polys
        // determine if any have the same tags and try to combine if so
        tagPolyPairs.sort(LongComparators.NATURAL_COMPARATOR);
        
        long pair = tagPolyPairs.getLong(0);
        long lastTag = pair & TAG_MASK;
        assert !input.isDeleted((int)(pair & POLY_MASK));
        polys.add((int)(pair & POLY_MASK));
        
        for(int i = 1; i < count; i++)
        {
            pair = tagPolyPairs.getLong(i);
            
            final long tag = pair & TAG_MASK;
            if(tag != lastTag)
            {
                combinePolys(input, output);
                polys.clear();
                lastTag = tag;
            }
            
            assert !input.isDeleted((int)(pair & POLY_MASK));
            polys.add((int)(pair & POLY_MASK));
        }
        
        combinePolys(input, output);
        polys.clear();
        
    }
    
    /**
     * Handles special case when there are only 2 polygons to join - avoids building a map.
     * For volcano terrain, this is about 15% of the cases involving a potential join (more than 1 poly).<p>
     * 
     * Assumes both polys have the same tag.
     */
    private void combineTwoPolys(CsgPolyStream input, IWritablePolyStream output, int polyAddress0, int polyAddress1)
    {
        IPolygon polyA = input.polyA(polyAddress0);
        IPolygon polyB = input.polyB(polyAddress1);
        
        final int aLimit = polyA.vertexCount();
        final int bLimit = polyB.vertexCount();
        
        assert !polyA.isDeleted();
        assert !polyB.isDeleted();

        for(int a = 0; a < aLimit; a++)
        {
            final float aX = polyA.getVertexX(a);
            final float aY = polyA.getVertexY(a);
            final float aZ = polyA.getVertexZ(a);
            
            for(int b = 0; b < bLimit; b++)
            {
                final float bX = polyB.getVertexX(b);
                final float bY = polyB.getVertexY(b);
                final float bZ = polyB.getVertexZ(b);
                
                if(aX == bX && aY == bY && aZ == bZ)
                {
                    
                    
                    final int newPolyAddress = joinAtVertex(input, polyA, a, polyB, b);
                    if(newPolyAddress == IPolygon.NO_LINK_OR_TAG)
                    {
                        // join failed
                        assert !polyA.isDeleted();
                        assert !polyB.isDeleted();
                        break;
                    }
                    else
                    {
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
    
    private int joinAtVertex(CsgPolyStream input, int addressA, int aTargetIndex, int addressB, int bTargetIndex)
    {
        IPolygon polyA = input.polyA(addressA);
        IPolygon polyB = input.polyB(addressB);
        
        return joinAtVertex(input, polyA, aTargetIndex, polyB, bTargetIndex);
    }
    
    private int joinAtVertex(CsgPolyStream input, IPolygon polyA, int aTargetIndex, IPolygon polyB, int bTargetIndex)
    {
        assert polyA.getVertexX(aTargetIndex) ==  polyB.getVertexX(bTargetIndex);
        assert polyA.getVertexY(aTargetIndex) ==  polyB.getVertexY(bTargetIndex);
        assert polyA.getVertexZ(aTargetIndex) ==  polyB.getVertexZ(bTargetIndex);
        
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
       
        /** Shared vertex that comes second on A polygon, is first shared vertex on B. */
        int aSecondSharedIndex;
        
        /** Shared vertex that comes second on B polygon, is first shared vertex on A. */
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
        if(polyA.getVertexX(aAfterTargetIndex) == polyB.getVertexX(bBeforeTargetIndex)
                && polyA.getVertexY(aAfterTargetIndex) == polyB.getVertexY(bBeforeTargetIndex)
                && polyA.getVertexZ(aAfterTargetIndex) == polyB.getVertexZ(bBeforeTargetIndex))
        {
            aFirstSharedIndex = aTargetIndex;
            aSecondSharedIndex = aAfterTargetIndex;
            bFirstSharedIndex = bBeforeTargetIndex;
            bSecondSharedIndex = bTargetIndex;
            aBeforeSharedIndex = aBeforeTargetIndex;
            bBeforeSharedIndex = bFirstSharedIndex == 0 ? bMaxIndex : bFirstSharedIndex - 1;
            aAfterSharedIndex = aSecondSharedIndex == aMaxIndex ? 0 : aSecondSharedIndex + 1;
            bAfterSharedIndex = bAfterTargetIndex;
        }
        else if(polyA.getVertexX(aBeforeTargetIndex) == polyB.getVertexX(bAfterTargetIndex)
            && polyA.getVertexY(aBeforeTargetIndex) == polyB.getVertexY(bAfterTargetIndex)
            && polyA.getVertexZ(aBeforeTargetIndex) == polyB.getVertexZ(bAfterTargetIndex))
        {
            aFirstSharedIndex = aBeforeTargetIndex;
            aSecondSharedIndex = aTargetIndex;
            bFirstSharedIndex =  bTargetIndex;
            bSecondSharedIndex = bAfterTargetIndex;
            aBeforeSharedIndex = aFirstSharedIndex == 0 ? aMaxIndex : aFirstSharedIndex - 1;
            bBeforeSharedIndex = bBeforeTargetIndex;
            aAfterSharedIndex = aAfterTargetIndex;
            bAfterSharedIndex = bSecondSharedIndex == bMaxIndex ? 0 : bSecondSharedIndex + 1;
        }
        else
        {
            return IPolygon.NO_LINK_OR_TAG;
        }
        
        /** positive values are A poly vertex index + 1
         *  negative values are negative (B poly vertex index + 1)
         *  zero values have not been populated
         */
        joinedVertex.clear();
        
        for(int a = 0; a < aSize; a++)
        {
            
            if(a == aFirstSharedIndex)
            {
                //if vertex is on the same line as prev and next vertex, leave it out.
                if(!IVec3f.isPointOnLine(
                        polyA.getVertexX(aFirstSharedIndex), polyA.getVertexY(aFirstSharedIndex), polyA.getVertexZ(aFirstSharedIndex),
                        polyA.getVertexX(aBeforeSharedIndex), polyA.getVertexY(aBeforeSharedIndex), polyA.getVertexZ(aBeforeSharedIndex),
                        polyB.getVertexX(bAfterSharedIndex), polyB.getVertexY(bAfterSharedIndex), polyB.getVertexZ(bAfterSharedIndex)))
                {
                    joinedVertex.add(a + 1);
                }

                // add b vertexes except two bQuad vertexes in common with A
                for(int b = 0; b < bSize - 2; b++)
                {
                    int bIndex = bAfterSharedIndex + b;
                    if(bIndex > bMaxIndex) bIndex -= bSize;
                    joinedVertex.add(-(bIndex + 1));
                }
            }
            else if(a == aSecondSharedIndex)
            {
                //if vertex is on the same line as prev and next vertex, leave it out
                if(!IVec3f.isPointOnLine(
                        polyA.getVertexX(aSecondSharedIndex), polyA.getVertexY(aSecondSharedIndex), polyA.getVertexZ(aSecondSharedIndex),
                        polyA.getVertexX(aAfterSharedIndex), polyA.getVertexY(aAfterSharedIndex), polyA.getVertexZ(aAfterSharedIndex),
                        polyB.getVertexX(bBeforeSharedIndex), polyB.getVertexY(bBeforeSharedIndex), polyB.getVertexZ(bBeforeSharedIndex)))
                {
                    joinedVertex.add(a + 1);
                }
            }
            else
            {
                joinedVertex.add(a + 1);
           }
        }   
        
        final int size = joinedVertex.size();
        
        if(size < 3)
        {
            assert false : "Bad polygon formation during CSG recombine.";
            return IPolygon.NO_LINK_OR_TAG;
        }
        
        // actually build the new quad
        final int result = input.writerAddress();
        IMutablePolygon writer = input.writer();
        input.setVertexCount(size);
        writer.copyFrom(polyA, false);
        writer.setTag(polyA.getTag());
        for(int i = 0; i < size; i++)
        {
            int j = joinedVertex.getInt(i);
            if(j > 0)
                writer.copyVertexFrom(i, polyA, j - 1);
            else
                writer.copyVertexFrom(i, polyB, -j - 1);
        }
        
        input.appendRaw();
        
        //mark inputs deleted
        polyA.setDeleted();
        polyB.setDeleted();
        
        return result;
        
    }

    /**
     * Assumes polys in list all have same tag.
     */
    private void combinePolys(CsgPolyStream input, IWritablePolyStream output)
    {
        assert !polys.isEmpty();
        
        final int count = polys.size();
        
        if(count == 1)
            handleOutput(input, polys.getInt(0), output);
        else if(count == 2)
            combineTwoPolys(input, output, polys.getInt(0), polys.getInt(1));
        else
            combinePolysInner(input, output);
    }
    
    
    /**
     * For three or more polys with same tag.
     */
    private void combinePolysInner(CsgPolyStream input, IWritablePolyStream output)
    {
        vertexMap.clear();
        {
            final int limit = polys.size();
            for(int i = 0; i < limit; i++)
                vertexMap.addPoly(input.polyA(polys.getInt(i)));
        }
        
        /** 
         * Cleared at top of each loop and set to true if and only if 
         * new polys are created due to joins AND the line/quad/vertex map
         * has at least one new value added to it. <p>
         * 
         * The second condition avoids making another pass when all the
         * joined polys have edges that are outside edges (and thus can't
         * be joined) or the edge is no longer being tracked because fewer
         * than two polys reference it.
         */
        boolean potentialMatchesRemain = true;
        PolyVertexMap.Cursor cursor = vertexMap.cursor();
        
        while(potentialMatchesRemain)
        {
            potentialMatchesRemain = false;
            
            if(cursor.origin()) do
            {
                if(cursor.polyCount() < 2)
                {
                    // nothing to simplify here
                    cursor.remove();
                }
                else if(cursor.polyCount() == 2)
                {
                    // eliminate T junctions
                    int firstPoly = cursor.firstPolyIndex();
                    int secondPoly = cursor.secondPolyIndex();
                    int newPoly = joinAtVertex(input, firstPoly, cursor.firstVertexIndex(), secondPoly, cursor.secondVertexIndex());
                    if(newPoly != IPolygon.NO_LINK_OR_TAG)
                    {
                        potentialMatchesRemain = true;
                        cursor.remove();
                        
                        boolean check = polys.rem(firstPoly);
                        assert check;
                        vertexMap.removePoly(firstPoly);
                        
                        check = polys.rem(secondPoly);
                        assert check;
                        vertexMap.removePoly(secondPoly);
                        
                        polys.add(newPoly);
                        vertexMap.addPolyGently(input.polyA(newPoly));
                    }
                }
            } while(cursor.next());
        }
        
        final int limit = polys.size();
        for(int i = 0; i < limit; i++)
            handleOutput(input, polys.getInt(i), output);
    }
}
