package grondag.xm.dispatch;

import java.util.function.Function;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.ModelState.Mutable;
import net.minecraft.item.ItemStack;

public interface XmItemAccess {
    @SuppressWarnings("unchecked")
    public static <T extends Mutable> T getModelState(ItemStack stack) {
        final Function<ItemStack, ModelState.Mutable> func = ((XmItemAccess)stack.getItem()).xm_modelStateFunc();
        return func == null ? null : (T) func.apply(stack);
    }

    Function<ItemStack, Mutable> xm_modelStateFunc();

    void xm_modelStateFunc(Function<ItemStack, Mutable> func);
}
