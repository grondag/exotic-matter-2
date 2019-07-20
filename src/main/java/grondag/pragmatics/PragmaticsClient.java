package grondag.pragmatics;

import grondag.pragmatics.impl.PlayerModifierHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;

public class PragmaticsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register(PlayerModifierHandler::update);
    }
}
