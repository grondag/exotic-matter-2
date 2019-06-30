package grondag.xm2.model;

import java.util.Collections;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemProxy extends ModelItemPropertyOverrideList {
    private final BakedModel owner;
    public ItemProxy(BakedModel owner) {
        super(null, null, null, Collections.emptyList());
        this.owner = owner;
    }
    
    @Override
    public BakedModel apply(BakedModel bakedModel_1, ItemStack itemStack_1, World world_1, LivingEntity livingEntity_1) {
        return owner;
    }
}