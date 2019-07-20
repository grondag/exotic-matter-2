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

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;

/**
 * Exists to allow interfaces that subclass tasks
 */
public interface ITask extends IDomainMember {

    /**
     * Moves status from READY to ACTIVE. Called by job manager when assigning work.
     */
    void claim();

    /**
     * Moves status from ACTIVE back to READY. Called by worker when task must be
     * abandoned.
     */
    void abandon();

    /**
     * Called when an antecedent that previously declared itself ready via
     * {@link #onAntecedentTerminated(ITask)} becomes unready again for any reason.
     * <p>
     * 
     * Will add called as an antecedent for this task and if status of this task is
     * something other than WAITING, will attempt to make status WAITING.
     * <p>
     * 
     * If this task has consequents, and this task was previously COMPLETE, then
     * will cascade the backtrack to the consequent tasks.
     */
    void backTrack(ITask antecedent);

    TaskType requestType();

    public RequestPriority priority();

    RequestStatus getStatus();

    /**
     * Convenient shorthand for getStatus().isTerminated
     */
    boolean isTerminated();

    void cancel();

    /**
     * Should be called on a claimed, active task to move it to completion.
     */
    void complete();

    void addListener(ITaskListener listener);

    void removeListener(ITaskListener listener);

    public int getId();

    public void onAntecedentTerminated(ITask antecedent);

    static ITask taskFromId(int id) {
        return (ITask) Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.TASK);
    }
}
