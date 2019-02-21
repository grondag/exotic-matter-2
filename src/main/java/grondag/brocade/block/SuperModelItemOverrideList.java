package grondag.brocade.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import grondag.exotic_matter.model.varia.SuperDispatcher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SuperModelItemOverrideList extends ItemOverrideList {
    private final SuperDispatcher dispatcher;

    public SuperModelItemOverrideList(SuperDispatcher dispatcher) {
        super(Lists.<ItemOverride>newArrayList());
        this.dispatcher = dispatcher;
    }

    @Override
    public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack,
            @Nullable World world, @Nullable EntityLivingBase entity) {
        return dispatcher.handleItemState(originalModel, stack);
    }
}
