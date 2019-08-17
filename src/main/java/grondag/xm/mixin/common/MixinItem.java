package grondag.xm.mixin.common;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.dispatch.XmItemAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(Item.class)
public class MixinItem implements XmItemAccess {
    private Function<ItemStack, ModelState.Mutable> modelStateFunc = null;

    @Override
    public void xm_modelStateFunc(Function<ItemStack, ModelState.Mutable> func) {
        modelStateFunc = func;
    }

    @Override
    public Function<ItemStack, ModelState.Mutable> xm_modelStateFunc() {
        return modelStateFunc;
    }
    
}
