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
package grondag.hard_science.simulator.jobs;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.fermion.serialization.IReadWriteNBT;
import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.structures.SimpleUnorderedArrayList;
import grondag.fermion.varia.Useful;
import net.minecraft.nbt.CompoundTag;

public abstract class AbstractTask implements IReadWriteNBT, IIdentified, IDomainMember, ITask {
    private static final String NBT_REQUEST_STATUS = NBTDictionary.claim("reqStatus");
    private static final String NBT_REQUEST_DEPENDENCIES = NBTDictionary.claim("reqDep");

    protected Job job = NullJob.INSTANCE;
    private int id = IIdentified.UNASSIGNED_ID;
    protected RequestStatus status = RequestStatus.NEW;

    /**
     * Contains consequent and antecedent data from deserialization. References are
     * obtained lazily, because some will not be available during deserialization
     * itself. Null if no dependencies or after references have been obtained. Data
     * are in pairs, so array size will always be an even number. index 0 =
     * antecedent , index 1 = consequent. If element 0 = this.id then element 1 is a
     * consequent of this request. Otherwise, element 0 will be an antecedent of
     * element 1.
     */
    private int[] dependencyData = null;

    /** DO NOT REFERENCE DIRECTLY! Use {@link #consequents()} */
    private final SimpleUnorderedArrayList<ITask> consequents = new SimpleUnorderedArrayList<>();

    /** DO NOT REFERENCE DIRECTLY! Use {@link #antecedents()} */
    private final SimpleUnorderedArrayList<ITask> antecedents = new SimpleUnorderedArrayList<>();

    /**
     * Objects wanting a callback when task is completed. Null if none. Not
     * persisted.
     */
    private final SimpleUnorderedArrayList<ITaskListener> listeners = new SimpleUnorderedArrayList<ITaskListener>();

    /**
     * @param isNew set false on deserialization.
     */
    protected AbstractTask(boolean isNew) {
        if (isNew)
            this.onLoaded();
    }

    /**
     * Called by job. If job is already part of the domain (has a job manager) when
     * a task is added, the job will call this method immediately.
     * <p>
     * 
     * If the task is added to the job before the job belongs to a domain (which is
     * common while jobs are being constructed) then job will call this when the job
     * is assigned a domain.
     * <p>
     * 
     * Is called both for new tasks and deserialized tasks. If need to detect if
     * this is a new task or a deserialized task, check
     * {@link DomainManager#isDeserializationInProgress()}.
     * 
     * All dependencies should be added before calling. Should not be called
     * otherwise nor called more than once. Does NOT call job to notify of status
     * change. Return true if the task is in a ready state.
     */
    public synchronized boolean initialize(Job job) {
        assert this.job == NullJob.INSTANCE : "AbstractTask.initialize called on same task more than once";

        this.job = job;

        assert this.status == RequestStatus.NEW : "AbstractTask.initialize called on task with non-NEW status";

        this.setDirty();

        if (this.antecedents().isEmpty()) {
            // access directly to avoid callback to job
            this.status = RequestStatus.READY;
            return true;
        } else {
            // access directly to avoid callback to job
            this.status = RequestStatus.WAITING;
            return false;
        }
    }

    @Override
    public void claim() {
        assert this.status == RequestStatus.READY : "AbstractTask.abandon called on task with non-READY status";

        this.setStatus(RequestStatus.ACTIVE);
    }

    @Override
    public void abandon() {
        assert this.status == RequestStatus.ABEND : "AbstractTask.abandon called on task with non-ACTIVE status";
        this.setStatus(RequestStatus.READY);
    }

    @Override
    public abstract TaskType requestType();

    @Override
    public AssignedNumber idType() {
        return AssignedNumber.TASK;
    }

    @Override
    public int getIdRaw() {
        return this.id;
    }

