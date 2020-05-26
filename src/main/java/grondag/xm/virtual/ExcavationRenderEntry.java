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
package grondag.xm.virtual;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apiguardian.api.API;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import grondag.fermion.position.IntegerBox;
import grondag.fermion.sc.unordered.SimpleUnorderedArrayList;
import grondag.xm.Xm;
import grondag.xm.XmConfig;
import grondag.xm.network.S2C_ExcavationRenderUpdate;

/**
 * Class exists on server but render methods do not. Server instantiates (and
 * generates IDs) and transmits to clients.
 */
@API(status = INTERNAL)
public class ExcavationRenderEntry {
	private static int nextID = 0;

	public final int id;

	public final World world;

	public final ExcavationRenderTask task;

	@Nullable
	private IntegerBox aabb;

	private boolean isFirstComputeDone = false;

	/** Non-null if should render individual renderPositions instead of AABB */
	@Nullable
	private BlockPos[] renderPositions = null;

	private final Int2ObjectMap<AtomicInteger> xCounts = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<AtomicInteger> yCounts = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<AtomicInteger> zCounts = new Int2ObjectOpenHashMap<>();

	private final Set<BlockPos> positions = Collections.synchronizedSet(new HashSet<BlockPos>());

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
	private final AtomicBoolean isDirty = new AtomicBoolean(true);

	/**
	 * Players who could be viewing this excavation and should received client-side
	 * updates.
	 */
	private final SimpleUnorderedArrayList<ServerPlayerEntity> listeners = new SimpleUnorderedArrayList<>();

	private void addPos(BlockPos pos) {
		positions.add(pos);

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
		positions.remove(pos);
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
		world = task.world();
		this.task = task;

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id = %d new Entry constructor", id);
		}

		task.addCompletionListener(this::onPositionComplete);

		task.forEachPosition(this::addPos);

		if (ExcavationRenderEntry.this.positions.size() == 0) {
			if (XmConfig.logExcavationRenderTracking) {
				Xm.LOG.info("id = %d new Entry constructor - invalid", id);
			}
			isValid = false;
		} else {
			if (XmConfig.logExcavationRenderTracking) {
				Xm.LOG.info("id = %d new Entry constructor - launching compute", id);
			}
			ExcavationRenderEntry.this.compute();
		}
	}

	public void onPositionComplete(BlockPos pos) {
		final boolean needsCompute = removePos(pos);
		isValid = isValid && positions.size() > 0;
		if (isValid) {
			if (needsCompute) {
				setDirty();
			}
		} else {
			ExcavationRenderTracker.INSTANCE.remove(this);
		}
	}

	private void setDirty() {
		isDirty.compareAndSet(false, true);
		compute();
	}

	/**
	 * If false, can't send packets with this. Implies compute in progress or to be
	 * scheduled.
	 */
	public boolean isFirstComputeDone() {
		return isFirstComputeDone;
	}

	public void compute() {
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id = %d Compute running.", id);
		}

		isDirty.set(false);

		final int count = positions.size();

		if (count == 0) {
			if (XmConfig.logExcavationRenderTracking) {
				Xm.LOG.info("id = %d Compute existing due to empty positions.", id);
			}
			updateListeners();
			ExcavationRenderTracker.INSTANCE.remove(this);
			return;
		}

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		for (final Int2ObjectMap.Entry<AtomicInteger> x : xCounts.int2ObjectEntrySet()) {
			if (x.getValue().get() > 0) {
				minX = Math.min(minX, x.getIntKey());
				maxX = Math.max(maxX, x.getIntKey());
			}
		}

		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (final Int2ObjectMap.Entry<AtomicInteger> y : yCounts.int2ObjectEntrySet()) {
			if (y.getValue().get() > 0) {
				minY = Math.min(minY, y.getIntKey());
				maxY = Math.max(maxY, y.getIntKey());
			}
		}

		int minZ = Integer.MAX_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (final Int2ObjectMap.Entry<AtomicInteger> z : zCounts.int2ObjectEntrySet()) {
			if (z.getValue().get() > 0) {
				minZ = Math.min(minZ, z.getIntKey());
				maxZ = Math.max(maxZ, z.getIntKey());
			}
		}

		final IntegerBox newBox = new IntegerBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

		// always send start time computed
		boolean needsListenerUpdate = !isFirstComputeDone;
		isFirstComputeDone = true;

		if (!newBox.equals(aabb)) {
			aabb = newBox;
			needsListenerUpdate = true;
		}

		if (count <= 16 && (renderPositions == null || renderPositions.length != count)) {
			synchronized (positions) {
				BlockPos[] newPositions = new BlockPos[positions.size()];
				newPositions = positions.toArray(newPositions);
				renderPositions = newPositions;
			}
			needsListenerUpdate = true;
			if (XmConfig.logExcavationRenderTracking) {
				Xm.LOG.info("id %d Computed render position length = %d", id, renderPositions == null ? 0 : renderPositions.length);
			}
		}

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id = %d Compute done, updateListeners=%s, isDirty=%s", id, Boolean.toString(needsListenerUpdate),
					Boolean.toString(isDirty.get()));
		}

		if (needsListenerUpdate) {
			updateListeners();
		}

		if (isDirty.get() && count > 0) {
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

	public IntegerBox aabb() {
		return aabb;
	}

	public void addListener(ServerPlayerEntity listener, boolean sendPacketIfNew) {
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id=%d addListenger sendIfNew=%s, isValue=%s, isFirstComputeDone=%s", id, Boolean.toString(sendPacketIfNew),
					Boolean.toString(isValid), Boolean.toString(isFirstComputeDone));
		}

		synchronized (listeners) {
			if (listeners.addIfNotPresent(listener) && sendPacketIfNew && isValid && isFirstComputeDone) {
				if (XmConfig.logExcavationRenderTracking) {
					Xm.LOG.info("id=%d addListenger scheduling packet.", id);
				}
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(listener, S2C_ExcavationRenderUpdate.toPacket(this));
			}
		}
	}

	public void removeListener(ServerPlayerEntity listener) {
		synchronized (listeners) {
			listeners.removeIfPresent(listener);
		}
	}

	public void updateListeners() {
		if (listeners.isEmpty())
			return;

		//        // think network operations need to run in world tick
		//        WorldTaskManager.enqueueImmediate(new Runnable()
		//        {
		final Packet<?> packet = ExcavationRenderEntry.this.isValid && ExcavationRenderEntry.this.positions.size() > 0
				// update
				? S2C_ExcavationRenderUpdate.toPacket(ExcavationRenderEntry.this)
						// remove
						: S2C_ExcavationRenderUpdate.toPacket(ExcavationRenderEntry.this.id);

				//            @Override
				//            public void run()
				//            {
				synchronized (ExcavationRenderEntry.this.listeners) {
					if (!ExcavationRenderEntry.this.listeners.isEmpty()) {
						for (final ServerPlayerEntity player : listeners) {
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
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id %d Render position retrieval, count = %d", id, renderPositions == null ? 0 : renderPositions.length);
		}
		return renderPositions;
	}
}
