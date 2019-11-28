package grondag.xm.api.modelstate.base;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

import grondag.xm.api.primitive.ModelPrimitive;

public interface BaseModelStateFactory<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>> {

	W claim(ModelPrimitive<R, W> primitive);

	W fromBuffer(ModelPrimitive<R, W> primitive, PacketByteBuf buf);

	W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag);

}