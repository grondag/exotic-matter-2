package grondag.brocade.model.state;

import javax.annotation.Nullable;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelStateProperty implements IUnlistedProperty<ISuperModelState> {

    @Override
    public final String getName() {
        return "ModelState";
    }

    @Override
    public final boolean isValid(@Nullable ISuperModelState value) {
        return true;
    }

    @Override
    public final String valueToString(@Nullable ISuperModelState value) {
        return value.toString();
    }

    @Override
    public final Class<ISuperModelState> getType() {
        return ISuperModelState.class;
    }
}
