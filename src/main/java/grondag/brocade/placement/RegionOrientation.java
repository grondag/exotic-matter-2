package grondag.brocade.placement;

import grondag.exotic_matter.serialization.IMessagePlusImmutable;
import grondag.exotic_matter.serialization.IReadWriteNBTImmutable;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.varia.ILocalized;
import grondag.exotic_matter.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

public enum RegionOrientation implements IMessagePlusImmutable<RegionOrientation>, IReadWriteNBTImmutable<RegionOrientation>, ILocalized
{
    AUTOMATIC,
    XYZ,
    ZYX,
    ZXY,
    XZY,
    YXZ,
    YZX;
    
    private static final String TAG_NAME = NBTDictionary.claim("regionOrientation");
    
    @Override
    public RegionOrientation deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    @Override
    public RegionOrientation fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(RegionOrientation.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeEnumValue(this);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public String localizedName()
    {
        return I18n.translateToLocal("placement.orientation.region." + this.name().toLowerCase());
    }
    
    public BlockPos rotatedRegionPos(BlockPos fromPos)
    {
        switch(this)
        {
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
