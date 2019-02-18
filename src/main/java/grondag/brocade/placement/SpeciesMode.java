package grondag.brocade.placement;

import grondag.exotic_matter.serialization.IMessagePlusImmutable;
import grondag.exotic_matter.serialization.IReadWriteNBTImmutable;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.varia.ILocalized;
import grondag.exotic_matter.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

public enum SpeciesMode implements IMessagePlusImmutable<SpeciesMode>, IReadWriteNBTImmutable<SpeciesMode>, ILocalized
{
    MATCH_CLICKED,
    MATCH_MOST,
    COUNTER_MOST;
    
    private static final String TAG_NAME = NBTDictionary.claim("speciesMode");

    @Override
    public SpeciesMode deserializeNBT(NBTTagCompound tag)
    {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    @Override
    public SpeciesMode fromBytes(PacketBuffer pBuff)
    {
        return pBuff.readEnumValue(SpeciesMode.class);
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
        return I18n.translateToLocal("placement.species_mode." + this.name().toLowerCase());
    }
    
    /** mode to use if player holding alt key */
    public SpeciesMode alternate()
    {
        switch(this)
        {
        case COUNTER_MOST:
        default:
            return MATCH_CLICKED;
            
        case MATCH_CLICKED:
            return COUNTER_MOST;
            
        case MATCH_MOST:
            return COUNTER_MOST;
        
        }
    }
}
