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

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// WIP: Fabric deps
@Internal
public class Packets {
	public static void initializeCommon() { }

	@Environment(EnvType.CLIENT)
	public static void initializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(PaintIndexUpdateS2C.ID, PaintIndexUpdateS2C::accept);
		ClientPlayNetworking.registerGlobalReceiver(PaintIndexSnapshotS2C.ID, PaintIndexSnapshotS2C::accept);
		//		ClientSidePacketRegistry.INSTANCE.register(S2C_ExcavationRenderUpdate.ID, S2C_ExcavationRenderUpdate::accept);
		//		ClientSidePacketRegistry.INSTANCE.register(S2C_PacketExcavationRenderRefresh.ID, S2C_PacketExcavationRenderRefresh::accept);
	}
}
