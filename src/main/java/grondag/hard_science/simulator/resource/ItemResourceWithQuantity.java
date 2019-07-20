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

import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemResourceWithQuantity extends AbstractResourceWithQuantity<StorageType.StorageTypeStack> {

    public ItemResourceWithQuantity(ItemResource resource, long quantity) {
        super(resource, quantity);
    }

    public ItemResourceWithQuantity() {
        super();
    }

    public ItemResourceWithQuantity(NBTTagCompound tag) {
        super(tag);
    }

    public static ItemResourceWithQuantity fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return (ItemResourceWithQuantity) StorageType.ITEM.emptyResource.withQuantity(0);
        return new ItemResourceWithQuantity(ItemResource.fromStack(stack), stack.getCount());
    }

    @Override
    public StorageTypeStack storageType() {
        return StorageType.ITEM;
    }

    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeStack>> SORT_BY_QTY_ASC = new Comparator<AbstractResourceWithQuantity<StorageTypeStack>>() {
        @Override
        public int compare(@Nullable AbstractResourceWithQuantity<StorageTypeStack> o1, @Nullable AbstractResourceWithQuantity<StorageTypeStack> o2) {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                }
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            int result = Long.compare(o1.quantity, o2.quantity);
            return result == 0 ? o1.resource().displayName().compareTo(o2.resource().displayName()) : result;
        }
    };

    public static final Comparator<? super AbstractResourceWithQuantity<StorageTypeStack>> SORT_BY_QTY_DESC = new Comparator<AbstractResourceWithQuantity<StorageTypeStack>>() {
        @Override
        public int compare(@Nullable AbstractResourceWithQuantity<StorageTypeStack> o1, @Nullable AbstractResourceWithQuantity<StorageTypeStack> o2) {
            return SORT_BY_QTY_ASC.compare(o2, o1);
        }

    };

    public ItemStack toStack() {
        return ((ItemResource) this.resource()).stackWithQuantity((int) this.getQuantity());
    }
}
