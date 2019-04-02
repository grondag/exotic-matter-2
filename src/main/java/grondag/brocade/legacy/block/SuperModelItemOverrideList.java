package grondag.brocade.legacy.block;




import com.google.common.collect.Lists;

import grondag.brocade.model.varia.SuperDispatcher;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;




public class SuperModelItemOverrideList extends ItemOverrideList {
    private final SuperDispatcher dispatcher;

    public SuperModelItemOverrideList(SuperDispatcher dispatcher) {
        super(Lists.<ItemOverride>newArrayList());
        this.dispatcher = dispatcher;
    }

    @Override
    public BakedModel handleItemState(BakedModel originalModel, ItemStack stack,
            World world, LivingEntity entity) {
        return dispatcher.handleItemState(originalModel, stack);
    }
}
