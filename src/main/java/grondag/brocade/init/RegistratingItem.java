package grondag.brocade.init;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;

public class RegistratingItem extends Item implements IItemModelRegistrant
{

    @Override
    public void handleBake(ModelBakeEvent event)
    {
        // let normal loader handle it
    }

}
