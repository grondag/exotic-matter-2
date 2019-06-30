package grondag.brocade;

import grondag.brocade.collision.CollisionBoxDispatcher;
import grondag.brocade.dispatch.BrocadeDispatcher;
import grondag.brocade.dispatch.BrocadeModelVariantProvider;
import grondag.brocade.init.BrocadeTextures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.InvalidateRenderStateCallback;

public class BrocadeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BrocadeTextures.init();
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(r -> new BrocadeModelVariantProvider());
        InvalidateRenderStateCallback.EVENT.register(BrocadeClient::invalidate);
    }
    
    public static void invalidate() {
        BrocadeDispatcher.INSTANCE.clear();
        CollisionBoxDispatcher.clear();
    }
}
