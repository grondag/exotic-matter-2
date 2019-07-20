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
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.StorageEvent.CapacityChange;
import grondag.hard_science.simulator.storage.StorageEvent.ResourceUpdate;
import grondag.hard_science.simulator.storage.StorageEvent.StorageNotification;

public class ItemStorageEvent implements IStorageEventFactory<StorageTypeStack> {
    public static final ItemStorageEvent INSTANCE = new ItemStorageEvent();

    @Override
    public void postBeforeStorageDisconnect(IResourceContainer<StorageTypeStack> storage) {
        if (storage.getDomain() == null)
            return;
        storage.getDomain().eventBus().post(new BeforeItemStorageDisconnect(storage));
    }

    @Override
    public void postAfterStorageConnect(IResourceContainer<StorageTypeStack> storage) {
        if (storage.getDomain() == null)
            return;
        storage.getDomain().eventBus().post(new AfterItemStorageConnect(storage));
    }

    @Override
    public void postStoredUpdate(IResourceContainer<StorageTypeStack> storage, IResource<StorageTypeStack> resource, long delta,
            @Nullable NewProcurementTask<StorageTypeStack> request) {
        if (storage.getDomain() == null)
            return;

        storage.getDomain().eventBus().post(new ItemStoredUpdate(storage, resource, delta, request));
    }

    @Override
    public void postAvailableUpdate(IResourceContainer<StorageTypeStack> storage, IResource<StorageTypeStack> resource, long delta,
            @Nullable NewProcurementTask<StorageTypeStack> request) {
        if (storage.getDomain() == null)
            return;

        storage.getDomain().eventBus().post(new ItemAvailableUpdate(storage, resource, delta, request));
    }

    @Override
    public void postCapacityChange(IResourceContainer<StorageTypeStack> storage, long delta) {
        if (storage.getDomain() == null)
            return;
        storage.getDomain().eventBus().post(new ItemCapacityChange(storage, delta));
    }

    public static class BeforeItemStorageDisconnect extends StorageNotification<StorageTypeStack> {
        private BeforeItemStorageDisconnect(IResourceContainer<StorageTypeStack> storage) {
            super(storage);
        }
    }

    public static class AfterItemStorageConnect extends StorageNotification<StorageTypeStack> {
        private AfterItemStorageConnect(IResourceContainer<StorageTypeStack> storage) {
            super(storage);
        }
    }

    public static class ItemCapacityChange extends CapacityChange<StorageTypeStack> {
        private ItemCapacityChange(IResourceContainer<StorageTypeStack> storage, long delta) {
            super(storage, delta);
        }
    }

    /**
     * Notifies of changes to quantity <em>stored</em>.
     */
    public static class ItemStoredUpdate extends ResourceUpdate<StorageTypeStack> {
        private ItemStoredUpdate(IResourceContainer<StorageTypeStack> storage, IResource<StorageTypeStack> resource, long delta,
                @Nullable NewProcurementTask<StorageTypeStack> request) {
            super(storage, resource, delta, request);
        }
    }

    /**
     * Notifies of changes to quantity <em>available</em>.
     */
    public static class ItemAvailableUpdate extends ResourceUpdate<StorageTypeStack> {
        private ItemAvailableUpdate(IResourceContainer<StorageTypeStack> storage, IResource<StorageTypeStack> resource, long delta,
                @Nullable NewProcurementTask<StorageTypeStack> request) {
            super(storage, resource, delta, request);
        }
    }
}
