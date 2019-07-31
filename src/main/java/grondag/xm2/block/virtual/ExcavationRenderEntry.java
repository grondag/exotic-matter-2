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
package grondag.xm2.block.virtual;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import grondag.exotic_matter.network.S2C_ExcavationRenderUpdate;
import grondag.fermion.sc.unordered.SimpleUnorderedArrayList;
import grondag.fermion.world.IntegerAABB;
//import grondag.hs.simulator.jobs.AbstractPositionedTask;
//import grondag.hs.simulator.jobs.AbstractTask;
//import grondag.hs.simulator.jobs.ITask;
//import grondag.hs.simulator.jobs.ITaskListener;
//import grondag.hs.simulator.jobs.Job;
//import grondag.hs.simulator.jobs.tasks.ExcavationTask;
//import grondag.hs.simulator.jobs.tasks.PlacementTask;
import grondag.xm2.Xm;
import grondag.xm2.XmConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Class exists on server but render methods do not. Server instantiates (and
 * generates IDs) and transmits to clients.
 */
public class ExcavationRenderEntry {
    private static int nextID = 0;

    public final int id;
    
    public final int rawDimensionId;
    
    public final ExcavationRenderTask task;

    @Nullable
    private IntegerAABB aabb;

    private boolean isFirstComputeDone = false;

    /** Non-null if should render individual renderPositions instead of AABB */
    @Nullable
    private BlockPos[] renderPositions = null;

    private Int2ObjectMap<AtomicInteger> xCounts = new Int2ObjectOpenHashMap<AtomicInteger>();
    private Int2ObjectMap<AtomicInteger> yCounts = new Int2ObjectOpenHashMap<AtomicInteger>();
    private Int2ObjectMap<AtomicInteger> zCounts = new Int2ObjectOpenHashMap<AtomicInteger>();

    private Set<BlockPos> positions = Collections.synchronizedSet(new HashSet<BlockPos>());

    private boolean isValid = true;

    /**
     * If true, has changed after the start of the last computation. Cleared at
     * start of computation run.
     * <p>
     * 
     * If dirty when computation completes, computation will resubmit self to queue
     * for recomputation.
     * <p>
     * 
     * If becomes dirty while computation not in progress, {@link #setDirty()} will
     * submit for computation.
     */
    private AtomicBoolean isDirty = new AtomicBoolean(true);

    /**
     * Players who could be viewing this excavation and should received client-side
     * updates.
     */
    private SimpleUnorderedArrayList<ServerPlayerEntity> listeners = new SimpleUnorderedArrayList<ServerPlayerEntity>();

    private void addPos(BlockPos pos) {
        this.positions.add(pos);

        synchronized (xCounts) {
            AtomicInteger xCounter = xCounts.get(pos.getX());
            if (xCounter == null) {
                xCounter = new AtomicInteger(1);
                xCounts.put(pos.getX(), xCounter);
            } else {
                xCounter.incrementAndGet();
            }
        }

        synchronized (yCounts) {
            AtomicInteger yCounter = yCounts.get(pos.getY());
            if (yCounter == null) {
                yCounter = new AtomicInteger(1);
                yCounts.put(pos.getY(), yCounter);
            } else {
                yCounter.incrementAndGet();
            }
        }

        synchronized (zCounts) {
            AtomicInteger zCounter = zCounts.get(pos.getZ());
            if (zCounter == null) {
                zCounter = new AtomicInteger(1);
                zCounts.put(pos.getZ(), zCounter);
            } else {
                zCounter.incrementAndGet();
            }
        }
    }

    /**
     * Returns true if any dimension had a count drop to zero
     */
    private boolean removePos(BlockPos pos) {
        this.positions.remove(pos);
        boolean gotZero = xCounts.get(pos.getX()).decrementAndGet() == 0;
        gotZero = yCounts.get(pos.getY()).decrementAndGet() == 0 || gotZero;
        gotZero = zCounts.get(pos.getZ()).decrementAndGet() == 0 || gotZero;
        return gotZero;
    }

    /**
     * For server side
     */
    public ExcavationRenderEntry(ExcavationRenderTask task) {
        id = nextID++;
        rawDimensionId = task.world().dimension.getType().getRawId();
        this.task = task;
        
        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("id = %d new Entry constructor", this.id);

        task.addCompletionListener(this::onPositionComplete);
        
        task.forEachPosition(this::addPos);

        if (ExcavationRenderEntry.this.positions.size() == 0) {
            if (XmConfig.logExcavationRenderTracking)
                Xm.LOG.info("id = %d new Entry constructor - invalid", this.id);
            this.isValid = false;
        } else {
            if (XmConfig.logExcavationRenderTracking)
                Xm.LOG.info("id = %d new Entry constructor - launching compute", this.id);
            ExcavationRenderEntry.this.compute();
        }
    }

    public void onPositionComplete(BlockPos pos) {
        boolean needsCompute = this.removePos(pos);
        this.isValid = this.isValid && this.positions.size() > 0;
        if (this.isValid) {
            if (needsCompute)
                this.setDirty();
        } else {
            ExcavationRenderTracker.INSTANCE.remove(this);
        }
    }

