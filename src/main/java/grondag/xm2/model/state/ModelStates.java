package grondag.xm2.model.state;

import javax.annotation.Nullable;

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.OwnedModelState;
import net.minecraft.nbt.CompoundTag;

public abstract class ModelStates {
    private ModelStates() {}
    
    public static final int PRIMITIVE_BIT_COUNT = 6;
    
    public static OwnedModelState claimSimple(ModelPrimitive primitive) {
        return PrimitiveModelState.claim(primitive);
    }
    
    public static @Nullable OwnedModelState fromTag(CompoundTag tag) {
        return PrimitiveModelState.fromTag(tag);
    }
}
