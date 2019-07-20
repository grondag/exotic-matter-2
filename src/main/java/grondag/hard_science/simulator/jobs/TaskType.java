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

import java.util.function.Supplier;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.Useful;
import grondag.hard_science.simulator.jobs.tasks.BlockFabricationTask;
import grondag.hard_science.simulator.jobs.tasks.BlockProcurementTask;
import grondag.hard_science.simulator.jobs.tasks.ExcavationTask;
import grondag.hard_science.simulator.jobs.tasks.PerpetualTask;
import grondag.hard_science.simulator.jobs.tasks.PlacementTask;
import net.minecraft.nbt.CompoundTag;

public enum TaskType {
    NO_OPERATION(new Supplier<AbstractTask>() {
        @Override
        public AbstractTask get() {
            return null;
        }
    }), EXCAVATION(new Supplier<AbstractTask>() {
        @Override
        public AbstractTask get() {
            return new ExcavationTask();
        }
    }), BLOCK_FABRICATION(new Supplier<AbstractTask>() {
        @Override
        public AbstractTask get() {
            return new BlockFabricationTask();
        }
    }), PLACEMENT(new Supplier<AbstractTask>() {
        @Override
        public AbstractTask get() {
            return new PlacementTask();
        }
    }), BLOCK_PROCUREMENT(new Supplier<AbstractTask>() {
        @Override
        public AbstractTask get() {
            return new BlockProcurementTask();
        }
    }),
//    SIMPLE_PROCUREMENT(new Supplier<AbstractTask>()
//    { 
//        @SuppressWarnings("rawtypes")
//        public AbstractTask get() {return new SimpleProcurementTask(); }
//    }),
//    DELIVERY(new Supplier<AbstractTask>()
//    { 
//        @SuppressWarnings("rawtypes")
//        public AbstractTask get() {return new DeliveryTask(); }
//    }), 
    PERPETUAL(new Supplier<AbstractTask>() {
        @Override
        public AbstractTask get() {
            return new PerpetualTask();
        }
    }),;

    private final Supplier<AbstractTask> supplier;

    private TaskType(Supplier<AbstractTask> supplier) {
        this.supplier = supplier;
    }

    private static final String NBT_REQUEST_TYPE = NBTDictionary.claim("reqType");

    public static CompoundTag serializeTask(AbstractTask task) {
        CompoundTag result = task.serializeNBT();
        Useful.saveEnumToTag(result, NBT_REQUEST_TYPE, task.requestType());
        return result;
    }

    public static AbstractTask deserializeTask(CompoundTag tag, Job job) {
        AbstractTask result = Useful.safeEnumFromTag(tag, NBT_REQUEST_TYPE, TaskType.NO_OPERATION).supplier.get();
        if (result != null) {
            result.job = job;
            result.deserializeNBT(tag);
            result.onLoaded();
        }
        return result;
    }
}
