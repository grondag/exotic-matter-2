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
package grondag.hard_science.network.server_to_client;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.network.AbstractServerToPlayerPacket;
import grondag.hard_science.machines.support.OpenContainerStorageProxy;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent by OpenContainerStorageListner to synch user inventory display for
 * IStorage
 */
public class PacketOpenContainerItemStorageRefresh extends AbstractServerToPlayerPacket<PacketOpenContainerItemStorageRefresh> {

    private List<ItemResourceDelegate> items;
    private long capacity;
    private boolean isFullRefresh;

    public List<ItemResourceDelegate> items() {
        return this.items;
    };

    public PacketOpenContainerItemStorageRefresh() {
    };

    public PacketOpenContainerItemStorageRefresh(List<ItemResourceDelegate> items, long capacity, boolean isFullRefresh) {
        this.items = items;
        this.capacity = capacity;
        this.isFullRefresh = isFullRefresh;
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.capacity = pBuff.readLong();
        this.isFullRefresh = pBuff.readBoolean();
        int count = pBuff.readInt();
        if (count == 0) {
            this.items = ImmutableList.of();
        } else {
            ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
            for (int i = 0; i < count; i++) {
                ItemResourceDelegate item = new ItemResourceDelegate();
                item.fromBytes(pBuff);
                builder.add(item);
            }
            this.items = builder.build();
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeLong(this.capacity);
        pBuff.writeBoolean(this.isFullRefresh);
        int count = items.size();
        pBuff.writeInt(count);
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                this.items.get(i).toBytes(pBuff);
            }
        }
    }

    @Override
    public void handle(PacketOpenContainerItemStorageRefresh message, MessageContext ctx) {
        OpenContainerStorageProxy.ITEM_PROXY.handleStorageRefresh(message.items, message.capacity, message.isFullRefresh);
    }
}