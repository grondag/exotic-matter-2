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

import java.util.HashMap;
import java.util.Iterator;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public class ResourceSlotsMulti<V extends StorageType<V>> extends HashMap<IResource<V>, AbstractResourceWithQuantity<V>> implements IResourceSlots<V> {

    /**
     * 
     */
    private static final long serialVersionUID = -2677602483014977294L;

    @Override
    public void changeQuantity(IResource<V> resource, long delta) {
        if (delta == 0)
            return;
        AbstractResourceWithQuantity<V> rwq = this.get(resource);
        if (rwq == null) {
            assert delta > 0 : "Encountered negative resource quantity";
            this.put(resource, resource.withQuantity(delta));
        } else {
            rwq.changeQuantity(delta);
            if (rwq.isEmpty())
                this.remove(resource);
        }
    }

    @Override
    public long getQuantity(IResource<V> resource) {
        AbstractResourceWithQuantity<V> rwq = this.get(resource);
        return rwq == null ? 0 : rwq.getQuantity();
    }

    @Override
    public Iterator<AbstractResourceWithQuantity<V>> iterator() {
        return this.values().iterator();
    }

}
