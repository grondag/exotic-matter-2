package grondag.xm2.placement;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.client.resource.language.I18n;

/**
 * Determines how blocks are to be selected for operation of the placement item.
 */
public enum TargetMode implements ILocalized {
    /** affect a single block - normal MC behavior */
    ON_CLICKED_FACE(false),

    /** use the placement item's selection region */
    FILL_REGION(true),

    /** use only the exterior blocks the placement item's selection region */
    HOLLOW_REGION(true),

    /**
     * use the placement item's selection region ONLY if all blocks in region match
     * the filter criteria
     */
    COMPLETE_REGION(true),

    /**
     * flood fill search for blocks that match the clicked block - like an exchanger
     */
    MATCH_CLICKED_BLOCK(false),

    /**
     * flood fill of adjacent surfaces that match the clicked block - like a
     * builder's wand
     */
    ON_CLICKED_SURFACE(false);

    private static final String TAG_NAME = NBTDictionary.claim("targetMode");

    /**
     * If true, this mode uses the geometrically-defined volume defined by the
     * placement item's current selection region. By extension, also determines if
     * the current filter mode applies. If false, affects a single block or employs
     * some other logic for determining what blocks are affected.
     */
    public final boolean usesSelectionRegion;

    private TargetMode(boolean usesSelectionRegion) {
        this.usesSelectionRegion = usesSelectionRegion;
    }

    public TargetMode deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public TargetMode fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(TargetMode.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.target_mode." + this.name().toLowerCase());
    }
}
