package grondag.brocade.placement;

import grondag.brocade.legacy.block.ISuperBlock;
import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.World;

/**
 * For selection modes that use a region, determines which blocks in the region
 * are affected.
 */
public enum FilterMode implements ILocalized {
    FILL_REPLACEABLE(false), REPLACE_SOLID(false), REPLACE_ALL(false), REPLACE_ONLY(true), REPLACE_ALL_EXCEPT(true);

    private static String NBT_TAG = NBTDictionary.claim("filterMode");

    /**
     * True if this mode uses the list of specific blocks configured in the
     * placement item as filters.
     */
    public final boolean usesFilterBlock;

    private FilterMode(boolean usesFilterBlock) {
        this.usesFilterBlock = usesFilterBlock;
    }

    public FilterMode deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, NBT_TAG, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, NBT_TAG, this);
    }

    public FilterMode fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(FilterMode.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.filter_mode." + this.name().toLowerCase());
    }

    /**
     * If isVirtual then will only affect virtual blocks and empty space.
     */
    public boolean shouldAffectBlock(BlockState blockState, World world, BlockPos pos, ItemStack stack,
            boolean isVirtual) {
        Block block = blockState.getBlock();

        switch (this) {
        case FILL_REPLACEABLE:
            return block.getMaterial(blockState).isReplaceable() && !ISuperBlock.isVirtualBlock(block);

        case REPLACE_ALL:
            if (isVirtual) {
                return block.getMaterial(blockState).isReplaceable() || ISuperBlock.isVirtualBlock(block);
            } else {
                return !ISuperBlock.isVirtualBlock(block);
            }

        case REPLACE_ALL_EXCEPT:
            // TODO
            return true;

        case REPLACE_ONLY:
            // TODO
            return false;

        case REPLACE_SOLID:
            // test for non-virtual relies on fact that all virtual blocks are replaceable
            return isVirtual ? ISuperBlock.isVirtualBlock(block) : !block.getMaterial(blockState).isReplaceable();

        default:
            return false;

        }
    }
}
