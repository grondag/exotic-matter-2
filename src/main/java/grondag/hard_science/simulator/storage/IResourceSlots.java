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

import java.util.Iterator;

import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public interface IResourceSlots<V extends StorageType<V>> extends Iterable<AbstractResourceWithQuantity<V>> {
    public boolean isEmpty();

    /** number of unique resources */
    public int size();

    public void clear();

    public void changeQuantity(IResource<V> resource, long delta);

    public long getQuantity(IResource<V> resource);

    @Override
    public Iterator<AbstractResourceWithQuantity<V>> iterator();
}
