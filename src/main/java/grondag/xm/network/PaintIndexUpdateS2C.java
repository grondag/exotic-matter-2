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

import static org.apiguardian.api.API.Status.INTERNAL;

import io.netty.buffer.Unpooled;
import org.apiguardian.api.API;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import grondag.xm.Xm;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.paint.PaintIndexImpl;

@API(status = INTERNAL)
public abstract class PaintIndexUpdateS2C {
	private PaintIndexUpdateS2C() {
	}

	public static final Identifier ID = new Identifier(Xm.MODID, "piu");

	public static Packet<?> toPacket(XmPaint paint, int index) {
		final PacketByteBuf pBuff = new PacketByteBuf(Unpooled.buffer());
		pBuff.writeVarInt(index);
		paint.toFixedBytes(pBuff);
		return ServerSidePacketRegistry.INSTANCE.toPacket(ID, pBuff);
	}

	public static void accept(PacketContext context, PacketByteBuf pBuff) {
		final int index = pBuff.readVarInt();
		final XmPaint paint = XmPaint.fromBytes(pBuff, null);

		if (context.getTaskQueue().isOnThread()) {
			PaintIndexImpl.CLIENT.updateClientIndex(paint, index);
		} else {
			context.getTaskQueue().execute(() -> PaintIndexImpl.CLIENT.updateClientIndex(paint, index));
		}
	}
}