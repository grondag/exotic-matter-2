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
package grondag.hard_science.simulator.resource;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Unique item stack / resource predicate that ignore meta and NBT
 */
public class ItemPredicate implements IResourcePredicate<StorageTypeStack> {
    private final Item item;

    public ItemPredicate(Item item) {
        this.item = item;
    }

    public ItemPredicate(ItemStack stack) {
        this.item = stack.getItem();
    }

    public ItemPredicate(ItemResource resource) {
        this.item = resource.getItem();
    }

    @Override
    public boolean test(@Nullable IResource<StorageTypeStack> t) {
        return ((ItemResource) t).getItem() == this.item;
    }

    @Override
    public boolean isEqualityPredicate() {
        return false;
    }

    @Override
    public Item item() {
        return this.item();
    }

    @Override
    public boolean ignoreMeta() {
        return true;
    }

    @Override
    public boolean ignoreNBT() {
        return true;
    }

    @Override
    public boolean ignoreCaps() {
        return true;
    }

}
