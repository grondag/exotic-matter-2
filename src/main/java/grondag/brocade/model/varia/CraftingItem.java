package grondag.brocade.model.varia;

import grondag.brocade.init.IItemModelRegistrant;
import grondag.exotic_matter.model.color.BlockColorMapProvider;
import grondag.exotic_matter.model.color.ColorMap.EnumColorMap;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;

/**
 * Generic item class with a SuperModel render and state. Be sure to set
 * creative tab for mod that uses it.
 */
public class CraftingItem extends Item implements IItemModelRegistrant {
    public final ISuperModelState modelState;

    public CraftingItem(String name, ISuperModelState modelState) {
        super();
        this.modelState = modelState;
        int colorIndex = this.hashCode() % BlockColorMapProvider.INSTANCE.getColorMapCount();
        this.modelState.setColorRGB(PaintLayer.BASE,
                BlockColorMapProvider.INSTANCE.getColorMap(colorIndex).getColor(EnumColorMap.BASE));
        this.setRegistryName(name);
        this.setTranslationKey(name);
    }

    @Override
    public void handleBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "inventory"),
                SuperDispatcher.INSTANCE.getItemDelegate());
    }
}
