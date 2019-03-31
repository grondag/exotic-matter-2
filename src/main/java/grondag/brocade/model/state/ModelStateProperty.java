package grondag.brocade.model.state;



import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelStateProperty implements IUnlistedProperty<ISuperModelState> {

    @Override
    public final String getName() {
        return "ModelState";
    }

    @Override
    public final boolean isValid(ISuperModelState value) {
        return true;
    }

    @Override
    public final String valueToString(ISuperModelState value) {
        return value.toString();
    }

    @Override
    public final Class<ISuperModelState> getType() {
        return ISuperModelState.class;
    }
}
