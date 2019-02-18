package grondag.exotic_matter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import grondag.exotic_matter.world.PackedBlockPos;
import grondag.exotic_matter.world.PackedChunkPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class PackedBlockPosTest
{

    @Test
    public void test()
    {
     BlockPos pos1 = new BlockPos(0, 0, 0);
        
        long long1 = PackedBlockPos.pack(pos1);
        
        long long2 = PackedBlockPos.pack(0, 0, 0);
        
        assert(long1 == long2);
        
        BlockPos pos2 = PackedBlockPos.unpack(long2);
        
        assert(pos1.equals(pos2));
        
        pos1 = new BlockPos(241524, 144, -58234);
        long1 = PackedBlockPos.pack(pos1);
        pos2 = PackedBlockPos.unpack(long1);
        assert(pos1.equals(pos2));
        
        pos1 = new BlockPos(30000000, 255, -30000000);
        long1 = PackedBlockPos.pack(pos1);
        pos2 = PackedBlockPos.unpack(long1);
        assert(pos1.equals(pos2));
        
        // position with extra bits
        pos1 = new BlockPos(-9572, 12, 5954);
        long1 = PackedBlockPos.pack(pos1, 1);
        assert PackedBlockPos.getX(long1) == pos1.getX();
        assert PackedBlockPos.getY(long1) == pos1.getY();
        assert PackedBlockPos.getZ(long1) == pos1.getZ();
        assert PackedBlockPos.getExtra(long1) == 1;
        
        // set extra bits
        pos1 = new BlockPos(241524, 144, -58234);
        long1 = PackedBlockPos.pack(pos1);
        long2 = PackedBlockPos.setExtra(long1, 7);
        pos2 = PackedBlockPos.unpack(long2);
        assert(pos1.equals(pos2));
        assert(PackedBlockPos.getExtra(long2) == 7);
        
        //setup for directions
        pos1 = new BlockPos(241524, 144, -58234);
        long1 = PackedBlockPos.pack(pos1);
        
        //up
        pos2 = PackedBlockPos.unpack(PackedBlockPos.up(long1));
        assert(pos1.up().equals(pos2));
        
        //down
        pos2 = PackedBlockPos.unpack(PackedBlockPos.down(long1));
        assert(pos1.down().equals(pos2));

        //east
        pos2 = PackedBlockPos.unpack(PackedBlockPos.east(long1));
        assert(pos1.east().equals(pos2));
        
        //west
        pos2 = PackedBlockPos.unpack(PackedBlockPos.west(long1));
        assert(pos1.west().equals(pos2));
        
        //north
        pos2 = PackedBlockPos.unpack(PackedBlockPos.north(long1));
        assert(pos1.north().equals(pos2));
        
        //south
        pos2 = PackedBlockPos.unpack(PackedBlockPos.south(long1));
        assert(pos1.south().equals(pos2));
        
        //add
        for(int n = 0; n < 100; n++)
        {
            int x1 = ThreadLocalRandom.current().nextInt(-15000000, 15000000);
            int x2 = ThreadLocalRandom.current().nextInt(-15000000, 1500000);
            
            int z1 = ThreadLocalRandom.current().nextInt(-15000000, 15000000);
            int z2 = ThreadLocalRandom.current().nextInt(-15000000, 15000000);
            
            int y1 = ThreadLocalRandom.current().nextInt(0, 256);
            int y2 = ThreadLocalRandom.current().nextInt(0, 256);

            y2 = Math.min(y2, 255 - y1); // don't test values that would violate world height - should not occur in practice
            long1 = PackedBlockPos.pack(x1, y1, z1);
            long2 = PackedBlockPos.pack(x2, y2, z2);
            pos1 = PackedBlockPos.unpack(PackedBlockPos.add(long1, long2));
            
            assert(pos1.getX() == x1 + x2);
            assert(pos1.getY() == y1 + y2);
            assert(pos1.getZ() == z1 + z2);
            
            
        }
        
        Random r = new Random(1);
        //chunk coords
        for(int i = 0; i < 1000000 ; i++)
        {
            pos1 = new BlockPos(r.nextInt(1000000) - 500000, r.nextInt(256), r.nextInt(1000000) - 500000);
            ChunkPos cpos = new ChunkPos(pos1);
            long1 = PackedChunkPos.getPackedChunkPos(PackedBlockPos.pack(pos1));
            assert(PackedChunkPos.getChunkXPos(long1) == cpos.x);
            assert(PackedChunkPos.getChunkZPos(long1) == cpos.z);
            assert(PackedChunkPos.getChunkXStart(long1) == cpos.getXStart());
            assert(PackedChunkPos.getChunkZStart(long1) == cpos.getZStart());
            ChunkPos cpos2 = PackedChunkPos.unpackChunkPos(long1);
            assert cpos2.x == cpos.x;
            assert cpos2.z == cpos.z;
        }
    }

}