package grondag.brocade.painting;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.primitives.QuadHelper;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.stream.IMutablePolyStream;
import grondag.exotic_matter.world.FaceCorner;

/**
 * Helper class to splits UV-locked quads into four quadrants at the u,v = 0.5, 0.5
 * point (if needed) and to test if quads are already within a single quadrant.
 */
public class QuadrantSplitter
{
    @Nullable
    public final static FaceCorner uvQuadrant(IMutablePolygon quad, int layerIndex)
    {
        final int vCount = quad.vertexCount();
        
        float uMin = quad.getVertexU(layerIndex, 0);
        float uMax = uMin;
        float vMin = quad.getVertexV(layerIndex, 0);
        float vMax = vMin;
        
        for(int i = 1; i < vCount; i++)
        {
            float u = quad.getVertexU(layerIndex, i);
            float v = quad.getVertexV(layerIndex, i);
            if(u < uMin) 
                uMin = u;
            else if(u > uMax)
                uMax = u;
            
            if(v < vMin) 
                vMin = v;
            else if(v > vMax)
                vMax = v;
        }
        
        // note that v is inverted from FaceCorner semantics.
        // (high v = bottom, low v = top)
        if(vertexType(uMin) == LOW)
        {
            // u is left 
            if(vertexType(uMax) == HIGH)
                // spanning
                return null;
            
            else if(vertexType(vMin) == LOW)
            {
                // v is low
                
                if(vertexType(vMax) == HIGH)
                    // spanning
                    return null;
                else
                    return FaceCorner.TOP_LEFT;
            }
            else
                // v is high
                return FaceCorner.BOTTOM_LEFT;
        }
        else
        {
            // u is right 
            if(vertexType(vMin) == LOW)
            {
                // v is low
                
                if(vertexType(vMax) == HIGH)
                    // spanning
                    return null;
                else
                    return FaceCorner.TOP_RIGHT;
            }
            else
                // v is high
                return FaceCorner.BOTTOM_RIGHT;
        }
    }
    
    /**
     * Stream editor must be at split position. <br>
     * May move editor but returns editor to position at call time if it does so.<br>
     * If split occurs, poly at editor position will be marked deleted.<p>
     * 
     * May also move reader, and does not restore reader position if it does so.
     */
    public static final void split(IMutablePolyStream stream, int layerIndex)
    {
        final int editorAddress = stream.editorAddress();
        final IPolygon reader = stream.reader(editorAddress);
        
        int lowCount = 0;
        int highCount = 0;
        final int vCount = reader.vertexCount();
        
        for(int i = 0; i < vCount; i++)
        {
            final int t = vertexType(reader.getVertexU(layerIndex, i));
            if(t == HIGH)
                highCount++;
            else if(t == LOW)
                lowCount++;
        }
        
        if(lowCount == 0)
            // all on on high side
            splitV(stream, editorAddress, true, layerIndex);
        else if(highCount == 0)
            // all on low side
            splitV(stream, editorAddress, false, layerIndex);
        else
        {
            // spanning
            IMutablePolygon writer = stream.writer();
            
            int highAddress = stream.writerAddress();
            int iHighVertex = 0;
            stream.setVertexCount(highCount + 2);
            writer.copyFrom(reader, false);
            stream.append();
            
            int lowAddress = stream.writerAddress();
            int iLowVertex = 0;
            stream.setVertexCount(lowCount + 2);
            writer.copyFrom(reader, false);
            stream.append();
            
            int iThis = vCount - 1;
            float uThis = reader.getVertexU(layerIndex, iThis);
            int thisType = vertexType(uThis);
                    
            for(int iNext = 0; iNext < vCount; iNext++)
            {
                final float uNext = reader.getVertexU(layerIndex, iNext);
                final int nextType = vertexType(uNext);
                
                if(thisType == EDGE)
                {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                }
                else if(thisType == HIGH)
                {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    if(nextType == LOW)
                    {
                        final float dist = (0.5f - uThis) / (uNext - uThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                }
                else
                {
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                    if(nextType == HIGH)
                    {
                        final float dist = (0.5f - uThis) / (uNext - uThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                }
                iThis = iNext;
                uThis = uNext;
                thisType = nextType;
            }
            
            reader.setDeleted();
            
            splitV(stream, highAddress, true, layerIndex);
            splitV(stream, lowAddress, false, layerIndex);
            
            // restore editor position if we moved it
            if(stream.editorAddress() != editorAddress)
                stream.moveEditor(editorAddress);
        }
    }
    
    private static final void splitV(IMutablePolyStream stream, int polyAddress, boolean isHighU, int layerIndex)
    {
        final IPolygon reader = stream.reader(polyAddress);
        
        int lowCount = 0;
        int highCount = 0;
        final int vCount = reader.vertexCount();
        
        for(int i = 0; i < vCount; i++)
        {
            final int t = vertexType(reader.getVertexV(layerIndex, i));
            if(t == HIGH)
                highCount++;
            else if(t == LOW)
                lowCount++;
        }
        
        if(lowCount == 0)
            // all on on high side
            return;
        else if(highCount == 0)
            // all on low side
            return;
        else
        {
            // spanning
            IMutablePolygon writer = stream.writer();
            
            int highAddress = stream.writerAddress();
            int iHighVertex = 0;
            stream.setVertexCount(highCount + 2);
            writer.copyFrom(reader, false);
            stream.append();
            
            int lowAddress = stream.writerAddress();
            int iLowVertex = 0;
            stream.setVertexCount(lowCount + 2);
            writer.copyFrom(reader, false);
            stream.append();
            
            int iThis = vCount - 1;
            float vThis = reader.getVertexV(layerIndex, iThis);
            int thisType = vertexType(vThis);
                    
            for(int iNext = 0; iNext < vCount; iNext++)
            {
                final float vNext = reader.getVertexV(layerIndex, iNext);
                final int nextType = vertexType(vNext);
                
                if(thisType == EDGE)
                {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                }
                else if(thisType == HIGH)
                {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    if(nextType == LOW)
                    {
                        final float dist = (0.5f - vThis) / (vNext - vThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                }
                else
                {
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                    if(nextType == HIGH)
                    {
                        final float dist = (0.5f - vThis) / (vNext - vThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                }
                iThis = iNext;
                vThis = vNext;
                thisType = nextType;
            }
            
            reader.setDeleted();
        }
    }
    
    private static final int EDGE = 0;
    private static final int HIGH = 1;
    private static final int LOW = 2;
    
    private final static int vertexType(float uvCoord)
    {
        if(uvCoord >= 0.5f - QuadHelper.EPSILON)
        {
            if(uvCoord <= 0.5f + QuadHelper.EPSILON)
                return EDGE;
            else
                return HIGH;
        }
        else
        {
            // < 0.5f - QuadHelper.EPSILON
            return LOW;
        }
    }
}
