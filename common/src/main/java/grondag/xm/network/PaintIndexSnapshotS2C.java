/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
public abstract class PaintIndexSnapshotS2C {
	private PaintIndexSnapshotS2C() { }

	public static final ResourceLocation ID = new ResourceLocation(Xm.MODID, "pxs");

	public static Packet<?> toPacket(PaintIndexImpl paintIndex) {
		final FriendlyByteBuf pBuff = new FriendlyByteBuf(Unpooled.buffer());
		paintIndex.toBytes(pBuff);
		return ServerPlayNetworking.createS2CPacket(ID, pBuff);
	}

	public static void accept(Minecraft client, ClientPacketListener handler, FriendlyByteBuf pBuff, PacketSender responseSender) {
		final XmPaint[] paints = PaintIndexImpl.arrayFromBytes(pBuff);

		if (client.isSameThread()) {
			PaintIndexImpl.CLIENT.fromArray(paints);
		} else {
			client.execute(() -> PaintIndexImpl.CLIENT.fromArray(paints));
		}
	}
}
