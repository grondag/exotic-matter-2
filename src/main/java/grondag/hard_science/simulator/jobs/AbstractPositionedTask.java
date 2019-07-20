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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

/**
 * Task with a position attribute.
 */
public abstract class AbstractPositionedTask extends AbstractTask {
    private static final String NBT_TASK_POSITION = NBTDictionary.claim("taskPos");

    private BlockPos pos;

    protected AbstractPositionedTask(BlockPos pos) {
        super(true);
        this.pos = pos;
    }

    protected AbstractPositionedTask() {
        super(false);
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag) {
        super.deserializeNBT(tag);
        this.pos = BlockPos.fromLong(tag.getLong(NBT_TASK_POSITION));
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        super.serializeNBT(tag);
        tag.putLong(NBT_TASK_POSITION, this.pos.asLong());
    }

    public BlockPos pos() {
        return this.pos;
    }

}
