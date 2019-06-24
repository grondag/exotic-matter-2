package grondag.brocade.dispatch;

import grondag.brocade.block.BrocadeBlock;
import grondag.brocade.model.BrocadeModelProxy;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.registry.Registry;

public class BrocadeModelVariantProvider implements ModelVariantProvider {
    private final ObjectOpenHashSet<String> targets = new ObjectOpenHashSet<>();
    
    public BrocadeModelVariantProvider() {
        targets.clear();
        Registry.BLOCK.forEach(b -> {
            if(b instanceof BrocadeBlock) {
                targets.add(Registry.BLOCK.getId(b).toString());
            }
        });
    }
    
    @Override
    public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
        return targets.contains(modelId.getNamespace() + ":" + modelId.getPath())
                ? BrocadeModelProxy.INSTANCE
                : null;
    }
}
