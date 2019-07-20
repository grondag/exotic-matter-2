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
package grondag.hard_science.simulator.transport.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.StoragePacket;
import grondag.hard_science.simulator.transport.TransportStatus;
import net.minecraft.nbt.NBTTagCompound;

public class IItinerary<T extends StorageType<T>> implements IReadWriteNBT {
    /** will be a direct reference if only one packet. ArrayList otherwise */
    private Object packets = null;

    private List<StoragePacket<T>> readOnlyPackets = Collections.emptyList();

    private int currentIndex = -1;

    /**
     * The packet currently being transported. For non-connected transport, can
     * remain with same value for an extended period while the packet is waiting for
     * pickup or being transported by a drone.
     */
    @SuppressWarnings("unchecked")
    public synchronized StoragePacket<T> currentPacket() {
        if (this.currentIndex == -1)
            return null;

        assert this.packets != null;

        if (this.packets instanceof StoragePacket) {
            assert this.currentIndex == 0;
            return (StoragePacket<T>) this.packets;
        } else {
            return ((ArrayList<StoragePacket<T>>) this.packets).get(this.currentIndex);
        }
    }

    public List<StoragePacket<T>> packets() {
        return this.readOnlyPackets;
    }

    /**
     * Adds a packet at the end of this itinerary.
     */
    @SuppressWarnings("unchecked")
    public synchronized void addPacket(StoragePacket<T> packet) {
        if (this.packets == null) {
            this.packets = packet;
            this.readOnlyPackets = ImmutableList.of(packet);
        } else if (this.packets instanceof StoragePacket) {
            ArrayList<StoragePacket<T>> list = new ArrayList<StoragePacket<T>>();
            list.add((StoragePacket<T>) this.packets);
            list.add(packet);
            this.packets = list;
            this.readOnlyPackets = Collections.unmodifiableList(list);
        } else {
            ((ArrayList<StoragePacket<T>>) this.packets).add(packet);
        }
    }

    public TransportStatus status() {
        return null;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {
        // TODO Auto-generated method stub

    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        // TODO Auto-generated method stub

    }
}
