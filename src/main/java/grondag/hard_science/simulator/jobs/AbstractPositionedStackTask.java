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
package grondag.hard_science.simulator.jobs;

import javax.annotation.Nullable;

import grondag.fermion.serialization.NBTDictionary;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

/**
 * Task with position and stack attributes
 */
public abstract class AbstractPositionedStackTask extends AbstractPositionedTask {
    private static final String NBT_TASK_STACK = NBTDictionary.claim("taskStack");

    private ItemStack stack;

    /**
     * Use for new instances.
     */
    public AbstractPositionedStackTask(BlockPos pos, ItemStack stack) {
        super(pos);
        this.stack = stack;
    }

    /** Use for deserialization */
    public AbstractPositionedStackTask() {
        super();
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        this.setDirty();
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag) {
        super.deserializeNBT(tag);
        this.stack = ItemStack.fromTag(tag.getCompound(NBT_TASK_STACK));
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        super.serializeNBT(tag);
        tag.put(NBT_TASK_STACK, stack.toTag(new CompoundTag()));
    }
}
