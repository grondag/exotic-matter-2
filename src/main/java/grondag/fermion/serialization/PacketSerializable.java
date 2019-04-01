package grondag.fermion.serialization;

import net.minecraft.util.PacketByteBuf;

//TODO: move to Fermion
public interface PacketSerializable {
    void fromBytes(PacketByteBuf buffer);

    void toBytes(PacketByteBuf buffer);
}
