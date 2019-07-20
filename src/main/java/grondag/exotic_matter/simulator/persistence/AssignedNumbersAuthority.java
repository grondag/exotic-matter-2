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
package grondag.exotic_matter.simulator.persistence;

import java.util.Arrays;

import javax.annotation.Nullable;

import grondag.fermion.serialization.IReadWriteNBT;
import grondag.fermion.serialization.NBTDictionary;
import grondag.xm2.Xm;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;

public class AssignedNumbersAuthority implements IReadWriteNBT, IDirtNotifier {

    private static final String NBT_TAG = NBTDictionary.claim("assignedNumAuth");

    public IdentifiedIndex createIndex(AssignedNumber numberType) {
        return new IdentifiedIndex(numberType);
    }

    @SuppressWarnings("serial")
    public class IdentifiedIndex extends Int2ObjectOpenHashMap<IIdentified> {
        public final AssignedNumber numberType;

        private IdentifiedIndex(AssignedNumber numberType) {
            this.numberType = numberType;
        }

        public synchronized void register(IIdentified thing) {
            IIdentified prior = this.put(thing.getId(), thing);

            if (prior != null && !prior.equals(thing)) {
                Xm.LOG.warn("Assigned number index overwrote registered object due to index collision.  This is a bug.");
            }
        }

        public synchronized void unregister(IIdentified thing) {
            IIdentified prior = this.remove(thing.getId());
            if (prior == null || !prior.equals(thing)) {
                Xm.LOG.warn("Assigned number index unregistered wrong object due to index collision.  This is a bug.");
            }
        }

        @Override
        public synchronized IIdentified get(int index) {
            return super.get(index);
        }
    }

    private int[] lastID = new int[AssignedNumber.values().length];

    private IDirtListener dirtKeeper = NullDirtListener.INSTANCE;

    private final IdentifiedIndex[] indexes;

    public AssignedNumbersAuthority() {
        this.indexes = new IdentifiedIndex[AssignedNumber.values().length];
        for (int i = 0; i < AssignedNumber.values().length; i++) {
            this.indexes[i] = createIndex(AssignedNumber.values()[i]);
        }
        this.clear();
    }

    public void register(IIdentified registrant) {
        this.indexes[registrant.idType().ordinal()].register(registrant);
    }

    public void unregister(IIdentified registrant) {
        this.indexes[registrant.idType().ordinal()].unregister(registrant);
    }

    @Nullable
    public IIdentified get(int id, AssignedNumber idType) {
        return this.indexes[idType.ordinal()].get(id);
    }

    public void clear() {
        lastID = new int[AssignedNumber.values().length];
        Arrays.fill(lastID, 999);
        for (int i = 0; i < AssignedNumber.values().length; i++) {
            this.indexes[i].clear();
        }
    }

    /**
     * First ID returned for each type is 1000 to allow room for system IDs. System
     * ID's should start at 1 to distinguish from missing/unset ID.
     */
    public synchronized int newNumber(AssignedNumber numberType) {
        dirtKeeper.setDirty();
        ;
        return ++this.lastID[numberType.ordinal()];
    }

    @Override
    public synchronized void deserializeNBT(@Nullable CompoundTag tag) {
        int input[] = tag.getIntArray(NBT_TAG);
        if (input.length == 0) {
            this.clear();
        } else {
            if (input.length == lastID.length) {
                lastID = Arrays.copyOf(input, input.length);
            } else {
                Xm.LOG.warn("Simulation assigned numbers save data appears to be corrupt.  World may be borked.");
                this.clear();
                int commonLength = Math.min(lastID.length, input.length);
                System.arraycopy(input, 0, lastID, 0, commonLength);
            }
        }
    }

    @Override
    public synchronized void serializeNBT(CompoundTag tag) {
        tag.putIntArray(NBT_TAG, Arrays.copyOf(lastID, lastID.length));
    }

    @Override
    public void setDirty() {
        this.dirtKeeper.setDirty();
    }

    @Override
    public void setDirtKeeper(IDirtKeeper keeper) {
        this.dirtKeeper = keeper;
    }

    public IdentifiedIndex getIndex(AssignedNumber idType) {
        return this.indexes[idType.ordinal()];
    }

}
