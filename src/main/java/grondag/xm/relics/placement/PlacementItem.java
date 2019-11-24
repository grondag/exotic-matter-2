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
package grondag.xm.relics.placement;

import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fermion.bits.EnumBitSet;
import grondag.fermion.position.PackedBlockPos;
import grondag.fermion.varia.NBTDictionary;
import grondag.fermion.varia.Useful;
import grondag.xm.api.connect.species.SpeciesMode;
import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.orientation.ClockwiseRotation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("rawtypes")
@API(status = Status.DEPRECATED)
@Deprecated
public interface PlacementItem {
	/////////////////////////////////////////////////////
	// STATIC MEMBERS
	/////////////////////////////////////////////////////

	String NBT_REGION_FLOATING_RANGE = NBTDictionary.claim("floatingRegionRange");
	String NBT_REGION_SIZE = NBTDictionary.claim("regionSize");
	String NBT_FIXED_REGION_ENABLED = NBTDictionary.claim("fixedRegionOn");
	String NBT_FIXED_REGION_SELECT_POS = NBTDictionary.claim("fixedRegionPos");

	EnumBitSet<PlacementItemFeature> BENUMSET_FEATURES = new EnumBitSet<>(PlacementItemFeature.class);

	/**
	 * Returns PlacementItem held by player in either hand, or null if player isn't
	 * holding one. If player is holding a PlacementItem in both hands, returns item
	 * in primary hand.
	 */

	static ItemStack getHeldPlacementItem(PlayerEntity player) {
		ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();

		if (stack.getItem() instanceof PlacementItem)
			return stack;

		stack = MinecraftClient.getInstance().player.getOffHandStack();

		if (stack.getItem() instanceof PlacementItem)
			return stack;

		return null;
	}

	static boolean isPlacementItem(ItemStack stack) {
		return stack.getItem() instanceof PlacementItem;
	}

	static PlacementItem getPlacementItem(ItemStack stack) {
		return isPlacementItem(stack) ? (PlacementItem) stack.getItem() : null;
	}

	/////////////////////////////////////////////////////
	// ABSTRACT MEMBERS
	/////////////////////////////////////////////////////

	/** True if item places air blocks or carves empty space in CSG blocks */
	boolean isExcavator(ItemStack placedStack);

	/** True if only places/affects virtual blocks. */
	boolean isVirtual(ItemStack stack);

	/////////////////////////////////////////////////////
	// DEFAULT MEMBERS
	/////////////////////////////////////////////////////

	/**
	 * Used with {@link #BENUMSET_FEATURES} to know what features are supported.
	 *
	 * @param stack
	 */
	default int featureFlags(ItemStack stack) {
		return 0xFFFFFFFF;
	}

	default boolean isGuiSupported(ItemStack stack) {
		return false;
	}

	default void displayGui(PlayerEntity player) {
		// noop
	}

