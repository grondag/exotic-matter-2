package grondag.xm2.dispatch;

import grondag.fermion.color.BlockColorMapProvider;
import grondag.fermion.color.ColorMap.EnumColorMap;
import grondag.xm2.painting.PaintLayer;
import grondag.xm2.state.ModelState;
import net.minecraft.item.Item;

/**
 * Generic item class with a SuperModel render and state. Be sure to set
 * creative tab for mod that uses it.
 */
public class CraftingItem extends Item {
    public final ModelState modelState;

    public CraftingItem(Settings settings, ModelState modelState) {
        super(settings);
        this.modelState = modelState;
        int colorIndex = this.hashCode() % BlockColorMapProvider.INSTANCE.getColorMapCount();
        this.modelState.setColorRGB(PaintLayer.BASE,
                BlockColorMapProvider.INSTANCE.getColorMap(colorIndex).getColor(EnumColorMap.BASE));
    }
}
