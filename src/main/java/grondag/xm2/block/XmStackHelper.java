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

package grondag.xm2.block;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.Useful;
import grondag.xm2.api.model.MutableModelState;
import grondag.xm2.api.model.OwnedModelState;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.model.state.ModelStateTagHelper;
import grondag.xm2.model.state.ModelStates;
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

    public static void setStackModelState(ItemStack stack, MutableModelState modelState) {
        CompoundTag tag = stack.getOrCreateTag();
        if (modelState == null) {
            ModelStateTagHelper.clearNBTValues(tag);
            return;
        }
        modelState.serializeNBT(tag);
    }

    public static OwnedModelState getStackModelState(ItemStack stack) {
        OwnedModelState stackState = stack.hasTag() ? ModelStates.fromTag(stack.getTag())
                : null;

        // WAILA or other mods might create a stack with no NBT
        if (stackState != null)
            return stackState;

        if (stack.getItem() instanceof BlockItem) {
            BlockItem item = (BlockItem) stack.getItem();
            XmBlockStateImpl xmState = XmBlockStateAccess.get(item.getBlock().getDefaultState());
            if (xmState != null) {
                return xmState.defaultModelState.mutableCopy();
            }
        }
        return null;
    }
}
