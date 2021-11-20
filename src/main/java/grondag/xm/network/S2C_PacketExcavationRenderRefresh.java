/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.network;

import java.util.Collection;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import grondag.fermion.position.IntegerBox;
import grondag.xm.Xm;
import grondag.xm.XmConfig;
import grondag.xm.virtual.ExcavationRenderEntry;
import grondag.xm.virtual.ExcavationRenderManager;
import grondag.xm.virtual.ExcavationRenderer;

/**
 * Replaces all excavation render entries. Sent when players logs in, changes
 * dimension or changes active domain.
 */
@Internal
public abstract class S2C_PacketExcavationRenderRefresh {
	private S2C_PacketExcavationRenderRefresh() { }

	public static final ResourceLocation ID = new ResourceLocation(Xm.MODID, "exrr");

	public static Packet<?> toPacket(Collection<ExcavationRenderEntry> entries) {
		final FriendlyByteBuf pBuff = new FriendlyByteBuf(Unpooled.buffer());

		pBuff.writeInt(entries.size());

		for (final ExcavationRenderEntry r : entries) {
			pBuff.writeInt(r.id);
			pBuff.writeLong(r.aabb().minPos().asLong());
			pBuff.writeLong(r.aabb().maxPos().asLong());
			pBuff.writeBoolean(r.task.isExchange());
			final BlockPos[] positions = r.renderPositions();
			pBuff.writeInt(positions == null ? 0 : positions.length);

			if (positions != null) {
				if (XmConfig.logExcavationRenderTracking) {
					Xm.LOG.info("id %d Refresh toBytes position count = %d", r.id, positions == null ? 0 : positions.length);
				}

				for (final BlockPos pos : positions) {
					pBuff.writeLong(pos.asLong());
				}
			}
		}

		return ServerPlayNetworking.createS2CPacket(ID, pBuff);
	}

	public static void accept(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf pBuff, PacketSender responseSender) {
		// FIX: thread safety

		ExcavationRenderManager.clear();
		final int count = pBuff.readInt();

		if (count > 0) {
			for (int i = 0; i < count; i++) {
				final int id = pBuff.readInt();
				final BlockPos minPos = BlockPos.of(pBuff.readLong());
				final BlockPos maxPos = BlockPos.of(pBuff.readLong());
				final boolean isExchange = pBuff.readBoolean();
				final int positionCount = pBuff.readInt();
				BlockPos[] list;

				if (positionCount == 0) {
					list = null;
				} else {
					list = new BlockPos[positionCount];

					for (int j = 0; j < positionCount; j++) {
						list[j] = BlockPos.of(pBuff.readLong());
					}
				}

				if (XmConfig.logExcavationRenderTracking) {
					Xm.LOG.info("id %d Refresh toBytes position count = %d", id, list == null ? 0 : list.length);
				}

				ExcavationRenderManager.addOrUpdate(new ExcavationRenderer(id, new IntegerBox(minPos, maxPos).toAABB(), isExchange, list));
			}
		}
	}
}
