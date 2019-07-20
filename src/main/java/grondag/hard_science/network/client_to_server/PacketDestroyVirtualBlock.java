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

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.network.AbstractPlayerToServerPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Necessary because virtual blocks report that they are air, server side and so
 * server will not apply normal destruction logic.
 */
public class PacketDestroyVirtualBlock extends AbstractPlayerToServerPacket<PacketDestroyVirtualBlock> {
    private BlockPos blockPos;

    public PacketDestroyVirtualBlock() {
    }

    public PacketDestroyVirtualBlock(BlockPos pos) {
        this.blockPos = pos;
    }

    @Override
    protected void handle(PacketDestroyVirtualBlock message, EntityPlayerMP player) {
        World world = player.getEntityWorld();
        if (world.isBlockLoaded(message.blockPos) && ISuperBlock.isVirtualBlock(world.getBlockState(message.blockPos).getBlock())) {
            world.setBlockToAir(message.blockPos);
        }
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.blockPos = pBuff.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeBlockPos(this.blockPos);
    }

}
