package grondag.exotic_matter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import gnu.trove.list.TLongList;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.world.PackedBlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class UsefulTest
{

    @Test
    public void test()
    {
        TLongList result = Useful.line2dInPlaneXZ(PackedBlockPos.pack(12, 0, -13), PackedBlockPos.pack(-16, 0, 4));
        for(long packedPos : result.toArray())
        {
            System.out.println(PackedBlockPos.getX(packedPos) + "        " + PackedBlockPos.getZ(packedPos));
        }
        
        Vec3d origin;
        Vec3d direction;
        AxisAlignedBB box;
        
        // should not find if behind direction
        origin = new Vec3d(0.5, 0.5, 0.5);
        direction = new Vec3d(2, 2, 2);
        box = new AxisAlignedBB(-1, -1, -1, 0, 0, 0);
        assert(!Useful.doesRayIntersectAABB(origin, direction, box));
        
        //should find if contained
        origin = new Vec3d(0.5, 0.5, 0.5);
        direction = new Vec3d(2, 2, 2);
        box = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        assert(Useful.doesRayIntersectAABB(origin, direction, box));
        
        //should find directly in front
        origin = new Vec3d(0.5, 0.5, 0.5);
        direction = new Vec3d(2, 2, 2);
        box = new AxisAlignedBB(1, 1, 1, 2, 2, 2);
        assert(Useful.doesRayIntersectAABB(origin, direction, box));
        
        //and should find if much father in front
        origin = new Vec3d(0.5, 0.5, 0.5);
        direction = new Vec3d(2, 2, 2);
        box = new AxisAlignedBB(40, 40, 40, 41, 41, 41);
        assert(Useful.doesRayIntersectAABB(origin, direction, box));
        
        //and should not find if not on line
        origin = new Vec3d(0.5, 0.5, 0.5);
        direction = new Vec3d(2, 2, 2);
        box = new AxisAlignedBB(3, 4, 5, 4, 5, 6);
        assert(!Useful.doesRayIntersectAABB(origin, direction, box));
        
        //real example
        origin = new Vec3d(609.5, 5.5, 770.5);
        direction = new Vec3d(-3.0, 0.0, 3.0);
        box = new AxisAlignedBB(608.9, 5.0, 770.9, 610.1, 6.0, 772.1);
        assert(Useful.doesRayIntersectAABB(origin, direction, box));
        
        
        Random r = ThreadLocalRandom.current();
        for(int n = 0; n < 100; n++)
        {
            long l = r.nextLong();
            final int low = Useful.longToIntLow(l);
            final int high = Useful.longToIntHigh(l);
            assert Useful.longFromInts(low, high) == l;
        }
    }

}