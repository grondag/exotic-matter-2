/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.virtual;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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

	public static ItemStack getStack(Level world, BlockState blockState, BlockPos pos) {
		// TODO Auto-generated method stub
		return null;
	}
}
