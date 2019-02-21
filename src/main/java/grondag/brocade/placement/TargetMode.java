package grondag.brocade.placement;

import grondag.exotic_matter.serialization.IMessagePlusImmutable;
import grondag.exotic_matter.serialization.IReadWriteNBTImmutable;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.varia.ILocalized;
import grondag.exotic_matter.varia.Useful;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;

/**
 * Determines how blocks are to be selected for operation of the placement item.
 */
public enum TargetMode implements IMessagePlusImmutable<TargetMode>, IReadWriteNBTImmutable<TargetMode>, ILocalized {
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

    @Override
    public TargetMode deserializeNBT(NBTTagCompound tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    @Override
    public TargetMode fromBytes(PacketBuffer pBuff) {
        return pBuff.readEnumValue(TargetMode.class);
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeEnumValue(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String localizedName() {
        return I18n.translateToLocal("placement.target_mode." + this.name().toLowerCase());
    }
}
