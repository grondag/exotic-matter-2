package grondag.exotic_matter.varia.intstream;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

import net.minecraft.util.math.MathHelper;

class IntStreamsTest
{
    @Test
    void test()
    {
        testCompact();
        int[] dummy = new int[10000];
        Arrays.stream(dummy).parallel().forEach(i -> testStream());
    }
    
    private void testCompact()
    {
        final int TEST_SIZE = 2731;
        IIntStream s = IntStreams.claim();
        for(int i = 0; i < TEST_SIZE; i++)
        {
            s.set(i, i);
        }
        assert s.capacity() == IntStreams.BLOCK_SIZE * 3;
        s.compact();
        assert s.capacity() == TEST_SIZE;
        s.set(4570, 3);
        assert s.capacity() == IntStreams.BLOCK_SIZE * 5;
        s.set(4570, 0);
        s.compact();
        assert s.capacity() == TEST_SIZE;
        s.clear();
        assert s.capacity() == IntStreams.BLOCK_SIZE * 2;
        s.compact();
        assert s.capacity() == 0;
    }

    private void testStream()
    {
        Random r = ThreadLocalRandom.current();
        int size = r.nextInt(1000000);
        int[] compare = new int[size];
        IIntStream s = r.nextBoolean() ? IntStreams.claim(size) : IntStreams.claim();
        
        for(int i = 0; i < size; i++)
        {
            int address = MathHelper.clamp(i + r.nextInt(64), 0, size - 1);
            int value = r.nextInt();
            compare[address] = value;
            s.set(address, value);
            assert s.get(address) == value;
        }
        
        for(int i = 0; i < size; i++)
        {
            assert s.get(i) == compare[i];
        }
    }
}
