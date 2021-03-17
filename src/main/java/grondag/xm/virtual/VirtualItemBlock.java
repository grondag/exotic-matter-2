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
package grondag.xm.virtual;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Internal
public class VirtualItemBlock { // extends XmBlockItem implements PlacementItem {

	//	@SuppressWarnings("hiding")
	//	public static final int FEATURE_FLAGS = PlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(PlacementItemFeature.FIXED_REGION,
	//			PlacementItemFeature.REGION_SIZE, PlacementItemFeature.REGION_ORIENTATION, PlacementItemFeature.SELECTION_RANGE, PlacementItemFeature.SPECIES_MODE,
	//			PlacementItemFeature.TARGET_MODE, PlacementItemFeature.BLOCK_ORIENTATION);
	//
	//	public VirtualItemBlock(Block block, Item.Settings settings) {
	//		super(block, settings.maxCount(1).maxDamage(0));
	//	}
	//
	//	@Override
	//	public int featureFlags(ItemStack stack) {
	//		return FEATURE_FLAGS;
	//	}
	//
	//	@Override
	//	public boolean isGuiSupported(ItemStack stack) {
	//		return true;
	//	}
	//
	//	@Override
	//	public void displayGui(PlayerEntity player) {
	//		if (!(PlacementItem.getHeldPlacementItem(player).getItem() == this))
	//			return;
	//		// TODO: reimplement GUI
	//		// player.openGui(Xm.LOG,
	//		// ModGuiHandler.ModGui.SUPERMODEL_ITEM.ordinal(), player.world, (int) player.x,
	//		// (int) player.y, (int) player.z);
	//	}
	//
	//	@Override
	//	public boolean isVirtual(ItemStack stack) {
	//		return true;
	//	}
	//
	//	@Override
	//	public FilterMode getFilterMode(ItemStack stack) {
	//		return FilterMode.FILL_REPLACEABLE;
	//	}
	//
	//	private static final Function<ModelState, BlockState> VFUNC = VirtualBlock::findAppropriateVirtualBlock;
	//	/**
	//	 * Gets the appropriate virtual block to place from a given item stack if it is
	//	 * a virtual item stack. Returns block state for AIR otherwise. May be different
	//	 * than the stack block because virtul in-world blocks are dependent rendering
	//	 * needs.
	//	 */
	//	@SuppressWarnings("unchecked")
	//	@Override
	//	public BlockState getPlacementBlockStateFromStack(ItemStack stack) {
	//		final Item item = stack.getItem();
	//		if (item instanceof VirtualItemBlock) {
	//			@SuppressWarnings("rawtypes")
	//			final MutableBaseModelState modelState = XmItem.modelState(stack);
	//			if (modelState == null)
	//				return null;
	//			else
	//				return  (BlockState) modelState.applyAndRelease(VFUNC);
	//		} else
	//			return Blocks.AIR.getDefaultState();
	//	}

	public static ItemStack getStack(World world, BlockState blockState, BlockPos pos) {
		// TODO Auto-generated method stub
		return null;
	}
}
