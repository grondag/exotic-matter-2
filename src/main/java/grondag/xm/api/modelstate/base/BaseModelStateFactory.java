package grondag.xm.api.modelstate.base;

import grondag.xm.api.primitive.ModelPrimitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public interface BaseModelStateFactory<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>> {

    W claim(ModelPrimitive<R, W> primitive);

    W fromBuffer(ModelPrimitive<R, W> primitive, PacketByteBuf buf);

    W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag);

}