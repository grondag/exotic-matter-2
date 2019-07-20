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
package grondag.hard_science.simulator.demand;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Implemented by machines and processes that fulfill resource requests by
 * producing something and can have WIP. Methods needed for WIP notification.
 *
 */
public interface IProducer<V extends StorageType<V>> {
    /**
     * Called by resource request for WIP when a job is cancelled or something else
     * happens causing request to no longer be valid and work no longer needed.
     */
    public void cancelWIP(NewProcurementTask<V> request);

    /**
     * Called by brokers when they have new demands to "wake up" this producer if it
     * is idle and/or query this broker the next time the producer looks for work.
     */
    public void notifyNewDemand(IBroker<V> broker, NewProcurementTask<V> request);
}