    private void setDirty() {
        this.isDirty.compareAndSet(false, true);
        this.compute();
    }

    /**
     * If false, can't send packets with this. Implies compute in progress or to be
     * scheduled.
     */
    public boolean isFirstComputeDone() {
        return this.isFirstComputeDone;
    }

    public void compute() {
        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("id = %d Compute running.", this.id);

        this.isDirty.set(false);

        int count = this.positions.size();

        if (count == 0) {
            if (XmConfig.logExcavationRenderTracking)
                Xm.LOG.info("id = %d Compute existing due to empty positions.", this.id);
            this.updateListeners();
            ExcavationRenderTracker.INSTANCE.remove(this);
            return;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (Int2ObjectMap.Entry<AtomicInteger> x : this.xCounts.int2ObjectEntrySet()) {
            if (x.getValue().get() > 0) {
                minX = Math.min(minX, x.getIntKey());
                maxX = Math.max(maxX, x.getIntKey());
            }
        }

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Int2ObjectMap.Entry<AtomicInteger> y : this.yCounts.int2ObjectEntrySet()) {
            if (y.getValue().get() > 0) {
                minY = Math.min(minY, y.getIntKey());
                maxY = Math.max(maxY, y.getIntKey());
            }
        }

        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (Int2ObjectMap.Entry<AtomicInteger> z : this.zCounts.int2ObjectEntrySet()) {
            if (z.getValue().get() > 0) {
                minZ = Math.min(minZ, z.getIntKey());
                maxZ = Math.max(maxZ, z.getIntKey());
            }
        }

        IntegerAABB newBox = new IntegerAABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

        // always send start time computed
        boolean needsListenerUpdate = !this.isFirstComputeDone;
        this.isFirstComputeDone = true;

        if (!newBox.equals(this.aabb)) {
            this.aabb = newBox;
            needsListenerUpdate = true;
        }

        if (count <= 16 && (this.renderPositions == null || this.renderPositions.length != count)) {
            synchronized (this.positions) {
                BlockPos[] newPositions = new BlockPos[this.positions.size()];
                newPositions = this.positions.toArray(newPositions);
                this.renderPositions = newPositions;
            }
            needsListenerUpdate = true;
            if (XmConfig.logExcavationRenderTracking)
                Xm.LOG.info("id %d Computed render position length = %d", this.id, this.renderPositions == null ? 0 : this.renderPositions.length);
        }

        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("id = %d Compute done, updateListeners=%s, isDirty=%s", this.id, Boolean.toString(needsListenerUpdate),
                    Boolean.toString(this.isDirty.get()));

        if (needsListenerUpdate)
            this.updateListeners();

        if (this.isDirty.get() && count > 0) {
            compute();
        }
    }

    /**
     * Checked by excavation tracker on creation and will not add if false.
     * {@link #onTaskComplete(AbstractTask)} also uses as signal to remove this
     * instance from tracker.
     */
    public boolean isValid() {
        return isValid;
    }

    public IntegerAABB aabb() {
        return aabb;
    }

    public void addListener(ServerPlayerEntity listener, boolean sendPacketIfNew) {
        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("id=%d addListenger sendIfNew=%s, isValue=%s, isFirstComputeDone=%s", this.id, Boolean.toString(sendPacketIfNew),
                    Boolean.toString(isValid), Boolean.toString(isFirstComputeDone));

        synchronized (this.listeners) {
            if (this.listeners.addIfNotPresent(listener) && sendPacketIfNew && this.isValid && this.isFirstComputeDone) {
                if (XmConfig.logExcavationRenderTracking)
                    Xm.LOG.info("id=%d addListenger scheduling packet.", this.id);
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(listener, S2C_ExcavationRenderUpdate.toPacket(this));
            }
        }
    }

    public void removeListener(ServerPlayerEntity listener) {
        synchronized (this.listeners) {
            this.listeners.removeIfPresent(listener);
        }
    }

    public void updateListeners() {
        if (this.listeners.isEmpty())
            return;

//        // think network operations need to run in world tick
//        WorldTaskManager.enqueueImmediate(new Runnable() 
//        {
        Packet<?> packet = ExcavationRenderEntry.this.isValid && ExcavationRenderEntry.this.positions.size() > 0
                // update
                ? S2C_ExcavationRenderUpdate.toPacket(ExcavationRenderEntry.this)
                // remove
                : S2C_ExcavationRenderUpdate.toPacket(ExcavationRenderEntry.this.id);

//            @Override
//            public void run()
//            {
        synchronized (ExcavationRenderEntry.this.listeners) {
            if (!ExcavationRenderEntry.this.listeners.isEmpty()) {
                for (ServerPlayerEntity player : listeners) {
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
                }
            }
        }
//            }
//        });
    }

    /**
     * Will be non-null if should render individual renderPositions. Populated when
     * position count is small enough not to be a problem.
     */
    @Nullable
    public BlockPos[] renderPositions() {
        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("id %d Render position retrieval, count = %d", this.id, this.renderPositions == null ? 0 : this.renderPositions.length);
        return this.renderPositions;
    }
}
