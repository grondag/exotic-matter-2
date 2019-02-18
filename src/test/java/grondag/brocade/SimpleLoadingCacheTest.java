package grondag.exotic_matter;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.exotic_matter.cache.LongAtomicLoadingCache;
import grondag.exotic_matter.cache.LongSimpleCacheLoader;
import grondag.exotic_matter.cache.LongSimpleLoadingCache;
import grondag.exotic_matter.cache.ObjectSimpleCacheLoader;
import grondag.exotic_matter.cache.ObjectSimpleLoadingCache;
import grondag.exotic_matter.cache.WideSimpleCacheLoader;
import grondag.exotic_matter.cache.WideSimpleLoadingCache;
import io.netty.util.internal.ThreadLocalRandom;

public class SimpleLoadingCacheTest
{
    /** added to key to produce result */
    private static final long MAGIC_NUMBER = 42L;
    private static final int STEP_COUNT = 10000000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int LOAD_COST = 10;
    private static final AtomicLong twiddler = new AtomicLong(0);
    
    private static class Loader extends CacheLoader<Long, Long> implements LongSimpleCacheLoader<Long>, ObjectSimpleCacheLoader<Long, Long>, WideSimpleCacheLoader<Long> 
    {
        @Override
        public Loader createNew()
        {
            return new Loader();
        }

        @Override
        public Long load(long key)
        {
            if(LOAD_COST > 0)
            {
              for(int i = 0; i < LOAD_COST; i++)
              {
                  twiddler.incrementAndGet();
              }
            }
            return new Long(key + MAGIC_NUMBER);
        }

        @Override
        public @Nonnull Long load(@Nullable Long key)
        {
            if(LOAD_COST > 0)
            {
              for(int i = 0; i < LOAD_COST; i++)
              {
                  twiddler.incrementAndGet();
              }
            }
            return load(key == null ? 0 : key.longValue());
        }

        @Override
        public Long load(long key1, long key2)
        {
            if(LOAD_COST > 0)
            {
              for(int i = 0; i < LOAD_COST; i++)
              {
                  twiddler.incrementAndGet();
              }
            }
            return load(key1);
        }
    }
    
    private static interface CacheAdapter
    {
        public abstract long get(long key);
        
        public abstract CacheAdapter newInstance(int maxSize);
    }
    
    private abstract class Runner implements Callable<Void>
    {
        
        private final CacheAdapter subject;
        
        private Runner(CacheAdapter subject)
        {
            this.subject = subject;
        }
        
