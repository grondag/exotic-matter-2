package grondag.xm.api.modelstate.base;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.primitive.ModelPrimitive;

public interface BaseModelStateFactory<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>> {

	W claim(ModelPrimitive<R, W> primitive);

	W fromBytes(ModelPrimitive<R, W> primitive, PacketByteBuf buf, PaintIndex sync);

	default W fromBytes(ModelPrimitive<R, W> primitive, PacketByteBuf buf) {
		return fromBytes(primitive, buf, PaintIndex.LOCAL);
	}

	W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag, PaintIndex sync);

	default W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag) {
		return fromTag(primitive, tag, PaintIndex.LOCAL);
	}
}