package grondag.brocade.placement;

import grondag.exotic_matter.serialization.IMessagePlusImmutable;
import grondag.exotic_matter.serialization.IReadWriteNBTImmutable;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.varia.ILocalized;
import grondag.exotic_matter.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;

public enum BlockOrientationAxis implements IMessagePlusImmutable<BlockOrientationAxis>,
        IReadWriteNBTImmutable<BlockOrientationAxis>, ILocalized {

    DYNAMIC(null), MATCH_CLOSEST(null), X(EnumFacing.Axis.X), Y(EnumFacing.Axis.Y), Z(EnumFacing.Axis.Z);

    private static final String TAG_ORIENTATION = NBTDictionary.claim("plcmnt_orient");

    public final EnumFacing.Axis axis;

    private BlockOrientationAxis(EnumFacing.Axis axis) {
        this.axis = axis;
    }

    @Override
    public BlockOrientationAxis deserializeNBT(NBTTagCompound tag) {
        return Useful.safeEnumFromTag(tag, TAG_ORIENTATION, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
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
        return I18n.translateToLocal("placement.orientation.axis." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
