package grondag.xm2.dispatch;

import grondag.xm2.block.XmBlock;
import grondag.xm2.model.XmModelProxy;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.registry.Registry;

public class XmVariantProvider implements ModelVariantProvider {
    private final ObjectOpenHashSet<String> targets = new ObjectOpenHashSet<>();
    
    public XmVariantProvider() {
        targets.clear();
        Registry.BLOCK.forEach(b -> {
            if(b instanceof XmBlock) {
                targets.add(Registry.BLOCK.getId(b).toString());
            }
        });
    }
    
    @Override
    public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
        return targets.contains(modelId.getNamespace() + ":" + modelId.getPath())
                ? XmModelProxy.INSTANCE
                : null;
    }
}
