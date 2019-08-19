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
package grondag.xm.network;

import java.util.Collection;

import grondag.fermion.position.IntegerBox;
import grondag.xm.Xm;
import grondag.xm.XmConfig;
import grondag.xm.virtual.ExcavationRenderEntry;
import grondag.xm.virtual.ExcavationRenderManager;
import grondag.xm.virtual.ExcavationRenderer;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

/**
 * Replaces all excavation render entries. Sent when players logs in, changes
 * dimension or changes active domain.
 */
public abstract class S2C_PacketExcavationRenderRefresh {
    private S2C_PacketExcavationRenderRefresh() {
    }

    public static final Identifier ID = new Identifier(Xm.MODID, "exrr");

    public static Packet<?> toPacket(Collection<ExcavationRenderEntry> entries) {
        PacketByteBuf pBuff = new PacketByteBuf(Unpooled.buffer());

        pBuff.writeInt(entries.size());
        for (ExcavationRenderEntry r : entries) {
            pBuff.writeInt(r.id);
            pBuff.writeLong(r.aabb().minPos().asLong());
            pBuff.writeLong(r.aabb().maxPos().asLong());
            pBuff.writeBoolean(r.task.isExchange());
            final BlockPos[] positions = r.renderPositions();
            pBuff.writeInt(positions == null ? 0 : positions.length);
            if (positions != null) {
                if (XmConfig.logExcavationRenderTracking)
                    Xm.LOG.info("id %d Refresh toBytes position count = %d", r.id, positions == null ? 0 : positions.length);

                for (BlockPos pos : positions) {
                    pBuff.writeLong(pos.asLong());
                }
            }
        }

        return ServerSidePacketRegistry.INSTANCE.toPacket(ID, pBuff);
    }

    public static void accept(PacketContext context, PacketByteBuf pBuff) {
        ExcavationRenderManager.clear();
        int count = pBuff.readInt();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                int id = pBuff.readInt();
                BlockPos minPos = BlockPos.fromLong(pBuff.readLong());
                BlockPos maxPos = BlockPos.fromLong(pBuff.readLong());
                boolean isExchange = pBuff.readBoolean();
                int positionCount = pBuff.readInt();
                BlockPos[] list;
                if (positionCount == 0) {
                    list = null;
                } else {
                    list = new BlockPos[positionCount];
                    for (int j = 0; j < positionCount; j++) {
                        list[j] = BlockPos.fromLong(pBuff.readLong());
                    }

                }

                if (XmConfig.logExcavationRenderTracking)
                    Xm.LOG.info("id %d Refresh toBytes position count = %d", id, list == null ? 0 : list.length);
                ExcavationRenderManager.addOrUpdate(new ExcavationRenderer(id, new IntegerBox(minPos, maxPos).toAABB(), isExchange, list));
            }
        }
    }
}
