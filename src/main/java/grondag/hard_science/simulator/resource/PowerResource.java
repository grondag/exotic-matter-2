package grondag.hard_science.simulator.resource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import net.minecraft.util.text.translation.I18n;

public class PowerResource extends AbstractResource<StorageType.StorageTypePower>
{
    public static final PowerResource JOULES = new PowerResource("joules");
    
    private final String unlocalizedName;
    
    public PowerResource(String unlocalizedName)
    {
        this.unlocalizedName = unlocalizedName;
    }
    
    @Override
    public StorageTypePower storageType()
    {
        return StorageType.POWER;
    }

    @Override
    public String displayName()
    {
        return I18n.translateToLocal("misc.power." + this.unlocalizedName + ".name");
    }

    @Override
    public AbstractResourceWithQuantity<StorageTypePower> withQuantity(long quantity)
    {
        return new PowerResourceWithQuantity(this, quantity);
    }

    @Override
    public boolean isResourceEqual(@Nullable IResource<?> other)
    {
        return other == this;
    }
}
