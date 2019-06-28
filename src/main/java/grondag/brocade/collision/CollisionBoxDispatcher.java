package grondag.brocade.collision;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.google.common.collect.ImmutableList;

import grondag.brocade.state.MeshState;
import grondag.fermion.cache.ObjectSimpleCacheLoader;
import grondag.fermion.cache.ObjectSimpleLoadingCache;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class CollisionBoxDispatcher {
    static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<Runnable>();
    private static final ExecutorService EXEC = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, QUEUE,
            new ThreadFactory() {
                private AtomicInteger count = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "Exotic Matter Collision Box Optimizer - " + count.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setPriority(Thread.NORM_PRIORITY - 2);
                    return thread;
                }
            }) {
        @Override
        protected void finalize() {
            super.finalize();
            shutdown();
        }
    };

    private static final ObjectSimpleLoadingCache<MeshState, OptimizingBoxList> modelBounds = new ObjectSimpleLoadingCache<MeshState, OptimizingBoxList>(
            new CollisionBoxLoader(), 0xFFF);

    private static ThreadLocal<FastBoxGenerator> fastBoxGen = new ThreadLocal<FastBoxGenerator>() {
        @Override
        protected FastBoxGenerator initialValue() {
            return new FastBoxGenerator();
        }
    };

    public static ImmutableList<Box> getCollisionBoxes(MeshState modelState) {
        return modelBounds.get(modelState.geometricState()).getList();
    }

    public static VoxelShape getOutlineShape(MeshState modelState) {
        return modelBounds.get(modelState.geometricState()).getShape();
    }
    
    /**
     * Clears the cache.
     */
    public static void clear() {
        modelBounds.clear();
        QUEUE.clear();
    }

    private static class CollisionBoxLoader implements ObjectSimpleCacheLoader<MeshState, OptimizingBoxList> {
//        static AtomicInteger runCounter = new AtomicInteger();
//        static AtomicLong totalNanos = new AtomicLong();

        @Override
        public OptimizingBoxList load(MeshState key) {
//            final long start = System.nanoTime();

            final FastBoxGenerator generator = fastBoxGen.get();
            key.getShape().meshFactory().produceShapeQuads(key, generator);

            // note that build clears for next use
            OptimizingBoxList result = new OptimizingBoxList(generator, key);
            EXEC.execute(result);

//            long total = totalNanos.addAndGet(System.nanoTime() - start);
//            if(runCounter.incrementAndGet() == 100)
//            {
//                Brocade.INSTANCE.info("Avg fast collision box nanos, past 100 samples = %d", total / 100);
//                runCounter.addAndGet(-100);
//                totalNanos.addAndGet(-total);
//            }

            return result;
        }
    }

    public static final Box FULL_BLOCK_BOX = new Box(0, 0, 0, 1, 1, 1);

    /**
     * Creates an AABB with the bounds and rotation provided.
     */
    public static Box makeRotatedAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Matrix4f rotation)
    {
        Vector3f minPos = new Vector3f(minX, minY, minZ);
        Vector3f maxPos = new Vector3f(maxX, maxY, maxZ);
        rotation.transformPosition(minPos);
        rotation.transformPosition(maxPos);
        return new Box(minPos.x, minPos.y, minPos.z, 
                maxPos.x, maxPos.y, maxPos.z);
    }

    public static Box makeRotatedAABB(Box fromAABB, Matrix4f rotation)
    {
        return makeRotatedAABB((float)fromAABB.minX, (float)fromAABB.minY, (float)fromAABB.minZ, (float)fromAABB.maxX, (float)fromAABB.maxY, (float)fromAABB.maxZ, rotation);
    }
}
