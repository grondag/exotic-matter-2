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
package grondag.hard_science.simulator.jobs.tasks;

import javax.annotation.Nonnull;

import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.hard_science.simulator.jobs.AbstractTask;
import grondag.hard_science.simulator.jobs.ITask;
import grondag.hard_science.simulator.jobs.ITaskListener;
import grondag.hard_science.simulator.jobs.Job;
import grondag.hard_science.simulator.jobs.TaskType;

/**
 * Task that is never ready and never completes. Used to keep system jobs alive
 * forever without introducing special case logic.
 */
public class PerpetualTask extends AbstractTask {
    public PerpetualTask(boolean isNew) {
        super(isNew);
    }

    /** Use for deserialization */
    public PerpetualTask() {
        this(false);
    }

    @Override
    public TaskType requestType() {
        return TaskType.PERPETUAL;
    }

    @Override
    public synchronized boolean initialize(@Nonnull Job job) {
        this.job = job;
        this.status = RequestStatus.WAITING;
        this.job.setDirty();
        return false;
    }

    @Override
    public void claim() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.WAITING;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void complete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(ITaskListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected synchronized void backTrackConsequents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void onAntecedentTerminated(ITask antecedent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void backTrack(ITask antecedent) {
        throw new UnsupportedOperationException();
    }
}
