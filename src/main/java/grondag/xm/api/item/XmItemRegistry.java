package grondag.xm.api.item;

import java.util.function.Function;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.dispatch.XmRegistryImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class XmItemRegistry {
    private XmItemRegistry() {}

    public static void addItem(Item item, Function<ItemStack, ModelState.Mutable> modelFunction) {
        XmRegistryImpl.register(item, modelFunction);
    }
}
