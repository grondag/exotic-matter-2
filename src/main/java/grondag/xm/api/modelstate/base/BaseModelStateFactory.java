package grondag.xm.api.modelstate.base;

import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.primitive.ModelPrimitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface BaseModelStateFactory<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>> {

	W claim(ModelPrimitive<R, W> primitive);

	W fromBytes(ModelPrimitive<R, W> primitive, FriendlyByteBuf buf, PaintIndex sync);

	W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag, PaintIndex sync);
}