package grondag.xm.api.modelstate;

import grondag.xm.api.primitive.ModelPrimitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public interface ModelStateFactory<R extends PrimitiveModelState<R, W>, W extends MutablePrimitiveModelState<R,W>> {

    W claim(ModelPrimitive<R, W> primitive);

    W fromBuffer(ModelPrimitive<R, W> primitive, PacketByteBuf buf);

    W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag);

}