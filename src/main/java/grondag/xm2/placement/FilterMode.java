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

package grondag.xm2.placement;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import grondag.xm2.block.VirtualBlock;
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
	    return block.getMaterial(blockState).isReplaceable() && !VirtualBlock.isVirtualBlock(block);

	case REPLACE_ALL:
	    if (isVirtual) {
		return block.getMaterial(blockState).isReplaceable() || VirtualBlock.isVirtualBlock(block);
	    } else {
		return !VirtualBlock.isVirtualBlock(block);
	    }

	case REPLACE_ALL_EXCEPT:
	    // TODO
	    return true;

	case REPLACE_ONLY:
	    // TODO
	    return false;

	case REPLACE_SOLID:
	    // test for non-virtual relies on fact that all virtual blocks are replaceable
	    return isVirtual ? VirtualBlock.isVirtualBlock(block) : !block.getMaterial(blockState).isReplaceable();

	default:
	    return false;

	}
    }
}
