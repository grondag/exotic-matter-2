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

import grondag.exotic_matter.simulator.job.RequestPriority;
import grondag.exotic_matter.simulator.job.RequestStatus;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Job that eats all method calls.
 */
public class NullJob extends Job {
    public static final NullJob INSTANCE = new NullJob();

    private NullJob() {
    }

    @Override
    public RequestPriority getPriority() {
        return RequestPriority.MINIMAL;
    }

    @Override
    public void setPriority(RequestPriority priority) {
        // NOOP
    }

    @Override
    public String userName() {
        return "NONE";
    }

    @Override
    protected void setStatus(RequestStatus newStatus) {
        // NOOP
    }

    @Override
    public RequestStatus getStatus() {
        return RequestStatus.COMPLETE;
    }

    @Override
    public void cancel() {
        // NOOP
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {
        // NOOP
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        // NOOP
    }

    @Override
    public void notifyTaskStatusChange(AbstractTask abstractTask, RequestStatus priorStatus) {
        // NOOP
    }

    @Override
    public int getIdRaw() {
        return 0;
    }

    @Override
    public void setId(int id) {
        // NOOP
    }

    @Override
    public RequestPriority effectivePriority() {
        return RequestPriority.MINIMAL;
    }

    @Override
    public void updateEffectivePriority() {
        // NOOP
    }

    @Override
    public RequestStatus effectiveStatus() {
        return RequestStatus.COMPLETE;
    }

    @Override
    public void updateEffectiveStatus() {
        // NOOP
    }

    @Override
    public boolean hasReadyWork() {
        return false;
    }

}