        @Override
        public @Nullable Void call()
        {
            try
            {
                Random random = ThreadLocalRandom.current();
                
                for(int i = 0; i < STEP_COUNT; i++)
                {
                    long key = getKey(i, random.nextLong());
                    Long result = subject.get(key);
                    assert(result.longValue() == key + MAGIC_NUMBER);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
        
        public abstract long getKey(int step, long randomLong);
    }
    
    private class UniformRunner extends Runner
    {
        private final long keyMask;
        
        private UniformRunner(CacheAdapter subject, long keyMask)
        {
            super(subject);
            this.keyMask = keyMask;
        }

        @Override
        public long getKey(int step, long randomLong)
        {
            return randomLong & keyMask;
        }
    }

    /** shifts from one set of uniform demand to another and then back again */
    private class ShiftRunner extends Runner
    {
        private final static int FIRST_MILESTONE = STEP_COUNT / 3;
        private final static int SECOND_MILESTONE = FIRST_MILESTONE * 2;

        private final long keyMask;
        
        private ShiftRunner(CacheAdapter subject, long keyMask)
        {
            super(subject);
            this.keyMask = keyMask;
        }

        @Override
        public long getKey(int step, long randomLong)
        {
            //return odd values in 1st and 3rd phase, even in middle phase
            if(step < FIRST_MILESTONE || step > SECOND_MILESTONE)
            {
                if((randomLong & 1L) == 0) randomLong++;
            }
            else
            {
                if((randomLong & 1L) == 1) randomLong++;
            }
            return randomLong & keyMask;
        }
    }

    private class GoogleAdapter implements CacheAdapter
    {    
        @SuppressWarnings("null")
        private LoadingCache<Long, Long> cache;
     
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.getUnchecked(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            GoogleAdapter result = new GoogleAdapter();
  
            result.cache = CacheBuilder.newBuilder().concurrencyLevel(THREAD_COUNT).initialCapacity(maxSize).maximumSize(maxSize).build(new Loader());
            
            return result;
        }
    }

    private class LongAtomicAdapter implements CacheAdapter
    {    
        @SuppressWarnings("null")
        private LongAtomicLoadingCache<Long> cache;
        
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.get(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            LongAtomicAdapter result = new LongAtomicAdapter();
            result.cache = new LongAtomicLoadingCache<Long>(new Loader(), maxSize);
            return result;
        }
    }
    
    private class LongSimpleAdapter implements CacheAdapter
    {    
        @SuppressWarnings("null")
        private LongSimpleLoadingCache<Long> cache;
        
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.get(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            LongSimpleAdapter result = new LongSimpleAdapter();
            result.cache = new LongSimpleLoadingCache<Long>(new Loader(), maxSize);
            return result;
        }
    }
    
    private class ObjectSimpleAdapter implements CacheAdapter
    {    
        @SuppressWarnings("null")
        private ObjectSimpleLoadingCache<Long, Long> cache;
        
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.get(key);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            ObjectSimpleAdapter result = new ObjectSimpleAdapter();
            result.cache = new ObjectSimpleLoadingCache<Long, Long>(new Loader(), maxSize);
            return result;
        }
    }
    
    private class WideSimpleAdapter implements CacheAdapter
    {    
        @SuppressWarnings("null")
        private WideSimpleLoadingCache<Long> cache;
        
        @Override
        public long get(long key)
        {
            long startTime = System.nanoTime();
            long result = cache.get(key, key * 31);
            nanoCount.addAndGet(System.nanoTime() - startTime);
            return result;
        }

        @Override
        public CacheAdapter newInstance(int maxSize)
        {
            WideSimpleAdapter result = new WideSimpleAdapter();
            result.cache = new WideSimpleLoadingCache<Long>(new Loader(), maxSize);
            return result;
        }
    }
    
    AtomicLong nanoCount = new AtomicLong(0);
    
    private void doTestInner(ExecutorService executor, CacheAdapter subject)
    {
        ArrayList<Runner> runs = new ArrayList<Runner>();
        
        System.out.println("Practical best case: key space == max capacity - uniform random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0xFFFFF);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new UniformRunner(subject, 0xFFFFF));
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Suboptimal case: moderately constrained memory test - uniform random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0xCCCCC);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new UniformRunner(subject, 0xFFFFF)); 
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        System.out.println("Worst case: Severely constrained memory test - uniform random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0x2FFFF);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new UniformRunner(subject, 0xFFFFF));
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Nominal case: moderately constrained memory test - shifting random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0x7FFFF);
        for(int i = 0; i < THREAD_COUNT; i++ )
        {
            runs.add(new ShiftRunner(subject, 0xFFFFF));
        }
        try
        {
            executor.invokeAll(runs);
            System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        System.out.println("Nominal case / single thread: moderately constrained memory test - shifting random demand");
        runs.clear();
        nanoCount.set(0);
        subject = subject.newInstance(0x7FFFF);
        for(int i = 0; i < THREAD_COUNT; i++)
        {
            new ShiftRunner(subject, 0xFFFFF).call();
        }
        System.out.println("Mean get() time = " + (nanoCount.get() / (STEP_COUNT * THREAD_COUNT)));

        System.out.println("");
    }
        
    
    public void doTestOuter(ExecutorService executor)
    {
        
        System.out.println("Running simple long cache test");
        doTestInner(executor, new LongSimpleAdapter());
      
        System.out.println("Running atomic long cache test");
        doTestInner(executor, new LongAtomicAdapter());
        
        System.out.println("Running wide key cache test");
        doTestInner(executor, new WideSimpleAdapter());
        
        System.out.println("Running simple object cache test");
        doTestInner(executor, new ObjectSimpleAdapter());

        System.out.println("Running google cache test");
        doTestInner(executor, new GoogleAdapter());
        
    }
    
    @Test
    public void test()
    {
        
        // not really a unit test, so disable unless actually want to run
        
//        ExecutorService SIMULATION_POOL;
//        SIMULATION_POOL = Executors.newFixedThreadPool(THREAD_COUNT);
//        
//        System.out.println("WARM UP RUN");
//        doTestOuter(SIMULATION_POOL);
//        
//        System.out.println("TEST RUN");
//        doTestOuter(SIMULATION_POOL);
    }
}