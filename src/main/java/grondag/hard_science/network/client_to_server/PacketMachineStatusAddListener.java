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
package grondag.hard_science.network.client_to_server;

import grondag.exotic_matter.network.AbstractPlayerToServerPacket;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * When sent for a non-tickage tile entity, is signal to send a single immediate
 * update. This is used for non-tickable machines that have infrequent updates
 * to their displays.
 *
 */
public class PacketMachineStatusAddListener extends AbstractPlayerToServerPacket<PacketMachineStatusAddListener> {
    public BlockPos pos;

    public PacketMachineStatusAddListener() {
    }

    public PacketMachineStatusAddListener(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    protected void handle(PacketMachineStatusAddListener message, EntityPlayerMP player) {
        if (player.world.isBlockLoaded(message.pos)) {
            TileEntity te = player.world.getTileEntity(message.pos);
            if (te != null && te instanceof MachineTileEntity) {
                ((MachineTileEntity) te).addPlayerListener(player);
            }
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.pos = pBuff.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeBlockPos(pos);
    }

}
