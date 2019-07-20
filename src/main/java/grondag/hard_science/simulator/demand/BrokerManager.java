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

import java.util.HashMap;

import javax.annotation.Nullable;

import grondag.exotic_matter.block.SuperModelBlock;
import grondag.exotic_matter.placement.SuperItemBlock;
import grondag.exotic_matter.simulator.domain.Domain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;

public class BrokerManager implements IDomainMember {

    private final HashMap<IResource<?>, IBroker<?>> simpleBrokers = new HashMap<IResource<?>, IBroker<?>>();

    private final Domain domain;

    public final AbstractBroker<StorageTypeStack> crushinatorInputBroker;

    public BrokerManager(Domain domain) {
        this.domain = domain;
        this.crushinatorInputBroker = new AbstractBroker<StorageTypeStack>(this) {
        };
    }

    public <T extends StorageType<T>> IBroker<T> brokerForResource(IResource<T> resource) {
        switch (resource.storageType().enumType) {
        case ITEM: {
            ItemResource item = (ItemResource) resource;

            if (item.getItem() instanceof SuperItemBlock) {
                if (((SuperItemBlock) item.getItem()).getBlock().getClass() == SuperModelBlock.class) {
//                            return (IBroker<T>) this.BLOCK_BROKER;
                }
            }
        }

        case POWER:
        case FLUID:
            // use per-resource default
            return this.getOrCreateSimpleBroker(resource);

        case PRIVATE:
            assert false : "Private storage type reference";
            return null;
        default:
            assert false : "Missing enum mapping";
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends StorageType<T>> IBroker<T> getOrCreateSimpleBroker(IResource<T> resource) {
        synchronized (this.simpleBrokers) {
            IBroker<?> result = this.simpleBrokers.get(resource);
            if (result == null) {
                result = new SimpleBroker<T>(this, resource);
                this.simpleBrokers.put(resource, result);
            }
            return (IBroker<T>) result;
        }
    }

    @Override
    public @Nullable Domain getDomain() {
        return this.domain;
    }

    /**
     * Called by brokers when last (non-inventory) producer and last request are
     * removed from them to free up memory.
     */
    public void removeSimpleBroker(SimpleBroker<?> simpleBroker) {
        synchronized (this.simpleBrokers) {
            this.simpleBrokers.remove(simpleBroker.resource());
        }
    }
}
