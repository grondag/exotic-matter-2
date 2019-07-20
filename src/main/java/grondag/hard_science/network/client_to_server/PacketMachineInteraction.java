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

import javax.annotation.Nonnull;

import grondag.exotic_matter.network.AbstractPlayerToServerPacket;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PacketMachineInteraction extends AbstractPlayerToServerPacket<PacketMachineInteraction> {
    public static enum Action {
        TOGGLE_POWER, TOGGLE_REDSTONE_CONTROL
    }

    private BlockPos blockPos;
    private Action action;

    public PacketMachineInteraction() {
    }

    public PacketMachineInteraction(Action action, BlockPos pos) {
        this.action = action;
        this.blockPos = pos;
    }

    @Override
    protected void handle(PacketMachineInteraction message, EntityPlayerMP player) {
        if (player == null)
            return;

        World world = player.getEntityWorld();

        if (!world.isBlockLoaded(message.blockPos))
            return;

        TileEntity te = player.world.getTileEntity(message.blockPos);
        if (te != null && te instanceof MachineTileEntity) {
            MachineTileEntity mte = (MachineTileEntity) te;

            switch (message.action) {

            case TOGGLE_POWER:
                mte.togglePower(player);
                return;

            case TOGGLE_REDSTONE_CONTROL:
                mte.toggleRedstoneControl(player);
                return;

            default:
                return;

            }
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.action = pBuff.readEnumValue(Action.class);
        this.blockPos = pBuff.readBlockPos();

    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeEnumValue(this.action);
        pBuff.writeBlockPos(this.blockPos);
    }

    public Action getAction() {
        return action;
    }

    public BlockPos getPos() {
        return this.blockPos;
    }
}