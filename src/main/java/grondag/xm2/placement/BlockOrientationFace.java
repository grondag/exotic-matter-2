package grondag.xm2.placement;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Direction;
import net.minecraft.client.resource.language.I18n;

public enum BlockOrientationFace implements ILocalized {
    DYNAMIC(null), MATCH_CLOSEST(null), UP(Direction.UP), DOWN(Direction.DOWN), NORTH(Direction.NORTH),
    EAST(Direction.EAST), SOUTH(Direction.SOUTH), WEST(Direction.WEST);

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientFace");

    public final Direction face;

    private BlockOrientationFace(Direction face) {
        this.face = face;
    }

    public BlockOrientationFace deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public BlockOrientationFace fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(BlockOrientationFace.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.face." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
