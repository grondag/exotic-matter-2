/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm.block;

import grondag.fermion.varia.NBTDictionary;
import grondag.fermion.varia.Useful;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.primitive.ModelPrimitiveRegistry;
import grondag.xm.model.state.ModelStateTagHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Item stack serialization handlers
 */
public class XmStackHelper {
    public static String NBT_SUPERMODEL_LIGHT_VALUE = NBTDictionary.claim("smLight");

    public static void setStackLightValue(ItemStack stack, int lightValue) {
        // important that the tag used here matches that used in tile entity
        Useful.getOrCreateTagCompound(stack).putByte(XmStackHelper.NBT_SUPERMODEL_LIGHT_VALUE, (byte) lightValue);
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
            ModelStateTagHelper.clearNBTValues(tag);
            return;
        }
        modelState.serializeNBT(tag);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ModelState.Mutable> T getStackModelState(ItemStack stack) {
        ModelState.Mutable stackState = stack.hasTag() ? ModelPrimitiveRegistry.INSTANCE.fromTag(stack.getTag()) : null;

        // WAILA or other mods might create a stack with no NBT
        if (stackState != null)
            return (T) stackState;

        if (stack.getItem() instanceof BlockItem) {
            BlockItem item = (BlockItem) stack.getItem();
            XmBlockState xmState = XmBlockState.get(item);
            if (xmState != null) {
                return (T) xmState.defaultModelState();
            }
        }
        return null;
    }
}
