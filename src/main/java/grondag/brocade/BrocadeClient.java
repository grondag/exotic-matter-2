package grondag.brocade;

import grondag.brocade.init.BrocadeTextures;
import net.fabricmc.api.ClientModInitializer;

public class BrocadeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BrocadeTextures.init();
    }

}
