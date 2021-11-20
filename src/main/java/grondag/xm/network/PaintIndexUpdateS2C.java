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

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import grondag.xm.Xm;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.paint.PaintIndexImpl;

@Internal
public abstract class PaintIndexUpdateS2C {
	private PaintIndexUpdateS2C() {
	}

	public static final ResourceLocation ID = new ResourceLocation(Xm.MODID, "piu");

	public static Packet<?> toPacket(XmPaint paint, int index) {
		final FriendlyByteBuf pBuff = new FriendlyByteBuf(Unpooled.buffer());
		pBuff.writeVarInt(index);
		paint.toFixedBytes(pBuff);
		return ServerPlayNetworking.createS2CPacket(ID, pBuff);
	}

	public static void accept(Minecraft client, ClientPacketListener handler, FriendlyByteBuf pBuff, PacketSender responseSender) {
		final int index = pBuff.readVarInt();
		final XmPaint paint = XmPaint.fromBytes(pBuff, null);

		if (client.isSameThread()) {
			PaintIndexImpl.CLIENT.updateClientIndex(paint, index);
		} else {
			client.execute(() -> PaintIndexImpl.CLIENT.updateClientIndex(paint, index));
		}
	}
}
