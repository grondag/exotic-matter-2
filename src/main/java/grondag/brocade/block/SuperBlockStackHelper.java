package grondag.brocade.block;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelState;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.varia.Useful;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Item stack serialization handlers
 * 
 * @author grondag
 *
 */
public class SuperBlockStackHelper {
    public static String NBT_SUPERMODEL_LIGHT_VALUE = NBTDictionary.claim("smLight");

    public static void setStackLightValue(ItemStack stack, int lightValue) {
        // important that the tag used here matches that used in tile entity
        Useful.getOrCreateTagCompound(stack).setByte(SuperBlockStackHelper.NBT_SUPERMODEL_LIGHT_VALUE,
                (byte) lightValue);
    }

    public static byte getStackLightValue(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        // important that the tag used here matches that used in tile entity
        return tag == null ? 0 : tag.getByte(SuperBlockStackHelper.NBT_SUPERMODEL_LIGHT_VALUE);
    }

    public static void setStackSubstance(ItemStack stack, BlockSubstance substance) {
        if (substance != null)
            substance.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public static BlockSubstance getStackSubstance(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? BlockSubstance.DEFAULT : BlockSubstance.deserializeNBT(tag);
    }

    public static void setStackModelState(ItemStack stack, @Nullable ISuperModelState modelState) {
        NBTTagCompound tag = stack.getTagCompound();
        if (modelState == null) {
            ModelState.clearNBTValues(tag);
            return;
        }

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        modelState.serializeNBT(tag);
    }

    @Nullable
    public static ISuperModelState getStackModelState(ItemStack stack) {
        @SuppressWarnings("null")
        ISuperModelState stackState = stack.hasTagCompound()
                ? ModelState.deserializeFromNBTIfPresent(stack.getTagCompound())
                : null;

        // WAILA or other mods might create a stack with no NBT
        if (stackState != null)
            return stackState;

        if (stack.getItem() instanceof ItemBlock) {
            ItemBlock item = (ItemBlock) stack.getItem();
            if (item.getBlock() instanceof ISuperBlock) {
                return ((ISuperBlock) item.getBlock()).getDefaultModelState();
            }
        }
        return null;
    }
}
