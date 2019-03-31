package grondag.brocade.placement;

import grondag.exotic_matter.serialization.IMessagePlusImmutable;
import grondag.exotic_matter.serialization.IReadWriteNBTImmutable;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Direction;
import net.minecraft.client.resource.language.I18n;

public enum BlockOrientationFace implements IMessagePlusImmutable<BlockOrientationFace>,
        IReadWriteNBTImmutable<BlockOrientationFace>, ILocalized {
    DYNAMIC(null), MATCH_CLOSEST(null), UP(Direction.UP), DOWN(Direction.DOWN), NORTH(Direction.NORTH),
    EAST(Direction.EAST), SOUTH(Direction.SOUTH), WEST(Direction.WEST);

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientFace");

    public final Direction face;

    private BlockOrientationFace(Direction face) {
        this.face = face;
    }

    @Override
    public BlockOrientationFace deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    @Override
    public BlockOrientationFace fromBytes(PacketBuffer pBuff) {
        return pBuff.readEnumValue(BlockOrientationFace.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeEnumValue(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.face." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
