package grondag.hard_science.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

public class Packets {

    public static void initializeCommon() {
        ServerSidePacketRegistry.INSTANCE.register(S2C_ExcavationRenderUpdate.ID, S2C_ExcavationRenderUpdate::accept);
        ServerSidePacketRegistry.INSTANCE.register(S2C_PacketExcavationRenderRefresh.ID, S2C_PacketExcavationRenderRefresh::accept);
    }
    
    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(C2S_OpenContainerStorageInteraction.ID, C2S_OpenContainerStorageInteraction::accept);
    }
}
