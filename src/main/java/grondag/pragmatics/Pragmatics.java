package grondag.pragmatics;

import grondag.pragmatics.impl.PlayerModifierHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

public class Pragmatics implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerSidePacketRegistry.INSTANCE.register(PlayerModifierHandler.PACKET_ID, PlayerModifierHandler::accept);
    }
}
