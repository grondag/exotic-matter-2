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
package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.storage.StorageEvent.CapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ResourceUpdate;
import grondag.hard_science.simulator.storage.StorageEvent.StorageNotification;

public class FluidStorageEvent implements IStorageEventFactory<StorageTypeFluid> {
    public static final FluidStorageEvent INSTANCE = new FluidStorageEvent();

    @Override
    public void postBeforeStorageDisconnect(IResourceContainer<StorageTypeFluid> storage) {
        if (storage.getDomain() == null)
            return;
        storage.getDomain().eventBus().post(new BeforeFluidStorageDisconnect(storage));
    }

    @Override
    public void postAfterStorageConnect(IResourceContainer<StorageTypeFluid> storage) {
        if (storage.getDomain() == null)
            return;
        storage.getDomain().eventBus().post(new AfterFluidStorageConnect(storage));
    }

    @Override
    public void postStoredUpdate(IResourceContainer<StorageTypeFluid> storage, IResource<StorageTypeFluid> resource, long delta,
            @Nullable NewProcurementTask<StorageTypeFluid> request) {
        if (storage.getDomain() == null)
            return;

        storage.getDomain().eventBus().post(new FluidStoredUpdate(storage, resource, delta, request));
    }

    @Override
    public void postAvailableUpdate(IResourceContainer<StorageTypeFluid> storage, IResource<StorageTypeFluid> resource, long delta,
            @Nullable NewProcurementTask<StorageTypeFluid> request) {
        if (storage.getDomain() == null)
            return;

        storage.getDomain().eventBus().post(new FluidAvailableUpdate(storage, resource, delta, request));
    }

    @Override
    public void postCapacityChange(IResourceContainer<StorageTypeFluid> storage, long delta) {
        if (storage.getDomain() == null)
            return;
        storage.getDomain().eventBus().post(new FluidCapacityChange(storage, delta));
    }

    public static class BeforeFluidStorageDisconnect extends StorageNotification<StorageTypeFluid> {
        private BeforeFluidStorageDisconnect(IResourceContainer<StorageTypeFluid> storage) {
            super(storage);
        }
    }

    public static class AfterFluidStorageConnect extends StorageNotification<StorageTypeFluid> {
        private AfterFluidStorageConnect(IResourceContainer<StorageTypeFluid> storage) {
            super(storage);
        }
    }

    public static class FluidCapacityChange extends CapacityChange<StorageTypeFluid> {
        private FluidCapacityChange(IResourceContainer<StorageTypeFluid> storage, long delta) {
            super(storage, delta);
        }
    }

    /**
     * Notifies of changes to quantity <em>stored</em>.
     */
    public static class FluidStoredUpdate extends ResourceUpdate<StorageTypeFluid> {
        private FluidStoredUpdate(IResourceContainer<StorageTypeFluid> storage, IResource<StorageTypeFluid> resource, long delta,
                @Nullable NewProcurementTask<StorageTypeFluid> request) {
            super(storage, resource, delta, request);
        }
    }

    /**
     * Notifies of changes to quantity <em>available</em>.
     */
    public static class FluidAvailableUpdate extends ResourceUpdate<StorageTypeFluid> {
        private FluidAvailableUpdate(IResourceContainer<StorageTypeFluid> storage, IResource<StorageTypeFluid> resource, long delta,
                @Nullable NewProcurementTask<StorageTypeFluid> request) {
            super(storage, resource, delta, request);
        }
    }
}