    @Override
    public int getId() {
        // necessary to disambiguate
        return IIdentified.super.getId();
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public Job job() {
        return this.job;
    }

    @Override
    public RequestPriority priority() {
        return this.job.priority;
    }

    @Override
    public RequestStatus getStatus() {
        return this.status;
    }

    protected synchronized void setStatus(RequestStatus newStatus) {
        if (newStatus != this.status) {
            RequestStatus oldStatus = this.status;
            this.status = newStatus;
            this.job.notifyTaskStatusChange(this, oldStatus);
            this.setDirty();
        }
    }

    @Override
    public boolean isTerminated() {
        return this.getStatus().isTerminated;
    }

    @Override
    public void cancel() {
        assert !this.status.isTerminated : "AbstractTask.cancel called on task with terminal status";

        this.setStatus(RequestStatus.CANCELLED);
        this.notifyConsequentsTerminated();
        if (!listeners.isEmpty()) {
            for (ITaskListener l : listeners) {
                l.onTaskCancelled(this);
            }
            listeners.clear();
        }
    }

    @Override
    public void complete() {
        assert this.status == RequestStatus.ACTIVE : "AbstractTask: Status != ACTIVE during complete.";

        this.setStatus(RequestStatus.COMPLETE);
        this.notifyConsequentsTerminated();
        if (!listeners.isEmpty()) {
            if (this.status.isComplete()) {
                for (ITaskListener l : listeners) {
                    l.onTaskComplete(this);
                }
            }
            listeners.clear();
        }
    }

    @Override
    public void addListener(ITaskListener listener) {
        synchronized (this.listeners) {
            if (!this.isTerminated())
                this.listeners.addIfNotPresent(listener);
        }
    }

    @Override
    public void removeListener(ITaskListener listener) {
        synchronized (this.listeners) {
            this.listeners.removeIfPresent(listener);
        }
    }

    /**
     * Called after instance is start created or after deserialized. Deserialization
     * call is made by external helper class so that it happens after any subclass
     * deserialization.
     * <p>
     * 
     * Task may not yet be assigned to a job at this point. If need to reference
     * other tasks or domain instances, override {@link #afterDeserialization()}
     * instead of this method.
     */
    protected synchronized void onLoaded() {
        Simulator.instance().assignedNumbersAuthority().register(this);
    }

    protected synchronized void notifyConsequentsTerminated() {
        if (!this.consequents().isEmpty()) {
            for (ITask r : this.consequents()) {
                r.onAntecedentTerminated(this);
                ;
            }
        }
    }

    /**
     * Called when goes from a terminated status back to a non-terminated status.
     * Propagates backtrack to downstream tasks.
     */
    protected synchronized void backTrackConsequents() {
        if (!this.consequents().isEmpty()) {
            for (ITask r : this.consequents()) {
                r.backTrack(this);
            }
        }
    }

    public static void link(AbstractTask antecedent, AbstractTask consequent) {
        antecedent.addConsequent(consequent);
        consequent.addAntecedent(antecedent);
    }

    private synchronized void addConsequent(ITask consequent) {
        this.consequents().addIfNotPresent(consequent);
        this.setDirty();
    }

    private synchronized void addAntecedent(ITask antecedent) {
        this.antecedents().addIfNotPresent(antecedent);
        this.setDirty();
    }

    @Override
    public synchronized void onAntecedentTerminated(ITask antecedent) {
        assert this.status == RequestStatus.NEW || this.status == RequestStatus.WAITING : "AbstractTask.onAntecedentTerminated called with invalid status";

        this.antecedents().removeIfPresent(antecedent);
        this.setDirty();
        if (antecedent.getStatus().didEndWithoutCompleting()) {
            this.cancel();
        } else if (this.antecedents().isEmpty()) {
            this.setStatus(RequestStatus.READY);
        }
    }

    @Override
    public synchronized void backTrack(ITask antecedent) {
        assert !antecedent.getStatus().isTerminated : "AbstractTask.backTrack called with terminal status";

        this.addAntecedent(antecedent);
        switch (this.status) {
        case READY:
            this.setStatus(RequestStatus.WAITING);
            break;

        case ACTIVE:
            this.cancel();
            this.setStatus(RequestStatus.WAITING);
            break;

        case ABEND:
        case CANCELLED:
        case COMPLETE:
            this.setStatus(RequestStatus.WAITING);
            this.backTrackConsequents();

        case NEW:
        case WAITING:
        default:
            // no impact because we haven't started
            break;
        }
    }

    /**
     * All tasks that depend on this task. Unlike {@link #antecedents()} this list
     * is never modified if this task or any of the consequent tasks have status
     * changes of any kind.
     */
    protected SimpleUnorderedArrayList<ITask> consequents() {
        this.initializeDependencies();
        return this.consequents;
    }

    /**
     * Contains ONLY unready antecedents for this task. When they become ready they
     * are remove. If a new contraint is introduced or if a prior dependency becomes
     * unready, then anteceded must be re-added via
     * {@link #addAntecedent(AbstractTask)}.
     */
    protected SimpleUnorderedArrayList<ITask> antecedents() {
        this.initializeDependencies();
        return this.antecedents;
    }

    protected void initializeDependencies() {
        if (this.dependencyData != null) {
            synchronized (this) {
                if (this.dependencyData != null) {
                    int i = 0;
                    while (i < this.dependencyData.length) {
                        int ant = this.dependencyData[i++];
                        int con = this.dependencyData[i++];

                        if (ant == this.id) {
                            ITask conReq = ITask.taskFromId(con);
                            if (conReq != null)
                                this.consequents.add(conReq);
                        } else if (con == this.id) {
                            ITask antReq = ITask.taskFromId(ant);
                            if (antReq != null)
                                this.antecedents.add(antReq);
                        }

                    }
                    this.dependencyData = null;
                }
            }
        }
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag) {
        this.deserializeID(tag);
        this.status = Useful.safeEnumFromTag(tag, NBT_REQUEST_STATUS, RequestStatus.NEW);
        this.dependencyData = tag.getIntArray(NBT_REQUEST_DEPENDENCIES);

        // discard depData if empty or not an even number (latter implies corrupt data)
        if (this.dependencyData.length == 0 || (this.dependencyData.length & 0x1) == 0x1)
            this.dependencyData = null;
    }

    @Override
    public synchronized void serializeNBT(CompoundTag tag) {
        this.serializeID(tag);
        Useful.saveEnumToTag(tag, NBT_REQUEST_STATUS, this.status);

        SimpleUnorderedArrayList<ITask> antecedents = this.antecedents();
        SimpleUnorderedArrayList<ITask> consequents = this.consequents();

        int dependencyCount = antecedents.size() + consequents.size();
        if (dependencyCount > 0) {
            int[] depData = new int[dependencyCount * 2];
            int i = 0;

            if (!consequents.isEmpty()) {
                for (ITask r : consequents) {
                    depData[i++] = this.getId();
                    depData[i++] = r.getId();
                }
            }

            if (!antecedents.isEmpty()) {
                for (ITask r : antecedents) {
                    depData[i++] = r.getId();
                    depData[i++] = this.getId();
                }
            }

            tag.putIntArray(NBT_REQUEST_DEPENDENCIES, depData);
        }
    }

    @Override
    public @Nullable IDomain getDomain() {
        return this.job.getDomain();
    }

    /**
     * Called on all tasks after deserialization is complete. Override to handle
     * actions that may require other objects to be deserialized start.
     */
    public void afterDeserialization() {
    };

    /**
     * If this task (or its holder) is serialized, causes serialization to occur at
     * next opportunity.
     */
    public void setDirty() {
        this.job.setDirty();
    }

}
