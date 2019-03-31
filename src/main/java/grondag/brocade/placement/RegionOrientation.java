package grondag.brocade.placement;

import grondag.exotic_matter.serialization.IMessagePlusImmutable;
import grondag.exotic_matter.serialization.IReadWriteNBTImmutable;
import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.resource.language.I18n;

public enum RegionOrientation
        implements IMessagePlusImmutable<RegionOrientation>, IReadWriteNBTImmutable<RegionOrientation>, ILocalized {
    AUTOMATIC, XYZ, ZYX, ZXY, XZY, YXZ, YZX;

    private static final String TAG_NAME = NBTDictionary.claim("regionOrientation");

    @Override
    public RegionOrientation deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    @Override
    public RegionOrientation fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumValue(RegionOrientation.class);
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumValue(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.region." + this.name().toLowerCase());
    }

    public BlockPos rotatedRegionPos(BlockPos fromPos) {
        switch (this) {
        case XYZ:
        case AUTOMATIC:
        default:
            return fromPos;

        case XZY:
            return new BlockPos(fromPos.getX(), fromPos.getZ(), fromPos.getY());

        case YXZ:
            return new BlockPos(fromPos.getY(), fromPos.getX(), fromPos.getZ());

        case YZX:
            return new BlockPos(fromPos.getY(), fromPos.getZ(), fromPos.getX());

        case ZXY:
            return new BlockPos(fromPos.getZ(), fromPos.getX(), fromPos.getY());

        case ZYX:
            return new BlockPos(fromPos.getZ(), fromPos.getY(), fromPos.getX());

        }
    }
}
