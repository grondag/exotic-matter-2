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

import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.hard_science.simulator.jobs.AbstractPositionedTask;
import grondag.hard_science.simulator.jobs.TaskType;
import net.minecraft.util.math.BlockPos;

public class ExcavationTask extends AbstractPositionedTask {
    /**
     * Use for new instances.
     */
    public ExcavationTask(BlockPos pos) {
        super(pos);
    }

    /** Use for deserialization */
    public ExcavationTask() {
        super();
    }

    @Override
    protected synchronized void onLoaded() {
        super.onLoaded();

        // prevent orphaned active excavation tasks
        // must be reclaimed at startup
        if (this.getStatus() == RequestStatus.ACTIVE)
            this.abandon();
    }

    @Override
    public TaskType requestType() {
        return TaskType.EXCAVATION;
    }
}
