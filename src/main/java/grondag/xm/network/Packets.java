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

import org.apiguardian.api.API;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@API(status = INTERNAL)
public class Packets {

	public static void initializeCommon() {
	}

	@Environment(EnvType.CLIENT)
	public static void initializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(PaintIndexUpdateS2C.ID, PaintIndexUpdateS2C::accept);
		ClientPlayNetworking.registerGlobalReceiver(PaintIndexSnapshotS2C.ID, PaintIndexSnapshotS2C::accept);
		//		ClientSidePacketRegistry.INSTANCE.register(S2C_ExcavationRenderUpdate.ID, S2C_ExcavationRenderUpdate::accept);
		//		ClientSidePacketRegistry.INSTANCE.register(S2C_PacketExcavationRenderRefresh.ID, S2C_PacketExcavationRenderRefresh::accept);
	}
}
