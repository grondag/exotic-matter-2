package grondag.xm.api.modelstate;

import javax.annotation.Nullable;

import grondag.xm.model.state.ModelStatesImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public interface ModelStates {

    public static @Nullable MutableModelState fromTag(CompoundTag tag) {
        return ModelStatesImpl.fromTag(tag);
    }
    
    public static @Nullable MutableModelState fromBuffer(PacketByteBuf buf) {
        return ModelStatesImpl.fromBuffer(buf);
    }
}
