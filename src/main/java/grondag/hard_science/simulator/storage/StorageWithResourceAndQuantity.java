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

import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Unique data class for returning store-related inquiry results.
 */
public class StorageWithResourceAndQuantity<T extends StorageType<T>> extends StorageWithQuantity<T> {
    public final IResource<T> resource;

    public StorageWithResourceAndQuantity(IResourceContainer<T> storage, IResource<T> resource, long quantity) {
        super(storage, quantity);
        this.resource = resource;
    }
}
