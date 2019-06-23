package grondag.brocade.dispatch;

import grondag.fermion.color.BlockColorMapProvider;
import grondag.fermion.color.ColorMap.EnumColorMap;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.state.ISuperModelState;
import net.minecraft.item.Item;

/**
 * Generic item class with a SuperModel render and state. Be sure to set
 * creative tab for mod that uses it.
 */
public class CraftingItem extends Item {
    public final ISuperModelState modelState;

    public CraftingItem(Settings settings, ISuperModelState modelState) {
        super(settings);
        this.modelState = modelState;
        int colorIndex = this.hashCode() % BlockColorMapProvider.INSTANCE.getColorMapCount();
        this.modelState.setColorRGB(PaintLayer.BASE,
                BlockColorMapProvider.INSTANCE.getColorMap(colorIndex).getColor(EnumColorMap.BASE));
    }
}
