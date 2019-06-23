package grondag.brocade.block;



import grondag.brocade.state.MeshState;
import grondag.brocade.state.MeshStateImpl;
import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.Useful;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Item stack serialization handlers
 * 
 * @author grondag
 *
 */
public class BrocadeBlockStackHelper {
    public static String NBT_SUPERMODEL_LIGHT_VALUE = NBTDictionary.claim("smLight");

    public static void setStackLightValue(ItemStack stack, int lightValue) {
        // important that the tag used here matches that used in tile entity
        Useful.getOrCreateTagCompound(stack).putByte(BrocadeBlockStackHelper.NBT_SUPERMODEL_LIGHT_VALUE,
                (byte) lightValue);
    }

    public static byte getStackLightValue(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        // important that the tag used here matches that used in tile entity
        return tag == null ? 0 : tag.getByte(BrocadeBlockStackHelper.NBT_SUPERMODEL_LIGHT_VALUE);
    }

    public static void setStackSubstance(ItemStack stack, BlockSubstance substance) {
        if (substance != null)
            substance.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static BlockSubstance getStackSubstance(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag == null ? BlockSubstance.DEFAULT : BlockSubstance.deserializeNBT(tag);
    }

    public static void setStackModelState(ItemStack stack, MeshState modelState) {
        CompoundTag tag = stack.getOrCreateTag();
        if (modelState == null) {
            MeshStateImpl.clearNBTValues(tag);
            return;
        }
        modelState.serializeNBT(tag);
    }

    
    public static MeshState getStackModelState(ItemStack stack) {
        MeshState stackState = stack.hasTag()
                ? MeshStateImpl.deserializeFromNBTIfPresent(stack.getTag())
                : null;

        // WAILA or other mods might create a stack with no NBT
        if (stackState != null)
            return stackState;

        if (stack.getItem() instanceof BlockItem) {
            BlockItem item = (BlockItem) stack.getItem();
            if (item.getBlock() instanceof BrocadeBlock) {
                return ((BrocadeBlock) item.getBlock()).getDefaultModelState();
            }
        }
        return null;
    }
}
