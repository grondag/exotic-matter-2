package grondag.xm2.placement;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.client.resource.language.I18n;

public enum SpeciesMode implements ILocalized {
    MATCH_CLICKED, MATCH_MOST, COUNTER_MOST;

    private static final String TAG_NAME = NBTDictionary.claim("speciesMode");

    public SpeciesMode deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public SpeciesMode fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(SpeciesMode.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.species_mode." + this.name().toLowerCase());
    }

    /** mode to use if player holding alt key */
    public SpeciesMode alternate() {
        switch (this) {
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