	default boolean isBlockOrientationSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.BLOCK_ORIENTATION, featureFlags(stack));
	}

	default void setBlockOrientationAxis(ItemStack stack, BlockOrientationAxis orientation) {
		if (!isBlockOrientationSupported(stack))
			return;

		orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	/**
	 * Hides what type of shape we are using and just lets us know the axis. Returns
	 * UP/DOWN if not applicable.
	 */
	default Direction.Axis getBlockPlacementAxis(ItemStack stack) {
		if (!isBlockOrientationSupported(stack))
			return Direction.Axis.Y;

		final BaseModelState modelState = XmItem.modelState(stack);
		if (modelState == null)
			return Direction.Axis.Y;

		switch (modelState.orientationType()) {
		case AXIS:
			return getBlockOrientationAxis(stack).axis;

		case FACE:
			return getBlockOrientationFace(stack).face.getAxis();

		case EDGE:
			// TODO
		case CORNER:
			// TODO

		case NONE:
		default:
			return Direction.Axis.Y;
		}
	}

	/**
	 * Hides what type of shape we are using and just lets us know the axis. Returns
	 * false if not applicable.
	 */
	default boolean getBlockPlacementAxisIsInverted(ItemStack stack) {
		if (!isBlockOrientationSupported(stack))
			return false;

		switch (((BaseModelState)XmItem.modelState(stack)).orientationType()) {
		case AXIS:
			return false;

		case FACE:
			return getBlockOrientationFace(stack).face.getDirection() == Direction.AxisDirection.NEGATIVE;

		case EDGE:
			// FIXME: is this right?
			return getBlockOrientationEdge(stack).edge.face1.getDirection() == Direction.AxisDirection.POSITIVE;

		case CORNER:
			// TODO

		case NONE:
		default:
			return false;
		}
	}

	default ClockwiseRotation getBlockPlacementRotation(ItemStack stack) {
		if (!isBlockOrientationSupported(stack))
			return ClockwiseRotation.ROTATE_NONE;

		switch (((BaseModelState)XmItem.modelState(stack)).orientationType()) {
		case EDGE:
			return getBlockOrientationEdge(stack).edge.rotation;

		case CORNER:
			// TODO

		case NONE:
		case FACE:
		case AXIS:
		default:
			return ClockwiseRotation.ROTATE_NONE;
		}
	}

	/**
	 * Hides what type of shape we are using.
	 */
	default boolean isBlockOrientationDynamic(ItemStack stack) {
		if (!isBlockOrientationSupported(stack))
			return false;

		switch (((BaseModelState)XmItem.modelState(stack)).orientationType()) {
		case AXIS:
			return getBlockOrientationAxis(stack) == BlockOrientationAxis.DYNAMIC;

		case CORNER:
			return getBlockOrientationCorner(stack) == BlockOrientationCorner.DYNAMIC;

		case EDGE:
			return getBlockOrientationEdge(stack) == BlockOrientationEdge.DYNAMIC;

		case FACE:
			return getBlockOrientationFace(stack) == BlockOrientationFace.DYNAMIC;

		case NONE:
		default:
			return false;
		}
	}

	/**
	 * Hides what type of shape we are using.
	 */
	default boolean isBlockOrientationFixed(ItemStack stack) {
		if (!isBlockOrientationSupported(stack))
			return false;

		switch (((BaseModelState)XmItem.modelState(stack)).orientationType()) {
		case AXIS:
			return getBlockOrientationAxis(stack).isFixed();

		case CORNER:
			return getBlockOrientationCorner(stack).isFixed();

		case EDGE:
			return getBlockOrientationEdge(stack).isFixed();

		case FACE:
			return getBlockOrientationFace(stack).isFixed();

		case NONE:
		default:
			return false;
		}
	}

	/**
	 * Hides what type of shape we are using.
	 */
	default boolean isBlockOrientationMatchClosest(ItemStack stack) {
		if (!isBlockOrientationSupported(stack))
			return false;

		switch (((BaseModelState)XmItem.modelState(stack)).orientationType()) {
		case AXIS:
			return getBlockOrientationAxis(stack) == BlockOrientationAxis.MATCH_CLOSEST;

		case CORNER:
			return getBlockOrientationCorner(stack) == BlockOrientationCorner.MATCH_CLOSEST;

		case EDGE:
			return getBlockOrientationEdge(stack) == BlockOrientationEdge.MATCH_CLOSEST;

		case FACE:
			return getBlockOrientationFace(stack) == BlockOrientationFace.MATCH_CLOSEST;

		case NONE:
		default:
			return false;
		}
	}

	default BlockOrientationAxis getBlockOrientationAxis(ItemStack stack) {
		return BlockOrientationAxis.DYNAMIC.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleBlockOrientationAxis(ItemStack stack, boolean reverse) {
		if (!isBlockOrientationSupported(stack))
			return false;

		setBlockOrientationAxis(stack, reverse ? Useful.prevEnumValue(getBlockOrientationAxis(stack)) : Useful.nextEnumValue(getBlockOrientationAxis(stack)));
		return true;
	}

	default void setBlockOrientationFace(ItemStack stack, BlockOrientationFace orientation) {
		if (!isBlockOrientationSupported(stack))
			return;
		orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	default BlockOrientationFace getBlockOrientationFace(ItemStack stack) {
		return BlockOrientationFace.DYNAMIC.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleBlockOrientationFace(ItemStack stack, boolean reverse) {
		if (!isBlockOrientationSupported(stack))
			return false;

		setBlockOrientationFace(stack, reverse ? Useful.prevEnumValue(getBlockOrientationFace(stack)) : Useful.nextEnumValue(getBlockOrientationFace(stack)));
		return true;
	}

	default void setBlockOrientationEdge(ItemStack stack, BlockOrientationEdge orientation) {
		if (!isBlockOrientationSupported(stack))
			return;

		orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	default BlockOrientationEdge getBlockOrientationEdge(ItemStack stack) {
		return BlockOrientationEdge.DYNAMIC.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleBlockOrientationEdge(ItemStack stack, boolean reverse) {
		if (!isBlockOrientationSupported(stack))
			return false;

		setBlockOrientationEdge(stack, reverse ? Useful.prevEnumValue(getBlockOrientationEdge(stack)) : Useful.nextEnumValue(getBlockOrientationEdge(stack)));
		return true;
	}

	default void setBlockOrientationCorner(ItemStack stack, BlockOrientationCorner orientation) {
		if (!isBlockOrientationSupported(stack))
			return;

		orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	default BlockOrientationCorner getBlockOrientationCorner(ItemStack stack) {
		return BlockOrientationCorner.DYNAMIC.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleBlockOrientationCorner(ItemStack stack, boolean reverse) {
		if (!isBlockOrientationSupported(stack))
			return false;

		setBlockOrientationCorner(stack,
				reverse ? Useful.prevEnumValue(getBlockOrientationCorner(stack)) : Useful.nextEnumValue(getBlockOrientationCorner(stack)));
		return true;
	}

	/**
	 * Context-sensitive version - calls appropriate cycle method based on shape
	 * type. Return false if this item doesn't support this feature.
	 */
	default boolean cycleBlockOrientation(ItemStack stack, boolean reverse) {
		if (!isBlockOrientationSupported(stack))
			return false;

		switch (((BaseModelState)XmItem.modelState(stack)).orientationType()) {
		case AXIS:
			cycleBlockOrientationAxis(stack, reverse);
			break;

		case CORNER:
			cycleBlockOrientationCorner(stack, reverse);
			break;

		case EDGE:
			cycleBlockOrientationEdge(stack, reverse);
			break;

		case FACE:
			cycleBlockOrientationFace(stack, reverse);
			break;

		case NONE:
		default:
			break;
		}
		return true;
	}

	/**
	 * Context-sensitive localized name of current orientation.
	 */
	default String blockOrientationLocalizedName(ItemStack stack) {
		if (!isBlockOrientationSupported(stack))
			return "NOT SUPPORTED";

		switch (((BaseModelState)XmItem.modelState(stack)).orientationType()) {
		case AXIS:
			return getBlockOrientationAxis(stack).localizedName();

		case CORNER:
			return getBlockOrientationCorner(stack).localizedName();

		case EDGE:
			return getBlockOrientationEdge(stack).localizedName();

		case FACE:
			return getBlockOrientationFace(stack).localizedName();

		case NONE:
		default:
			return I18n.translate("placement.orientation.none");
		}
	}

	default boolean isRegionOrientationSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.REGION_ORIENTATION, featureFlags(stack));
	}

	default void setRegionOrientation(ItemStack stack, RegionOrientation orientation) {
		if (!isRegionOrientationSupported(stack))
			return;

		orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	/**
	 * Always returns XYZ during selection operations because display wouldn't match
	 * what user is doing otherwise.
	 */
	default RegionOrientation getRegionOrientation(ItemStack stack) {
		return isFixedRegionSelectionInProgress(stack) ? RegionOrientation.XYZ : RegionOrientation.XYZ.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleRegionOrientation(ItemStack stack, boolean reverse) {
		if (!isRegionOrientationSupported(stack))
			return false;

		setRegionOrientation(stack, reverse ? Useful.prevEnumValue(getRegionOrientation(stack)) : Useful.nextEnumValue(getRegionOrientation(stack)));
		return true;
	}

	default boolean isTargetModeSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.TARGET_MODE, featureFlags(stack));
	}

	default void setTargetMode(ItemStack stack, TargetMode mode) {
		if (!isTargetModeSupported(stack))
			return;

		mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	default TargetMode getTargetMode(ItemStack stack) {
		return TargetMode.FILL_REGION.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleTargetMode(ItemStack stack, boolean reverse) {
		if (!isTargetModeSupported(stack))
			return false;

		setTargetMode(stack, reverse ? Useful.prevEnumValue(getTargetMode(stack)) : Useful.nextEnumValue(getTargetMode(stack)));
		return true;
	}

	default boolean isFilterModeSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.FILTER_MODE, featureFlags(stack));
	}

	default void setFilterMode(ItemStack stack, FilterMode mode) {
		if (!isFilterModeSupported(stack))
			return;

		mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	default FilterMode getFilterMode(ItemStack stack) {
		return FilterMode.FILL_REPLACEABLE.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleFilterMode(ItemStack stack, boolean reverse) {
		if (!isFilterModeSupported(stack))
			return false;

		setFilterMode(stack, reverse ? Useful.prevEnumValue(getFilterMode(stack)) : Useful.nextEnumValue(getFilterMode(stack)));
		return true;
	}

	default boolean isSpeciesModeSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.SPECIES_MODE, featureFlags(stack));
	}

	default void setSpeciesMode(ItemStack stack, SpeciesMode mode) {
		if (!isSpeciesModeSupported(stack))
			return;

		mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
	}

	default SpeciesMode getSpeciesMode(ItemStack stack) {
		return SpeciesMode.MATCH_CLICKED.deserializeNBT(stack.getTag());
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleSpeciesMode(ItemStack stack, boolean reverse) {
		if (!isSpeciesModeSupported(stack))
			return false;

		setSpeciesMode(stack, reverse ? Useful.prevEnumValue(getSpeciesMode(stack)) : Useful.nextEnumValue(getSpeciesMode(stack)));
		return true;
	}

	/**
	 * Gets the appropriate super block to place from a given item stack if it is a
	 * SuperItemBlock stack. Otherwise tries to get a regular block state. May be
	 * different than the stack block because SuperModel in-world blocks are
	 * dependent on substance and other properties stored in the stack.
	 */
	static BlockState getPlacementBlockStateFromStackStatically(ItemStack stack) {
		// supermodel blocks may need to use a different block instance depending on
		// model/substance
		// handle this here by substituting a stack different than what we received
		final Item item = stack.getItem();

		if (item instanceof PlacementItem) {
			final BaseModelState modelState = XmItem.modelState(stack);
			if (modelState == null)
				return null;

			final Block targetBlock = ((BlockItem) stack.getItem()).getBlock();

			// TODO: remove when confirmed no longer be needed
			//            if (!sBlock.isVirtual() && targetBlock instanceof SuperBlock) {
			//                BlockSubstance substance = SuperBlockStackHelper.getStackSubstance(stack);
			//                if (substance == null)
			//                    return null;
			//                targetBlock = SuperModelBlock.findAppropriateSuperModelBlock(substance, modelState);
			//            }

			// TODO: may need to handle other properties/make dynamic somehow
			final BlockState result = targetBlock.getDefaultState();
			if (modelState.hasSpecies()) {
				//                result = result.with(XmSimpleBlock.SPECIES, modelState.species());
			}

			return result;
		} else if (item instanceof BlockItem) {
			final Block targetBlock = ((BlockItem) stack.getItem()).getBlock();
			return targetBlock.getDefaultState();
		} else
			return Blocks.AIR.getDefaultState();

	}

	default BlockState getPlacementBlockStateFromStack(ItemStack stack) {
		return getPlacementBlockStateFromStackStatically(stack);
	}

	//    public default void toggleDeleteMode(ItemStack stack)
	//    {
	//        setDeleteModeEnabled(stack, !isDeleteModeEnabled(stack));
	//    }
	//
	//    public default boolean isDeleteModeEnabled(ItemStack stack)
	//    {
	//        return Useful.getOrCreateTagCompound(stack).getBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED);
	//    }
	//
	//    public default void setDeleteModeEnabled(ItemStack stack, boolean enabled)
	//    {
	//        Useful.getOrCreateTagCompound(stack).setBoolean(ModNBTTag.PLACEMENT_DELETE_ENABLED, enabled);
	//    }

	default boolean isFixedRegionSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.FIXED_REGION, featureFlags(stack));
	}

	/**
	 * Return false if this item doesn't support this feature. Turning off cancels
	 * any region selection in progress.
	 */
	default boolean toggleFixedRegionEnabled(ItemStack stack) {
		if (!isFixedRegionSupported(stack))
			return false;

		final boolean current = isFixedRegionEnabled(stack);

		if (current && isFixedRegionSelectionInProgress(stack)) {
			fixedRegionCancel(stack);
		}

		setFixedRegionEnabled(stack, !current);
		return true;
	}

	/**
	 * If true, any region target method should use the fixed endpoints from
	 * {@link #fixedRegionFinish(ItemStack, EntityPlayer, BlockPos, boolean)}
	 *
	 * @param stack
	 * @return
	 */
	default boolean isFixedRegionEnabled(ItemStack stack) {
		if (!isFixedRegionSupported(stack))
			return false;

		return Useful.getOrCreateTagCompound(stack).getBoolean(NBT_FIXED_REGION_ENABLED);
	}

	default void setFixedRegionEnabled(ItemStack stack, boolean isFixedRegion) {
		if (!isFixedRegionSupported(stack))
			return;

		Useful.getOrCreateTagCompound(stack).putBoolean(NBT_FIXED_REGION_ENABLED, isFixedRegion);
	}

	default FixedRegionBounds getFixedRegion(ItemStack stack) {
		return new FixedRegionBounds(Useful.getOrCreateTagCompound(stack));
	}

	/**
	 * Sets fixed region in the stack. Does not enable fixed region.
	 */
	default void setFixedRegion(FixedRegionBounds bounds, ItemStack stack) {
		if (!isFixedRegionSupported(stack))
			return;

		bounds.saveToNBT(Useful.getOrCreateTagCompound(stack));
	}

	/**
	 * Sets the begining point for a fixed region. Enables fixed region. Does not
	 * change the current fixed region.
	 */
	default void fixedRegionStart(ItemStack stack, BlockPos pos, boolean isCenter) {
		if (!isFixedRegionSupported(stack))
			return;

		if (!isFixedRegionEnabled(stack)) {
			setFixedRegionEnabled(stack, true);
		}

		final CompoundTag tag = Useful.getOrCreateTagCompound(stack);

		tag.putLong(NBT_FIXED_REGION_SELECT_POS, PackedBlockPos.pack(pos, isCenter ? 1 : 0));

		final TargetMode currentMode = getTargetMode(stack);
		// assume user wants to fill a region and
		// change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
		if (!currentMode.usesSelectionRegion) {
			TargetMode.FILL_REGION.serializeNBT(tag);
		}
	}

	default boolean isFixedRegionSelectionInProgress(ItemStack stack) {
		if (!isFixedRegionSupported(stack) || !isFixedRegionEnabled(stack))
			return false;

		return Useful.getOrCreateTagCompound(stack).contains(NBT_FIXED_REGION_SELECT_POS);
	}

	default void fixedRegionCancel(ItemStack stack) {
		if (!isFixedRegionSupported(stack))
			return;

		final CompoundTag tag = Useful.getOrCreateTagCompound(stack);
		tag.remove(NBT_FIXED_REGION_SELECT_POS);

		// disable fixed region if we don't have one
		if (!FixedRegionBounds.isPresentInTag(tag)) {
			tag.putBoolean(NBT_FIXED_REGION_ENABLED, false);
		}
	}

	/**
	 * If fixed region selection in progress, returns the starting point that was
	 * set by {@link #fixedRegionStart(ItemStack, BlockPos, boolean)} Boolean valus
	 * is true if point is centered.
	 */

	default Pair<BlockPos, Boolean> fixedRegionSelectionPos(ItemStack stack) {
		if (!isFixedRegionSupported(stack))
			return null;

		final CompoundTag tag = Useful.getOrCreateTagCompound(stack);

		if (tag.contains(NBT_FIXED_REGION_SELECT_POS)) {
			final long packed = tag.getLong(NBT_FIXED_REGION_SELECT_POS);
			return Pair.of(PackedBlockPos.unpack(packed), PackedBlockPos.getExtra(packed) == 1);
		} else
			return null;
	}

	default void fixedRegionFinish(ItemStack stack, PlayerEntity player, BlockPos pos, boolean isCenter) {
		if (!isFixedRegionSupported(stack))
			return;

		final Pair<BlockPos, Boolean> fromPos = fixedRegionSelectionPos(stack);

		// if somehow missing start position, still want to cancel selection operation
		final CompoundTag tag = Useful.getOrCreateTagCompound(stack);

		if (fromPos == null)
			return;

		tag.remove(NBT_FIXED_REGION_SELECT_POS);

		setFixedRegion(new FixedRegionBounds(fromPos.getLeft(), fromPos.getRight(), pos, isCenter), stack);

		setFixedRegionEnabled(stack, true);

		final TargetMode currentMode = getTargetMode(stack);
		// assume user wants to fill a region and
		// change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
		if (!currentMode.usesSelectionRegion) {
			TargetMode.FILL_REGION.serializeNBT(tag);
		}

	}

	//    /**
	//     * See {@link #placementRegionPosition(ItemStack)} for some explanation.
	//     */
	//    public static BlockPos getPlayerRelativeRegionFromEndPoints(BlockPos from, BlockPos to, EntityPlayer player)
	//    {
	//        BlockPos diff = to.subtract(from);
	//        BlockPos selectedPos = new BlockPos(
	//                diff.getX() >= 0 ? diff.getX() + 1 : diff.getX() -1,
	//                diff.getY() >= 0 ? diff.getY() + 1 : diff.getY() -1,
	//                diff.getZ() >= 0 ? diff.getZ() + 1 : diff.getZ() -1);
	//        return(player.getHorizontalFacing().getAxis() == Direction.Axis.Z)
	//                ? selectedPos
	//                : new BlockPos(selectedPos.getZ(), selectedPos.getY(), selectedPos.getX());
	//    }

	default boolean isRegionSizeSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.REGION_SIZE, featureFlags(stack));
	}

	/**
	 * For cubic selection regions. X is left/right relative to player and Z is
	 * depth in direction player is facing.<br>
	 * Y is always vertical height.<br>
	 * All are always positive numbers.<br>
	 * Region rotation is or isn't applied according to parameter.<br>
	 */
	default BlockPos getRegionSize(ItemStack stack, boolean applyRegionRotation) {
		final CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(NBT_REGION_SIZE))
			return new BlockPos(1, 1, 1);

		final BlockPos result = BlockPos.fromLong(tag.getLong(NBT_REGION_SIZE));

		return applyRegionRotation ? getRegionOrientation(stack).rotatedRegionPos(result) : result;
	}

	/**
	 * See {@link #getRegionSize(ItemStack, boolean)}
	 */
	default void setRegionSize(ItemStack stack, BlockPos pos) {
		if (!isRegionSizeSupported(stack))
			return;

		final CompoundTag tag = stack.getTag();
		tag.putLong(NBT_REGION_SIZE, pos.asLong());
	}

	/**
	 * See {@link #getRegionSize(ItemStack, boolean)} Returns false if feature not
	 * supported.
	 */
	default boolean changeRegionSize(ItemStack stack, int dx, int dy, int dz) {
		if (!isRegionSizeSupported(stack))
			return false;

		final CompoundTag tag = Useful.getOrCreateTagCompound(stack);
		final BlockPos oldPos = BlockPos.fromLong(tag.getLong(NBT_REGION_SIZE));

		final BlockPos newPos = new BlockPos(MathHelper.clamp(oldPos.getX() + dx, 1, 9), MathHelper.clamp(oldPos.getY() + dy, 1, 9),
				MathHelper.clamp(oldPos.getZ() + dz, 1, isExcavator(stack) ? 64 : 9));
		tag.putLong(NBT_REGION_SIZE, newPos.asLong());

		if (newPos.getX() == 1 && newPos.getY() == 1 && newPos.getZ() == 1) {
			// change to single-block mode if region size is single block
			if (getTargetMode(stack).usesSelectionRegion && isTargetModeSupported(stack)) {
				setTargetMode(stack, TargetMode.ON_CLICKED_FACE);
			}
		} else {
			// change to multiblock mode if region size is single block
			if (!getTargetMode(stack).usesSelectionRegion && isTargetModeSupported(stack)) {
				setTargetMode(stack, TargetMode.FILL_REGION);
			}
		}

		return true;
	}

	//    /**
	//     * Meant for use by packet handler
	//     */
	//    public static void selectedRegionUpdate(ItemStack stack, BlockPos selectedPos)
	//    {
	//        CompoundTag tag = Useful.getOrCreateTagCompound(stack);
	//        if(selectedPos == null)
	//        {
	//            tag.removeTag(NBT_REGION_SIZE);
	//        }
	//        else
	//        {
	//            tag.setLong(NBT_REGION_SIZE, selectedPos.toLong());
	//        }
	//    }

	default String selectedRegionLocalizedName(ItemStack stack) {
		switch (getTargetMode(stack)) {
		case FILL_REGION:
			if (!isRegionSizeSupported(stack))
				return "";
			final BlockPos pos = getRegionSize(stack, false);
			return I18n.translate("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());

		case ON_CLICKED_SURFACE:
			return I18n.translate("placement.message.region_additive");

		case ON_CLICKED_FACE:
		default:
			return I18n.translate("placement.message.region_single");

		}
	}

	default boolean isSelectionRangeSupported(ItemStack stack) {
		return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.SELECTION_RANGE, featureFlags(stack));
	}

	/**
	 * 0 means non-floating
	 */
	default void setSelectionTargetRange(ItemStack stack, int range) {
		if (!isSelectionRangeSupported(stack))
			return;

		// subtract because external values jump from 0 to 2
		if (range > 0) {
			range--;
		}
		Useful.getOrCreateTagCompound(stack).putByte(NBT_REGION_FLOATING_RANGE, (byte) range);
	}

	/**
	 * 0 means non-floating
	 */
	default int getFloatingSelectionRange(ItemStack stack) {
		if (!isSelectionRangeSupported(stack))
			return 0;

		final CompoundTag tag = stack.getTag();
		final int range = tag == null ? 0 : MathHelper.clamp(tag.getByte(NBT_REGION_FLOATING_RANGE), 0, 4);
		return range == 0 ? 0 : range + 1;
	}

	/**
	 * Return false if this item doesn't support this feature.
	 */
	default boolean cycleSelectionTargetRange(ItemStack stack, boolean reverse) {
		if (!isSelectionRangeSupported(stack))
			return false;

		final CompoundTag tag = Useful.getOrCreateTagCompound(stack);
		int range = tag.getByte(NBT_REGION_FLOATING_RANGE) + (reverse ? -1 : 1);
		if (range > 4) {
			range = 0;
		}
		if (range < 0) {
			range = 4;
		}
		tag.putByte(NBT_REGION_FLOATING_RANGE, (byte) range);
		return true;
	}

	default boolean isFloatingSelectionEnabled(ItemStack stack) {
		if (!isSelectionRangeSupported(stack))
			return false;

		return getFloatingSelectionRange(stack) != 0;
	}

	/**
	 * Will return a meaningless result if floating selection is disabled.
	 *
	 * TODO: remove - replaced by PlacementPosition
	 */
	default BlockPos getFloatingSelectionBlockPos(ItemStack stack, LivingEntity entity) {
		final int range = getFloatingSelectionRange(stack);

		final Vec3d look = entity.getRotationVector();
		final Vec3d pos = entity.getPos();
		final int blockX = MathHelper.floor(look.x * range + pos.x);
		final int blockY = MathHelper.floor(look.y * range + pos.y + entity.getEyeHeight(entity.getPose()));
		final int blockZ = MathHelper.floor(look.z * range + pos.z);

		return new BlockPos(blockX, blockY, blockZ);
	}

}
