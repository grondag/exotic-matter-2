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

public enum BlockOrientationAxis implements IMessagePlusImmutable<BlockOrientationAxis>,
        IReadWriteNBTImmutable<BlockOrientationAxis>, ILocalized {

    DYNAMIC(null), MATCH_CLOSEST(null), X(Direction.Axis.X), Y(Direction.Axis.Y), Z(Direction.Axis.Z);

    private static final String TAG_ORIENTATION = NBTDictionary.claim("plcmnt_orient");

    public final Direction.Axis axis;

    private BlockOrientationAxis(Direction.Axis axis) {
        this.axis = axis;
    }

    @Override
    public BlockOrientationAxis deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_ORIENTATION, this);
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_ORIENTATION, this);
    }

    @Override
    public BlockOrientationAxis fromBytes(PacketBuffer pBuff) {
        return pBuff.readEnumValue(BlockOrientationAxis.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeEnumValue(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.axis." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
