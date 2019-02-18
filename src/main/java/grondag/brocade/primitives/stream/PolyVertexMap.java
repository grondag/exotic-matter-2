package grondag.brocade.primitives.stream;

import java.util.HashMap;

import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;
import grondag.exotic_matter.varia.intstream.Float3Int1Map;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class PolyVertexMap
{
    final Float3Int1Map vertexMap = Float3Int1Map.claim();
    final IntArrayList vertexLinks = new IntArrayList();
    
    public void clear()
    {
        vertexMap.clear();
        vertexLinks.clear();
    }

    public void addPoly(IPolygon poly)
    {
        final int limit = poly.vertexCount();
        for(int i = 0; i < limit; i++)
        {
            Vec3f v = poly.getPos(i);
            IntArrayList bucket = vertexMap.get(v);
            if(bucket == null)
            {
                bucket = new IntArrayList();
                vertexMap.put(v, bucket);
            }
            bucket.add(poly.streamAddress());
        }
    }
    
    /**
     * For use during second phase of combined - will not create buckets that are not found.
     * Assumes these have been deleted because only had a single poly in them.
     */
    public void addPolyGently(IPolygon poly)
    {
        final int limit = poly.vertexCount();
        for(int i = 0; i < limit; i++)
        {
            Vec3f v = poly.getPos(i);
            IntArrayList bucket = vertexMap.get(v);
            if(bucket != null)
                bucket.add(poly.streamAddress());
        }
    }

//    public void removePoly(IPolygon poly, Vec3f excludingVertex)
//    {
//        final int limit = poly.vertexCount();
//        for(int i = 0; i < limit; i++)
//        {
//            Vec3f v = poly.getPos(i);
//            if(excludingVertex.equals(v))
//                continue;
//            
//            IntArrayList bucket = vertexMap.get(v);
//            
//            if(bucket == null)
//                continue;
//            
//            boolean check  = bucket.rem(poly.streamAddress());
//            assert check;
//        }
//    }
    
    public void removePoly(int polyAddress)
    {
        // TODO Auto-generated method stub
        
    }
    
    public class Cursor
    {

        public boolean origin()
        {
            // TODO Auto-generated method stub
            return true;
            
        }
        
        public boolean next()
        {
            // TODO Auto-generated method stub
            return true;
        }

        public int polyCount()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public void remove()
        {
            // TODO Auto-generated method stub
            
        }

        public int firstPolyIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int secondPolyIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int firstVertexIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int secondVertexIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }
    }
    
    final Cursor cursor = new Cursor();
    
    public Cursor cursor()
    {
        return cursor;
    }
    
}
