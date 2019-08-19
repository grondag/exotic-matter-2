/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.collision;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apiguardian.api.API;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.google.common.collect.ImmutableList;

import grondag.fermion.sc.cache.ObjectSimpleLoadingCache;
import grondag.xm.api.modelstate.ModelState;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

@API(status = INTERNAL)
public class CollisionDispatcherImpl {
    static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<Runnable>();
    private static final ExecutorService EXEC = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, QUEUE, new ThreadFactory() {
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

    private static final ObjectSimpleLoadingCache<ModelState, OptimizingBoxList> modelBounds = new ObjectSimpleLoadingCache<ModelState, OptimizingBoxList>(
            CollisionDispatcherImpl::load, k -> k.toImmutable(), 0xFFF);

    private static ThreadLocal<FastBoxGenerator> fastBoxGen = new ThreadLocal<FastBoxGenerator>() {
        @Override
        protected FastBoxGenerator initialValue() {
            return new FastBoxGenerator();
        }
    };

    public static ImmutableList<Box> boxesFor(ModelState modelState) {
        return modelBounds.get(modelState.geometricState()).getList();
    }

    public static VoxelShape shapeFor(ModelState modelState) {
        return modelBounds.get(modelState.geometricState()).getShape();
    }

    /**
     * Clears the cache.
     */
    public static void clear() {
        modelBounds.clear();
        QUEUE.clear();
    }

    private static OptimizingBoxList load(ModelState key) {
//            final long start = System.nanoTime();

        final FastBoxGenerator generator = fastBoxGen.get();
        key.produceQuads(generator);

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

    static final Box FULL_BLOCK_BOX = new Box(0, 0, 0, 1, 1, 1);

    /**
     * Creates an AABB with the bounds and rotation provided.
     */
    static Box makeRotatedAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Matrix4f rotation) {
        Vector3f minPos = new Vector3f(minX, minY, minZ);
        Vector3f maxPos = new Vector3f(maxX, maxY, maxZ);
        rotation.transformPosition(minPos);
        rotation.transformPosition(maxPos);
        return new Box(minPos.x, minPos.y, minPos.z, maxPos.x, maxPos.y, maxPos.z);
    }

    static Box makeRotatedAABB(Box fromAABB, Matrix4f rotation) {
        return makeRotatedAABB((float) fromAABB.minX, (float) fromAABB.minY, (float) fromAABB.minZ, (float) fromAABB.maxX, (float) fromAABB.maxY,
                (float) fromAABB.maxZ, rotation);
    }
}
