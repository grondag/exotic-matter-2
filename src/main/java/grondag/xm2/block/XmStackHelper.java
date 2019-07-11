package grondag.xm2.block;



import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.Useful;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.ModelStateImpl;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Item stack serialization handlers
 * 
 * @author grondag
 *
 */
public class XmStackHelper {
    public static String NBT_SUPERMODEL_LIGHT_VALUE = NBTDictionary.claim("smLight");

    public static void setStackLightValue(ItemStack stack, int lightValue) {
        // important that the tag used here matches that used in tile entity
        Useful.getOrCreateTagCompound(stack).putByte(XmStackHelper.NBT_SUPERMODEL_LIGHT_VALUE,
                (byte) lightValue);
    }

    public static byte getStackLightValue(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        // important that the tag used here matches that used in tile entity
        return tag == null ? 0 : tag.getByte(XmStackHelper.NBT_SUPERMODEL_LIGHT_VALUE);
    }

    public static void setStackSubstance(ItemStack stack, BlockSubstance substance) {
        if (substance != null)
            substance.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static BlockSubstance getStackSubstance(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag == null ? BlockSubstance.DEFAULT : BlockSubstance.deserializeNBT(tag);
    }

    public static void setStackModelState(ItemStack stack, ModelState modelState) {
        CompoundTag tag = stack.getOrCreateTag();
        if (modelState == null) {
            ModelStateImpl.clearNBTValues(tag);
            return;
        }
        modelState.serializeNBT(tag);
    }

    
    public static ModelState getStackModelState(ItemStack stack) {
        ModelState stackState = stack.hasTag()
                ? ModelStateImpl.deserializeFromNBTIfPresent(stack.getTag())
                : null;

        // WAILA or other mods might create a stack with no NBT
        if (stackState != null)
            return stackState;

        if (stack.getItem() instanceof BlockItem) {
            BlockItem item = (BlockItem) stack.getItem();
            XmBlockStateImpl xmState = XmBlockStateAccess.get(item.getBlock().getDefaultState());
            if (xmState != null) {
                return xmState.defaultModelState;
            }
        }
        return null;
    }
}
