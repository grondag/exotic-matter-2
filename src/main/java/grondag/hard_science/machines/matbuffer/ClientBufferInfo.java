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
package grondag.hard_science.machines.matbuffer;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of material buffer descriptive information for display on client.
 */
public class ClientBufferInfo {
    public final ClientContainerInfo<StorageTypeStack> itemInput = new ClientContainerInfo<>(StorageType.ITEM);

    public final ClientContainerInfo<StorageTypeStack> itemOutput = new ClientContainerInfo<>(StorageType.ITEM);

    public final ClientContainerInfo<StorageTypeFluid> fluidInput = new ClientContainerInfo<>(StorageType.FLUID);

    public final ClientContainerInfo<StorageTypeFluid> fluidOutput = new ClientContainerInfo<>(StorageType.FLUID);

    public static void toBytes(BufferManager bufferManager, PacketBuffer pBuff) {
        ClientContainerInfo.toBytes(bufferManager.itemInput(), pBuff);
        ClientContainerInfo.toBytes(bufferManager.itemOutput(), pBuff);
        ClientContainerInfo.toBytes(bufferManager.fluidInput(), pBuff);
        ClientContainerInfo.toBytes(bufferManager.fluidOutput(), pBuff);
    }

    public void fromBytes(PacketBuffer pBuff) {
        this.itemInput.fromBytes(pBuff);
        this.itemOutput.fromBytes(pBuff);
        this.fluidInput.fromBytes(pBuff);
        this.fluidOutput.fromBytes(pBuff);
    }

    public boolean hasFailureCause() {
        return !(this.itemInput.blames.isEmpty() && this.fluidInput.blames.isEmpty());
    }

}
