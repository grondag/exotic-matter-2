package grondag.exotic_matter.varia.structures;

import org.junit.Test;

import grondag.exotic_matter.varia.structures.LongQueue;

public class LongQueueTest
{
   
    @Test
    public void test()
    {
        LongQueue q = new LongQueue(2);
        
        q.enqueue(0);
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        q.enqueue(4);
        
        assert q.size() == 5;
        
        assert q.dequeueLong() ==0;
        
        long[] contents = q.toArray();
        
        assert contents.length == 4;
        assert contents[0] == 1;
        assert contents[3] == 4;
    }

    
}