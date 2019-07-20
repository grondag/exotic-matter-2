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
package grondag.timeshare;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BooleanSupplier;

import grondag.xm2.XmConfig;

// PERF: throttle performance based on elapsed time instead of number of operations
/**
 * Maintains a queue of tasks that require world access and executes them in
 * FIFO order.
 * 
 * Ensures that all world access is synchronized, in-order, and does not exceed
 * the configured threshold for points per tick.
 * 
 * Tasks are not serialized or persisted - queue must be rebuilt by task
 * providers on world reload.
 *
 */
public class WorldTaskManager {
    /**
     * Metered tasks - run based on budget consumption until drained.
     */
    private static ConcurrentLinkedQueue<BooleanSupplier> tasks = new ConcurrentLinkedQueue<BooleanSupplier>();

    /**
     * Immediate tasks - queue is fully drained each tick.
     */
    private static ConcurrentLinkedQueue<Runnable> immediateTasks = new ConcurrentLinkedQueue<Runnable>();

    public static void clear() {
        tasks.clear();
    }

    public static void doServerTick() {
        while (!immediateTasks.isEmpty()) {
            Runnable r = immediateTasks.poll();
            r.run();
        }

        if (tasks.isEmpty())
            return;

        int operations = XmConfig.EXECUTION.maxQueuedWorldOperationsPerTick;

        BooleanSupplier task = tasks.peek();
        while (operations > 0 && task != null) {
            // check for canceled tasks and move to next if checked
            if (task.getAsBoolean()) {
                operations--;
            } else {
                tasks.poll();
                task = tasks.peek();
            }
        }
    }

    public static void enqueue(BooleanSupplier task) {
        tasks.offer(task);
    }

    /**
     * Use for short-running operations that should run on next tick.
     */
    public static void enqueueImmediate(Runnable task) {
        immediateTasks.offer(task);
    }
}
