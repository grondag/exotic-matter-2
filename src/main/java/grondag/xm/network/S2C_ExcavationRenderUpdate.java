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
 * Adds, changes or removes an excavation render entry in current client list.
 * Sent when a new excavation is added, the bounds change, or excavation is
 * removed.
 */
@Internal
public abstract class S2C_ExcavationRenderUpdate {
	private S2C_ExcavationRenderUpdate() {
	}

	public static final ResourceLocation ID = new ResourceLocation(Xm.MODID, "exru");

	/**
	 * Use this for new and changed.
	 */
	public static Packet<?> toPacket(ExcavationRenderEntry entry) {
		final IntegerBox aabb = entry.aabb();
		final BlockPos[] positions = entry.renderPositions();
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id %d New update packet position count = %d, aabb=%s", entry.id, positions == null ? 0 : positions.length,
					aabb == null ? "null" : aabb.toString());
		}
		return toPacket(entry.id, aabb, entry.task.isExchange(), positions);
	}

	/**
	 *
	 * Use this for deleted.
	 */
	public static Packet<?> toPacket(int deletedId) {
		return toPacket(deletedId, null, false, null);
	}

	private static Packet<?> toPacket(int id, IntegerBox aabb, boolean isExchange, BlockPos[] positions) {
		final FriendlyByteBuf pBuff = new FriendlyByteBuf(Unpooled.buffer());
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id %d Update toBytes position count = %d", id, positions == null ? 0 : positions.length);
		}

		pBuff.writeInt(id);
		// deletion flag
		pBuff.writeBoolean(aabb == null);
		if (aabb != null) {
			pBuff.writeLong(aabb.minPos().asLong());
			pBuff.writeLong(aabb.maxPos().asLong());
			pBuff.writeBoolean(isExchange);

			pBuff.writeInt(positions == null ? 0 : positions.length);
			if (positions != null) {
				for (final BlockPos pos : positions) {
					pBuff.writeLong(pos.asLong());
				}
			}
		}

		return ServerPlayNetworking.createS2CPacket(ID, pBuff);
	}

	public static void accept(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
		// FIX: thread safety

		final int id = buf.readInt();
		final IntegerBox aabb;
		final BlockPos[] positions;
		final boolean isExchange;

		if (buf.readBoolean()) {
			// deletion
			aabb = null;
			positions = null;
			isExchange = false;
		} else {
			final BlockPos minPos = BlockPos.of(buf.readLong());
			final BlockPos maxPos = BlockPos.of(buf.readLong());
			aabb = new IntegerBox(minPos, maxPos);
			isExchange = buf.readBoolean();

			final int posCount = buf.readInt();

			if (posCount == 0) {
				positions = null;
			} else {
				positions = new BlockPos[posCount];
				for (int i = 0; i < posCount; i++) {
					positions[i] = BlockPos.of(buf.readLong());
				}
			}
		}

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id %d Update fromBytes position count = %d", id, positions == null ? 0 : positions.length);
		}

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id %d Update handler position count = %d, aabb=%s", id, positions == null ? 0 : positions.length,
					aabb == null ? "null" : aabb.toString());
		}

		if (aabb == null) {
			ExcavationRenderManager.remove(id);
		} else {
			ExcavationRenderManager.addOrUpdate(new ExcavationRenderer(id, aabb.toAABB(), isExchange, positions));
		}
	}
}
