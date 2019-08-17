package grondag.xm.api.item;

import javax.annotation.Nullable;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.dispatch.XmItemAccess;
import net.minecraft.item.ItemStack;

public class XmItem {
    private XmItem() {}
    
    public static @Nullable <T extends ModelState.Mutable> T modelState(ItemStack stack) {
        return XmItemAccess.getModelState(stack);
    }
}
