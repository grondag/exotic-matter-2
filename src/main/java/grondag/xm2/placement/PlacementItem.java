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

import org.apache.commons.lang3.tuple.Pair;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.structures.BinaryEnumSet;
import grondag.fermion.varia.FixedRegionBounds;
import grondag.fermion.varia.Useful;
import grondag.fermion.world.PackedBlockPos;
import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.model.MutableModelPrimitiveState;
import grondag.xm2.api.model.MutableModelState;
import grondag.xm2.block.XmSimpleBlock;
import grondag.xm2.block.XmStackHelper;
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

public interface PlacementItem {
    /////////////////////////////////////////////////////
    // STATIC MEMBERS
    /////////////////////////////////////////////////////

    public final static String NBT_REGION_FLOATING_RANGE = NBTDictionary.claim("floatingRegionRange");
    public final static String NBT_REGION_SIZE = NBTDictionary.claim("regionSize");
    public final static String NBT_FIXED_REGION_ENABLED = NBTDictionary.claim("fixedRegionOn");
    public final static String NBT_FIXED_REGION_SELECT_POS = NBTDictionary.claim("fixedRegionPos");

    public static BinaryEnumSet<PlacementItemFeature> BENUMSET_FEATURES = new BinaryEnumSet<PlacementItemFeature>(
            PlacementItemFeature.class);

    /**
     * Returns PlacementItem held by player in either hand, or null if player isn't
     * holding one. If player is holding a PlacementItem in both hands, returns item
     * in primary hand.
     */

    public static ItemStack getHeldPlacementItem(PlayerEntity player) {
        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();

        if (stack.getItem() instanceof PlacementItem)
            return stack;

        stack = MinecraftClient.getInstance().player.getOffHandStack();

        if (stack.getItem() instanceof PlacementItem)
            return stack;

        return null;
    }

    public static boolean isPlacementItem(ItemStack stack) {
        return stack.getItem() instanceof PlacementItem;
    }

    public static PlacementItem getPlacementItem(ItemStack stack) {
        return isPlacementItem(stack) ? (PlacementItem) stack.getItem() : null;
    }

    /////////////////////////////////////////////////////
    // ABSTRACT MEMBERS
    /////////////////////////////////////////////////////

    /** True if item places air blocks or carves empty space in CSG blocks */
    public boolean isExcavator(ItemStack placedStack);

    /** True if only places/affects virtual blocks. */
    public boolean isVirtual(ItemStack stack);

    /////////////////////////////////////////////////////
    // DEFAULT MEMBERS
    /////////////////////////////////////////////////////

    /**
     * Used with {@link #BENUMSET_FEATURES} to know what features are supported.
     * 
     * @param stack
     */
    public default int featureFlags(ItemStack stack) {
        return 0xFFFFFFFF;
    }

    public default boolean isGuiSupported(ItemStack stack) {
        return false;
    }

    public default void displayGui(PlayerEntity player) {
        // noop
    }

