package grondag.exotic_matter;

import java.util.Random;

import org.junit.Test;

import grondag.exotic_matter.model.CSG.CSGPlane;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;

public class CSGPlanPerf
{

    @Test
    public void test()
    {
        Random r = new Random(45);
        
        Vec3f.Mutable vec = new Vec3f.Mutable(r.nextFloat(), r.nextFloat(), r.nextFloat()).normalize();
        CSGPlane plane = new CSGPlane(vec.toImmutable(), r.nextFloat());
        
        for(int i = 0; i < 200000000; i++)
        {
            vec.load(r.nextFloat(), r.nextFloat(), r.nextFloat());
            plane.vertexIncrement(vec);
        }
    }

}