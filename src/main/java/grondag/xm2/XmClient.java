package grondag.xm2;

import grondag.xm2.collision.CollisionBoxDispatcher;
import grondag.xm2.dispatch.XmDispatcher;
import grondag.xm2.dispatch.XmVariantProvider;
import grondag.xm2.init.XmTextures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.InvalidateRenderStateCallback;

public class XmClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        XmTextures.init();
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(r -> new XmVariantProvider());
        InvalidateRenderStateCallback.EVENT.register(XmClient::invalidate);
    }
    
    public static void invalidate() {
        XmDispatcher.INSTANCE.clear();
        CollisionBoxDispatcher.clear();
    }
}
