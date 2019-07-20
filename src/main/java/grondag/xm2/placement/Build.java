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
package grondag.xm2.placement;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.fermion.serialization.IReadWriteNBT;
import grondag.fermion.serialization.NBTDictionary;
import grondag.hard_science.simulator.jobs.Job;
import grondag.hard_science.simulator.jobs.JobManager;
import grondag.hard_science.simulator.jobs.tasks.BlockProcurementTask;
import grondag.hard_science.simulator.jobs.tasks.PlacementTask;
import grondag.timeshare.WorldTaskManager;
import grondag.xm2.Xm;
import grondag.xm2.block.virtual.VirtualBlock;
import grondag.xm2.block.virtual.VirtualItemBlock;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Build implements IReadWriteNBT, IDomainMember, IIdentified {
    private static final String NBT_BUILD_JOB_ID = NBTDictionary.claim("buildJobID");
    private static final String NBT_BUILD_DIMENSION_ID = NBTDictionary.claim("buildDimID");
    private static final String NBT_BUILD_POSITIONS = NBTDictionary.claim("buildPos");

    private final BuildManager buildManager;

    private int id = IIdentified.UNASSIGNED_ID;

    private Identifier dimensionID;

    /**
     * Don't use directly. Use {@link #world()} instead. Is lazily retrieved after
     * deserialization.
     */
    private World world;

    /**
     * Tracks positions of all virtual blocks that belong to this build.
     */
    private LongOpenHashSet positions = new LongOpenHashSet();

    /**
     * If set, means build is under construction. Persisted.
     */
    private int jobID = IIdentified.UNASSIGNED_ID;

    /**
     * Use #job() because is lazy lookup after deserialization.
     */
    private Job job;

    private Build(BuildManager buildManager) {
        super();
        this.buildManager = buildManager;
    };

    public Build(BuildManager buildManager, World inWorld) {
        this(buildManager, Registry.DIMENSION.getId(inWorld.dimension.getType()));
        this.world = inWorld;
    }

    public Build(BuildManager buildManager, Identifier dimensionID) {
        this(buildManager);
        this.dimensionID = dimensionID;
    }

    public Build(BuildManager buildManager, CompoundTag tag) {
        this(buildManager);
        this.deserializeNBT(tag);

        // re-open if prior launch failed to complete
        if (this.jobID == IIdentified.SIGNAL_ID) {
            this.jobID = IIdentified.UNASSIGNED_ID;
            buildManager.setDirty();
        }
    }

    public Identifier dimensionID() {
        return this.dimensionID;
    }

    public World world() {
        if (this.world == null) {
            this.world = Simulator.server().getWorld(Registry.DIMENSION.get(dimensionID));
        }
        return this.world;
    }

    /**
     * Begin tracking virtual block at position as part of this build. Does NOT tag
     * the block with the build ID.
     */
    public void addPosition(BlockPos pos) {
        if (!this.isOpen()) {
            Xm.LOG.warn("Build manager rejected attempt to modify an unopen build. This is a bug.");
            return;
        }

        synchronized (this.positions) {
            positions.add(pos.asLong());
        }
        this.buildManager.setDirty();
    }

    /**
     * Stop tracking virtual block at position as part of this build. Does NOT
     * un-tag or remove the block. Has no effect if build is not open.
     */
    public void removePosition(BlockPos pos) {
        if (!this.isOpen()) {
            Xm.LOG.warn("Build manager rejected attempt to modify an unopen build. This is a bug.");
            return;
        }

        synchronized (this.positions) {
            positions.remove(pos.asLong());
        }
        this.buildManager.setDirty();
    }

    /**
     * Currently assigned job. Will return null if no job or no longer exists but
     * will return terminated jobs.
     */
    @Nullable
    public Job job() {
        if (this.job == null && this.jobID != IIdentified.UNASSIGNED_ID) {
            this.job = Job.jobFromId(this.jobID);
        }
        return this.job;
    }

    /**
     * True if a job has been submitted for this build, the job still exists, and
     * the job has not terminated.
     */
    public boolean isUnderConstruction() {
        Job j = this.job();
        return j != null && !j.getStatus().isTerminated;
    }

    /**
     * Submits spec world task to world task manager which compiles affected
     * positions and then optimizes build order depending on the nature of the
     * build.
     * </p>
     * 
     * After the spec is bound to the world creates a new job and submits it to the
     * job manager.
     * 
     * Job ID is used to determine if we have started but not completed. If world
     * stops before completed then will restart from the beginning on reload.
     * Unassigned job ID means not running. Any other job ID means launch completed
     * and job was submitted.
     */
    public void launch(RequestPriority priority, PlayerEntity player) {
        // abort if already launched or assigned
        if (this.jobID != IIdentified.UNASSIGNED_ID)
            return;

        this.jobID = IIdentified.SIGNAL_ID;
        this.buildManager.setDirty();

        WorldTaskManager.enqueue(new BooleanSupplier() {
            private Job job = new Job(priority, player);

            private boolean isDone = false;

            LongIterator iterator = positions.iterator();

            World world = player.world;

            @Override
            public boolean getAsBoolean() {
                if (isDone)
                    return false;

                if (iterator.hasNext()) {
                    BlockPos pos = BlockPos.fromLong(iterator.nextLong());
                    BlockState blockState = world.getBlockState(pos);
                    if (VirtualBlock.isVirtualBlock(blockState.getBlock())) {
                        ItemStack stack = VirtualItemBlock.getStack(world, blockState, pos);
                        if (stack == null) {
                            Xm.LOG.warn("Build manager unable to retrieve stack from virtual block. This is a bug");
                        } else {
                            BlockProcurementTask procTask = new BlockProcurementTask(pos, stack);
                            job.addTask(procTask);
                            PlacementTask placeTask = new PlacementTask(procTask);
                            job.addTask(placeTask);
                        }
                    } else {
                        iterator.remove();
                    }
                }

                if (!iterator.hasNext()) {
                    complete();
                }

                return !isDone;
            }

            private void complete() {
                buildManager.domain.getCapability(JobManager.class).addJob(job);
                Build.this.jobID = job.getId();
                buildManager.setDirty();
                this.isDone = true;
            }
        });

    }

    @Override
    public @Nullable IDomain getDomain() {
        return this.buildManager.getDomain();
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag) {
        this.deserializeID(tag);
        Simulator.instance().assignedNumbersAuthority().register(this);
        this.jobID = tag.getInt(NBT_BUILD_JOB_ID);
        this.dimensionID = new Identifier(tag.getString(NBT_BUILD_DIMENSION_ID));
        if (tag.containsKey(NBT_BUILD_POSITIONS)) {
            int[] posData = tag.getIntArray(NBT_BUILD_POSITIONS);
            if (posData != null && posData.length > 0 && (posData.length & 1) == 0) {
                int i = 0;
                while (i < posData.length) {
                    this.positions.add((((long) posData[i++]) << 32) | (posData[i++] & 0xffffffffL));
                }
            }
        }
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        this.serializeID(tag);
        tag.putInt(NBT_BUILD_JOB_ID, this.jobID);
        tag.putString(NBT_BUILD_DIMENSION_ID, this.dimensionID.toString());
        synchronized (this.positions) {
            if (!this.positions.isEmpty()) {
                int i = 0;
                int[] posData = new int[this.positions.size() * 2];
                for (long pos : this.positions) {
                    posData[i++] = (int) (pos >> 32);
                    posData[i++] = (int) pos;
                }
                tag.putIntArray(NBT_BUILD_POSITIONS, posData);
            }
        }
    }

    @Override
    public int getIdRaw() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public AssignedNumber idType() {
        return AssignedNumber.BUILD;
    }

    /**
     * True if build is open for edits. Means it has not been submitted for
     * construction or job was canceled and was reopened.
     */
    public boolean isOpen() {
        return this.jobID == IIdentified.UNASSIGNED_ID;
    }

    public boolean isEmpty() {
        return this.positions.isEmpty();
    }

    public static Build buildFromId(int id) {
        return (Build) Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.BUILD);
    }
}
