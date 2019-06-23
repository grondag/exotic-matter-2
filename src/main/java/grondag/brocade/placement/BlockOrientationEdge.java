package grondag.brocade.placement;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import grondag.brocade.world.BlockCorner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.client.resource.language.I18n;

public enum BlockOrientationEdge implements ILocalized {
    DYNAMIC(null), MATCH_CLOSEST(null), UP_EAST(BlockCorner.UP_EAST), UP_WEST(BlockCorner.UP_WEST),
    UP_NORTH(BlockCorner.UP_NORTH), UP_SOUTH(BlockCorner.UP_SOUTH), NORTH_EAST(BlockCorner.NORTH_EAST),
    NORTH_WEST(BlockCorner.NORTH_WEST), SOUTH_EAST(BlockCorner.SOUTH_EAST), SOUTH_WEST(BlockCorner.SOUTH_WEST),
    DOWN_EAST(BlockCorner.DOWN_EAST), DOWN_WEST(BlockCorner.DOWN_WEST), DOWN_NORTH(BlockCorner.DOWN_NORTH),
    DOWN_SOUTH(BlockCorner.DOWN_SOUTH);

    public final BlockCorner edge;

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientEdge");

    private BlockOrientationEdge(BlockCorner edge) {
        this.edge = edge;
    }

    public BlockOrientationEdge deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public BlockOrientationEdge fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(BlockOrientationEdge.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.edge." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
