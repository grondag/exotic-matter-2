package grondag.xm2.placement;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import grondag.xm2.connect.api.model.BlockEdge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.client.resource.language.I18n;

public enum BlockOrientationEdge implements ILocalized {
    DYNAMIC(null), 
    MATCH_CLOSEST(null), 
    UP_EAST(BlockEdge.UP_EAST), 
    UP_WEST(BlockEdge.UP_WEST),
    UP_NORTH(BlockEdge.UP_NORTH), 
    UP_SOUTH(BlockEdge.UP_SOUTH), 
    NORTH_EAST(BlockEdge.NORTH_EAST),
    NORTH_WEST(BlockEdge.NORTH_WEST), 
    SOUTH_EAST(BlockEdge.SOUTH_EAST), 
    SOUTH_WEST(BlockEdge.SOUTH_WEST),
    DOWN_EAST(BlockEdge.DOWN_EAST), 
    DOWN_WEST(BlockEdge.DOWN_WEST), 
    DOWN_NORTH(BlockEdge.DOWN_NORTH),
    DOWN_SOUTH(BlockEdge.DOWN_SOUTH);

    public final BlockEdge edge;

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientEdge");

    private BlockOrientationEdge(BlockEdge edge) {
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