    public default boolean isBlockOrientationSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.BLOCK_ORIENTATION, this.featureFlags(stack));
    }

    public default void setBlockOrientationAxis(ItemStack stack, BlockOrientationAxis orientation) {
        if (!isBlockOrientationSupported(stack))
            return;

        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    /**
     * Hides what type of shape we are using and just lets us know the axis. Returns
     * UP/DOWN if not applicable.
     */
    public default Direction.Axis getBlockPlacementAxis(ItemStack stack) {
        if (!isBlockOrientationSupported(stack))
            return Direction.Axis.Y;

        MutableModelPrimitiveState modelState = XmStackHelper.getStackModelState(stack);
        if (modelState == null)
            return Direction.Axis.Y;

        switch (modelState.orientationType()) {
            case AXIS:
                return this.getBlockOrientationAxis(stack).axis;

            case FACE:
                return this.getBlockOrientationFace(stack).face.getAxis();

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
    public default boolean getBlockPlacementAxisIsInverted(ItemStack stack) {
        if (!isBlockOrientationSupported(stack))
            return false;

        switch (XmStackHelper.getStackModelState(stack).orientationType()) {
            case AXIS:
                return false;

            case FACE:
                return this.getBlockOrientationFace(stack).face.getDirection() == Direction.AxisDirection.NEGATIVE;

            case EDGE:
                // FIXME: is this right?
                return this.getBlockOrientationEdge(stack).edge.face1
                        .getDirection() == Direction.AxisDirection.POSITIVE;

            case CORNER:
                // TODO

            case NONE:
            default:
                return false;
        }
    }

    public default ClockwiseRotation getBlockPlacementRotation(ItemStack stack) {
        if (!isBlockOrientationSupported(stack))
            return ClockwiseRotation.ROTATE_NONE;

        switch (XmStackHelper.getStackModelState(stack).orientationType()) {
            case EDGE:
                return this.getBlockOrientationEdge(stack).edge.rotation;

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
    public default boolean isBlockOrientationDynamic(ItemStack stack) {
        if (!isBlockOrientationSupported(stack))
            return false;

        switch (XmStackHelper.getStackModelState(stack).orientationType()) {
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
    public default boolean isBlockOrientationFixed(ItemStack stack) {
        if (!isBlockOrientationSupported(stack))
            return false;

        switch (XmStackHelper.getStackModelState(stack).orientationType()) {
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
    public default boolean isBlockOrientationMatchClosest(ItemStack stack) {
        if (!isBlockOrientationSupported(stack))
            return false;

        switch (XmStackHelper.getStackModelState(stack).orientationType()) {
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

    public default BlockOrientationAxis getBlockOrientationAxis(ItemStack stack) {
        return BlockOrientationAxis.DYNAMIC.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationAxis(ItemStack stack, boolean reverse) {
        if (!isBlockOrientationSupported(stack))
            return false;

        setBlockOrientationAxis(stack, reverse ? Useful.prevEnumValue(getBlockOrientationAxis(stack))
                : Useful.nextEnumValue(getBlockOrientationAxis(stack)));
        return true;
    }

    public default void setBlockOrientationFace(ItemStack stack, BlockOrientationFace orientation) {
        if (!isBlockOrientationSupported(stack))
            return;
        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default BlockOrientationFace getBlockOrientationFace(ItemStack stack) {
        return BlockOrientationFace.DYNAMIC.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationFace(ItemStack stack, boolean reverse) {
        if (!isBlockOrientationSupported(stack))
            return false;

        setBlockOrientationFace(stack, reverse ? Useful.prevEnumValue(getBlockOrientationFace(stack))
                : Useful.nextEnumValue(getBlockOrientationFace(stack)));
        return true;
    }

    public default void setBlockOrientationEdge(ItemStack stack, BlockOrientationEdge orientation) {
        if (!isBlockOrientationSupported(stack))
            return;

        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default BlockOrientationEdge getBlockOrientationEdge(ItemStack stack) {
        return BlockOrientationEdge.DYNAMIC.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationEdge(ItemStack stack, boolean reverse) {
        if (!isBlockOrientationSupported(stack))
            return false;

        setBlockOrientationEdge(stack, reverse ? Useful.prevEnumValue(getBlockOrientationEdge(stack))
                : Useful.nextEnumValue(getBlockOrientationEdge(stack)));
        return true;
    }

    public default void setBlockOrientationCorner(ItemStack stack, BlockOrientationCorner orientation) {
        if (!isBlockOrientationSupported(stack))
            return;

        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default BlockOrientationCorner getBlockOrientationCorner(ItemStack stack) {
        return BlockOrientationCorner.DYNAMIC.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientationCorner(ItemStack stack, boolean reverse) {
        if (!isBlockOrientationSupported(stack))
            return false;

        setBlockOrientationCorner(stack, reverse ? Useful.prevEnumValue(getBlockOrientationCorner(stack))
                : Useful.nextEnumValue(getBlockOrientationCorner(stack)));
        return true;
    }

    /**
     * Context-sensitive version - calls appropriate cycle method based on shape
     * type. Return false if this item doesn't support this feature.
     */
    public default boolean cycleBlockOrientation(ItemStack stack, boolean reverse) {
        if (!isBlockOrientationSupported(stack))
            return false;

        switch (XmStackHelper.getStackModelState(stack).orientationType()) {
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
    public default String blockOrientationLocalizedName(ItemStack stack) {
        if (!isBlockOrientationSupported(stack))
            return "NOT SUPPORTED";

        switch (XmStackHelper.getStackModelState(stack).orientationType()) {
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

    public default boolean isRegionOrientationSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.REGION_ORIENTATION, this.featureFlags(stack));
    }

    public default void setRegionOrientation(ItemStack stack, RegionOrientation orientation) {
        if (!isRegionOrientationSupported(stack))
            return;

        orientation.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    /**
     * Always returns XYZ during selection operations because display wouldn't match
     * what user is doing otherwise.
     */
    public default RegionOrientation getRegionOrientation(ItemStack stack) {
        return isFixedRegionSelectionInProgress(stack) ? RegionOrientation.XYZ
                : RegionOrientation.XYZ.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleRegionOrientation(ItemStack stack, boolean reverse) {
        if (!isRegionOrientationSupported(stack))
            return false;

        setRegionOrientation(stack, reverse ? Useful.prevEnumValue(getRegionOrientation(stack))
                : Useful.nextEnumValue(getRegionOrientation(stack)));
        return true;
    }

    public default boolean isTargetModeSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.TARGET_MODE, this.featureFlags(stack));
    }

    public default void setTargetMode(ItemStack stack, TargetMode mode) {
        if (!this.isTargetModeSupported(stack))
            return;

        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default TargetMode getTargetMode(ItemStack stack) {
        return TargetMode.FILL_REGION.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleTargetMode(ItemStack stack, boolean reverse) {
        if (!this.isTargetModeSupported(stack))
            return false;

        setTargetMode(stack,
                reverse ? Useful.prevEnumValue(getTargetMode(stack)) : Useful.nextEnumValue(getTargetMode(stack)));
        return true;
    }

    public default boolean isFilterModeSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.FILTER_MODE, this.featureFlags(stack));
    }

    public default void setFilterMode(ItemStack stack, FilterMode mode) {
        if (!this.isFilterModeSupported(stack))
            return;

        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default FilterMode getFilterMode(ItemStack stack) {
        return FilterMode.FILL_REPLACEABLE.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleFilterMode(ItemStack stack, boolean reverse) {
        if (!this.isFilterModeSupported(stack))
            return false;

        setFilterMode(stack,
                reverse ? Useful.prevEnumValue(getFilterMode(stack)) : Useful.nextEnumValue(getFilterMode(stack)));
        return true;
    }

    public default boolean isSpeciesModeSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.SPECIES_MODE, this.featureFlags(stack));
    }

    public default void setSpeciesMode(ItemStack stack, SpeciesMode mode) {
        if (!this.isSpeciesModeSupported(stack))
            return;

        mode.serializeNBT(Useful.getOrCreateTagCompound(stack));
    }

    public default SpeciesMode getSpeciesMode(ItemStack stack) {
        return SpeciesMode.MATCH_CLICKED.deserializeNBT(stack.getTag());
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleSpeciesMode(ItemStack stack, boolean reverse) {
        if (!this.isSpeciesModeSupported(stack))
            return false;

        setSpeciesMode(stack,
                reverse ? Useful.prevEnumValue(getSpeciesMode(stack)) : Useful.nextEnumValue(getSpeciesMode(stack)));
        return true;
    }

    /**
     * Gets the appropriate super block to place from a given item stack if it is a
     * SuperItemBlock stack. Otherwise tries to get a regular block state. May be
     * different than the stack block because SuperModel in-world blocks are
     * dependent on substance and other properties stored in the stack.
     */
    public static BlockState getPlacementBlockStateFromStackStatically(ItemStack stack) {
        // supermodel blocks may need to use a different block instance depending on
        // model/substance
        // handle this here by substituting a stack different than what we received
        Item item = stack.getItem();

        if (item instanceof PlacementItem) {
            MutableModelState modelState = XmStackHelper.getStackModelState(stack);
            if (modelState == null)
                return null;

            Block targetBlock = ((BlockItem) stack.getItem()).getBlock();

            // TODO: remove when confirmed no longer be needed
//            if (!sBlock.isVirtual() && targetBlock instanceof SuperBlock) {
//                BlockSubstance substance = SuperBlockStackHelper.getStackSubstance(stack);
//                if (substance == null)
//                    return null;
//                targetBlock = SuperModelBlock.findAppropriateSuperModelBlock(substance, modelState);
//            }

            // TODO: may need to handle other properties/make dynamic somehow
            BlockState result = targetBlock.getDefaultState();
            if (modelState.hasSpecies()) {
                result = result.with(XmSimpleBlock.SPECIES, modelState.species());
            }

            return result;
        } else if (item instanceof BlockItem) {
            Block targetBlock = (Block) ((BlockItem) stack.getItem()).getBlock();
            return targetBlock.getDefaultState();
        } else {
            return Blocks.AIR.getDefaultState();
        }

    }

    public default BlockState getPlacementBlockStateFromStack(ItemStack stack) {
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

    public default boolean isFixedRegionSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.FIXED_REGION, this.featureFlags(stack));
    }

    /**
     * Return false if this item doesn't support this feature. Turning off cancels
     * any region selection in progress.
     */
    public default boolean toggleFixedRegionEnabled(ItemStack stack) {
        if (!this.isFixedRegionSupported(stack))
            return false;

        boolean current = this.isFixedRegionEnabled(stack);

        if (current
                && this.isFixedRegionSelectionInProgress(stack))
            this.fixedRegionCancel(stack);

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
    public default boolean isFixedRegionEnabled(ItemStack stack) {
        if (!this.isFixedRegionSupported(stack))
            return false;

        return Useful.getOrCreateTagCompound(stack).getBoolean(NBT_FIXED_REGION_ENABLED);
    }

    public default void setFixedRegionEnabled(ItemStack stack, boolean isFixedRegion) {
        if (!this.isFixedRegionSupported(stack))
            return;

        Useful.getOrCreateTagCompound(stack).putBoolean(NBT_FIXED_REGION_ENABLED, isFixedRegion);
    }

    public default FixedRegionBounds getFixedRegion(ItemStack stack) {
        return new FixedRegionBounds(Useful.getOrCreateTagCompound(stack));
    }

    /**
     * Sets fixed region in the stack. Does not enable fixed region.
     */
    public default void setFixedRegion(FixedRegionBounds bounds, ItemStack stack) {
        if (!this.isFixedRegionSupported(stack))
            return;

        bounds.saveToNBT(Useful.getOrCreateTagCompound(stack));
    }

    /**
     * Sets the begining point for a fixed region. Enables fixed region. Does not
     * change the current fixed region.
     */
    public default void fixedRegionStart(ItemStack stack, BlockPos pos, boolean isCenter) {
        if (!this.isFixedRegionSupported(stack))
            return;

        if (!this.isFixedRegionEnabled(stack))
            this.setFixedRegionEnabled(stack, true);

        CompoundTag tag = Useful.getOrCreateTagCompound(stack);

        tag.putLong(NBT_FIXED_REGION_SELECT_POS, PackedBlockPos.pack(pos, isCenter ? 1 : 0));

        TargetMode currentMode = getTargetMode(stack);
        // assume user wants to fill a region and
        // change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
        if (!currentMode.usesSelectionRegion)
            TargetMode.FILL_REGION.serializeNBT(tag);
    }

    public default boolean isFixedRegionSelectionInProgress(ItemStack stack) {
        if (!this.isFixedRegionSupported(stack)
                || !this.isFixedRegionEnabled(stack))
            return false;

        return Useful.getOrCreateTagCompound(stack).containsKey(NBT_FIXED_REGION_SELECT_POS);
    }

    public default void fixedRegionCancel(ItemStack stack) {
        if (!this.isFixedRegionSupported(stack))
            return;

        CompoundTag tag = Useful.getOrCreateTagCompound(stack);
        tag.remove(NBT_FIXED_REGION_SELECT_POS);

        // disable fixed region if we don't have one
        if (!FixedRegionBounds.isPresentInTag(tag))
            tag.putBoolean(NBT_FIXED_REGION_ENABLED, false);
    }

    /**
     * If fixed region selection in progress, returns the starting point that was
     * set by {@link #fixedRegionStart(ItemStack, BlockPos, boolean)} Boolean valus
     * is true if point is centered.
     */

    public default Pair<BlockPos, Boolean> fixedRegionSelectionPos(ItemStack stack) {
        if (!this.isFixedRegionSupported(stack))
            return null;

        CompoundTag tag = Useful.getOrCreateTagCompound(stack);

        if (tag.containsKey(NBT_FIXED_REGION_SELECT_POS)) {
            long packed = tag.getLong(NBT_FIXED_REGION_SELECT_POS);
            return Pair.of(PackedBlockPos.unpack(packed), PackedBlockPos.getExtra(packed) == 1);
        } else {
            return null;
        }
    }

    public default void fixedRegionFinish(ItemStack stack, PlayerEntity player, BlockPos pos, boolean isCenter) {
        if (!this.isFixedRegionSupported(stack))
            return;

        Pair<BlockPos, Boolean> fromPos = fixedRegionSelectionPos(stack);

        // if somehow missing start position, still want to cancel selection operation
        CompoundTag tag = Useful.getOrCreateTagCompound(stack);

        if (fromPos == null)
            return;

        tag.remove(NBT_FIXED_REGION_SELECT_POS);

        setFixedRegion(new FixedRegionBounds(fromPos.getLeft(), fromPos.getRight(), pos, isCenter), stack);

        setFixedRegionEnabled(stack, true);

        TargetMode currentMode = getTargetMode(stack);
        // assume user wants to fill a region and
        // change mode to region fill if not already set to FILL_REGION or HOLLOW_FILL
        if (!currentMode.usesSelectionRegion)
            TargetMode.FILL_REGION.serializeNBT(tag);

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

    public default boolean isRegionSizeSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.REGION_SIZE, this.featureFlags(stack));
    }

    /**
     * For cubic selection regions. X is left/right relative to player and Z is
     * depth in direction player is facing.<br>
     * Y is always vertical height.<br>
     * All are always positive numbers.<br>
     * Region rotation is or isn't applied according to parameter.<br>
     */
    public default BlockPos getRegionSize(ItemStack stack, boolean applyRegionRotation) {
        CompoundTag tag = stack.getTag();
        if (tag == null
                || !tag.containsKey(NBT_REGION_SIZE))
            return new BlockPos(1, 1, 1);

        BlockPos result = BlockPos.fromLong(tag.getLong(NBT_REGION_SIZE));

        return applyRegionRotation ? getRegionOrientation(stack).rotatedRegionPos(result) : result;
    }

    /**
     * See {@link #getRegionSize(ItemStack, boolean)}
     */
    public default void setRegionSize(ItemStack stack, BlockPos pos) {
        if (!this.isRegionSizeSupported(stack))
            return;

        CompoundTag tag = stack.getTag();
        tag.putLong(NBT_REGION_SIZE, pos.asLong());
    }

    /**
     * See {@link #getRegionSize(ItemStack, boolean)} Returns false if feature not
     * supported.
     */
    public default boolean changeRegionSize(ItemStack stack, int dx, int dy, int dz) {
        if (!this.isRegionSizeSupported(stack))
            return false;

        CompoundTag tag = Useful.getOrCreateTagCompound(stack);
        BlockPos oldPos = BlockPos.fromLong(tag.getLong(NBT_REGION_SIZE));

        BlockPos newPos = new BlockPos(MathHelper.clamp(oldPos.getX() + dx, 1, 9),
                MathHelper.clamp(oldPos.getY() + dy, 1, 9),
                MathHelper.clamp(oldPos.getZ() + dz, 1, this.isExcavator(stack) ? 64 : 9));
        tag.putLong(NBT_REGION_SIZE, newPos.asLong());

        if (newPos.getX() == 1
                && newPos.getY() == 1
                && newPos.getZ() == 1) {
            // change to single-block mode if region size is single block
            if (this.getTargetMode(stack).usesSelectionRegion
                    && this.isTargetModeSupported(stack)) {
                this.setTargetMode(stack, TargetMode.ON_CLICKED_FACE);
            }
        } else {
            // change to multiblock mode if region size is single block
            if (!this.getTargetMode(stack).usesSelectionRegion
                    && this.isTargetModeSupported(stack)) {
                this.setTargetMode(stack, TargetMode.FILL_REGION);
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

    public default String selectedRegionLocalizedName(ItemStack stack) {
        switch (getTargetMode(stack)) {
            case FILL_REGION:
                if (!this.isRegionSizeSupported(stack))
                    return "";
                BlockPos pos = getRegionSize(stack, false);
                return I18n.translate("placement.message.region_box", pos.getX(), pos.getY(), pos.getZ());

            case ON_CLICKED_SURFACE:
                return I18n.translate("placement.message.region_additive");

            case ON_CLICKED_FACE:
            default:
                return I18n.translate("placement.message.region_single");

        }
    }

    public default boolean isSelectionRangeSupported(ItemStack stack) {
        return BENUMSET_FEATURES.isFlagSetForValue(PlacementItemFeature.SELECTION_RANGE, this.featureFlags(stack));
    }

    /**
     * 0 means non-floating
     */
    public default void setSelectionTargetRange(ItemStack stack, int range) {
        if (!this.isSelectionRangeSupported(stack))
            return;

        // subtract because external values jump from 0 to 2
        if (range > 0)
            range--;
        Useful.getOrCreateTagCompound(stack).putByte(NBT_REGION_FLOATING_RANGE, (byte) range);
    }

    /**
     * 0 means non-floating
     */
    public default int getFloatingSelectionRange(ItemStack stack) {
        if (!this.isSelectionRangeSupported(stack))
            return 0;

        CompoundTag tag = stack.getTag();
        int range = tag == null ? 0 : MathHelper.clamp(tag.getByte(NBT_REGION_FLOATING_RANGE), 0, 4);
        return range == 0 ? 0 : range + 1;
    }

    /**
     * Return false if this item doesn't support this feature.
     */
    public default boolean cycleSelectionTargetRange(ItemStack stack, boolean reverse) {
        if (!this.isSelectionRangeSupported(stack))
            return false;

        CompoundTag tag = Useful.getOrCreateTagCompound(stack);
        int range = tag.getByte(NBT_REGION_FLOATING_RANGE) + (reverse ? -1 : 1);
        if (range > 4)
            range = 0;
        if (range < 0)
            range = 4;
        tag.putByte(NBT_REGION_FLOATING_RANGE, (byte) range);
        return true;
    }

    public default boolean isFloatingSelectionEnabled(ItemStack stack) {
        if (!this.isSelectionRangeSupported(stack))
            return false;

        return getFloatingSelectionRange(stack) != 0;
    }

    /**
     * Will return a meaningless result if floating selection is disabled.
     * 
     * TODO: remove - replaced by PlacementPosition
     */
    public default BlockPos getFloatingSelectionBlockPos(ItemStack stack, LivingEntity entity) {
        int range = getFloatingSelectionRange(stack);

        Vec3d look = entity.getRotationVector();
        Vec3d pos = entity.getPos();
        int blockX = MathHelper.floor(look.x * range + pos.x);
        int blockY = MathHelper.floor(look.y * range + pos.y + entity.getEyeHeight(entity.getPose()));
        int blockZ = MathHelper.floor(look.z * range + pos.z);

        return new BlockPos(blockX, blockY, blockZ);
    }

}
