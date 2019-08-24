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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fermion.modkeys.api.ModKeys;
import grondag.fermion.varia.Useful;
import grondag.xm.XmConfig;
import grondag.xm.api.block.SpeciesHelper;
import grondag.xm.api.block.SpeciesMode;
import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.orientation.HorizontalFace;
import grondag.xm.relics.XmStackHelper;
import grondag.xm.virtual.VirtualBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@API(status = Status.DEPRECATED)
@Deprecated
@Environment(EnvType.CLIENT)
public abstract class PlacementHandler {
    private PlacementHandler() {
    }

    /**
     * Called client-side by overlay renderer to know what should be rendered for
     * player. If no operation is in progress, assumes player will click the right
     * mouse button. If an operation is in progress, assumes user will click the
     * button that completes the operation.
     */
    public static PlacementResult predictPlacementResults(ClientPlayerEntity player, ItemStack stack, PlacementItem item) {

        /*
         * if player is in range to a solid block and floating selection is off, the
         * block against which we would place our block(s). Null if out of range or
         * floating selection is on.
         */
        BlockPos onPos = null;

        /**
         * Face that will be clicked. Null if onPos is null
         */
        Direction onFace = null;

        /**
         * Hit vector for block that will be clicked. Null if onPos is null
         */
        Vec3d hitVec = null;

        if (!item.isFloatingSelectionEnabled(stack)) {
            // if floating selection enabled, there is no "placed on" position
            // No floating selection - so look for block placed on.

            MinecraftClient mc = MinecraftClient.getInstance();

            HitResult target = mc.hitResult;

            if (target.getType() == HitResult.Type.BLOCK) {
                final BlockHitResult hitBlock = (BlockHitResult) target;

                onPos = hitBlock.getBlockPos();

                // if block out of range there will be no "placed on" position
                // TODO: how does this math work again?
                if (onPos.getSquaredDistance(onPos) > Useful.squared(mc.interactionManager.getReachDistance() + 1)
                        && !VirtualBlock.isVirtuallySolidBlock(onPos, player)) {
                    onPos = null;
                } else {
                    onFace = hitBlock.getSide();
                    hitVec = hitBlock.getPos();
                }
            }
        }

        // assume user will click the right mouse button
        // Pass stack copy so that predicted action doesn't affect real stack
        return doRightClickBlock(player, onPos, onFace, hitVec, stack, item);
    }

    /**
     * Determines outcome when player right clicks on the face of a block. if no hit
     * block is known or if floating selection is known to be enabled, pass onPos,
     * onFace, and hitVec = null instead. DOES NOT UPDATE STATE.
     * <p>
     * 
     * Called by Predict placement results, and by OnItemUse and onItemRightClick.
     */
    public static PlacementResult doRightClickBlock(PlayerEntity player, @Nullable BlockPos onPos, @Nullable Direction onFace, @Nullable Vec3d hitVec,
            ItemStack stack, PlacementItem item) {

        PlacementPosition pPos = new PlacementPosition(player, onPos, onFace, hitVec, item.getFloatingSelectionRange(stack), item.isExcavator(stack));

        // if not position, then either need to be using floating selection
        // or a fixed region (for preview only - see logic below) if not enabled
        if (onPos == null && !item.isFloatingSelectionEnabled(stack)) {
            // don't force player to be in placement range to see big region selections
            // but note this doesn't work for selection in progress
            if (item.isFixedRegionEnabled(stack) && !item.isFixedRegionSelectionInProgress(stack)) {
                return new PlacementResult(pPos.inPos, PlacementEvent.NO_OPERATION_CONTINUE, PlacementSpecHelper.placementBuilder(player, pPos, stack));
            } else
                return PlacementResult.EMPTY_RESULT_CONTINUE;
        }

        // nothing to do if no position
        if (pPos.inPos == null)
            return PlacementResult.EMPTY_RESULT_CONTINUE;

        // only virtual blocks support advanced placement behavior
        // so emulate vanilla right-click behavior if we have non-virtual block
        if (!VirtualBlock.isVirtualBlock(((BlockItem) item).getBlock())) {
            ItemStack tweakedStack = stack.copy();
            item.setTargetMode(tweakedStack, TargetMode.ON_CLICKED_FACE);

            return new PlacementResult(pPos.inPos, PlacementEvent.PLACE, PlacementSpecHelper.placementBuilder(player, pPos, tweakedStack));
        }

        // Ctrl + right click: start new placement region
        if (ModKeys.isControlPressed(player)) {
            ItemStack tweakedStack = stack.copy();
            item.fixedRegionStart(tweakedStack, pPos.inPos, false);

            return new PlacementResult(pPos.inPos, PlacementEvent.START_PLACEMENT_REGION, PlacementSpecHelper.placementBuilder(player, pPos, tweakedStack));
        }

        if (item.isFixedRegionSelectionInProgress(stack)) {
            // finish placement region
            ItemStack tweakedStack = stack.copy();
            item.fixedRegionFinish(tweakedStack, player, pPos.inPos, false);
            IPlacementSpec builder = PlacementSpecHelper.placementBuilder(player, pPos, stack);

            return new PlacementResult(pPos.inPos, builder.isExcavation() ? PlacementEvent.EXCAVATE : PlacementEvent.PLACE,
                    PlacementSpecHelper.placementBuilder(player, pPos, tweakedStack));
        } else {
            // normal right click on block
            IPlacementSpec builder = PlacementSpecHelper.placementBuilder(player, pPos, stack);
            return new PlacementResult(pPos.inPos, builder.isExcavation() ? PlacementEvent.EXCAVATE : PlacementEvent.PLACE, builder);
        }
    }

    /**
     * Returns modified copy of stack adjusted for context-dependent state. Right
     * now this is just species. Intended for single and cubic region placements of
     * non-CSG virtual blocks.
     * <p>
     * 
     * Assumes block rotation was already set in stack by calling
     * {@link BlockOrientationHandler#configureStackForPlacement(ItemStack, PlayerEntity, PlacementPosition)}
     * when spec was constructed.
     */
    public static ItemStack cubicPlacementStack(SingleStackPlacementSpec specBuilder) {
        final ItemStack stack = specBuilder.placedStack().copy();
        final PlacementPosition pPos = specBuilder.placementPosition();
        @SuppressWarnings("rawtypes")
        final PrimitiveModelState.Mutable modelState = XmItem.modelState(stack);
        if (modelState != null && modelState.hasSpecies()) {
            final int species = speciesForPlacement(specBuilder.player(), pPos.onPos, pPos.onFace, stack, specBuilder.region());
            if (species >= 0) {
                modelState.species(species);
                XmStackHelper.setStackModelState(stack, modelState);
            }
        }
        modelState.release();
        return stack;
    }

    /**
     * Determines species that should be used for placing a region according to
     * current stack settings.
     */
    public static int speciesForPlacement(PlayerEntity player, BlockPos onPos, Direction onFace, ItemStack stack, grondag.fermion.position.BlockRegion region) {
        // ways this can happen:
        // have a species we want to match because we clicked on a face
        // break with everything - need to know adjacent species
        // match with most - need to know adjacent species

        if (!PlacementItem.isPlacementItem(stack))
            return 0;
        PlacementItem item = (PlacementItem) stack.getItem();

        SpeciesMode mode = item.getSpeciesMode(stack);
        if (ModKeys.isAltPressed(player))
            mode = mode.alternate();

        boolean shouldBreak = mode != SpeciesMode.MATCH_MOST;

        @SuppressWarnings("rawtypes")
        PrimitiveModelState.Mutable withModelState = XmItem.modelState(stack);
        if (withModelState == null || !withModelState.hasSpecies())
            return 0;

        if (player.world == null)
            return 0;

        World world = player.world;

        @SuppressWarnings("unused")
        BlockState withBlockState = item.getPlacementBlockStateFromStack(stack);

        // if no region provided or species mode used clicked block then
        // result is based on the clicked face
        if (region == null || ((mode == SpeciesMode.MATCH_CLICKED || mode == SpeciesMode.MATCH_MOST) && onPos != null && onFace != null)) {
            int clickedSpecies = SpeciesHelper.getJoinableSpecies(world, onPos, null); //withBlockState, withModelState);

            // if no region, then return something different than what is clicked,
            // unless didn't get a species - will return 0 in that case.
            if (region == null)
                return shouldBreak || clickedSpecies < 0 ? clickedSpecies + 1 : clickedSpecies;

            if (clickedSpecies >= 0)
                return clickedSpecies;
        }

        int[] adjacentCount = new int[16];
        int[] surfaceCount = new int[16];

        /** limit block positions checked for very large regions */
        int checkCount = 0;

        for (BlockPos pos : region.adjacentPositions()) {
            int adjacentSpecies = SpeciesHelper.getJoinableSpecies(world, pos, null); //withBlockState, withModelState);
            if (adjacentSpecies >= 0 && adjacentSpecies <= 15)
                adjacentCount[adjacentSpecies]++;
            if (checkCount++ >= XmConfig.BLOCKS.maxPlacementCheckCount)
                break;
        }

        for (BlockPos pos : region.surfacePositions()) {
            int interiorSpecies = SpeciesHelper.getJoinableSpecies(world, pos, null); //withBlockState, withModelState);
            if (interiorSpecies >= 0 && interiorSpecies <= 15)
                surfaceCount[interiorSpecies]++;
            if (checkCount++ >= XmConfig.BLOCKS.maxPlacementCheckCount)
                break;
        }

        if (shouldBreak) {
            // find a species that matches as few things as possible
            int bestSpecies = 0;
            int bestCount = adjacentCount[0] + surfaceCount[0];

            for (int i = 1; i < 16; i++) {
                int tryCount = adjacentCount[i] + surfaceCount[i];
                if (tryCount < bestCount) {
                    bestCount = tryCount;
                    bestSpecies = i;
                }
            }
            return bestSpecies;
        } else {
            // find the most common species and match with that
            // give preference to species that are included in the region surface if any
            int bestSpecies = 0;
            int bestCount = surfaceCount[0];

            for (int i = 1; i < 16; i++) {
                if (surfaceCount[i] > bestCount) {
                    bestCount = surfaceCount[i];
                    bestSpecies = i;
                }
            }

            if (bestCount == 0) {
                for (int i = 1; i < 16; i++) {
                    if (adjacentCount[i] > bestCount) {
                        bestCount = adjacentCount[i];
                        bestSpecies = i;
                    }
                }
            }
            return bestSpecies;
        }
    }

    /**
     * Find the position offset for the placement/deletion position values in
     * PlacementItem relative to the player's current orientation and starting
     * location.
     * 
     * @param onFace if non-null, assumes startPos is against this face, and should
     *               extend in the opposite direction. OffsetPosition alters box
     *               placements and is used to find alternate regions that might
     *               avoid obstacles.
     */
    public static BlockPos getPlayerRelativeOffset(BlockPos startPos, BlockPos offsetPos, PlayerEntity player, Direction onFace, OffsetPosition offset) {
        Vec3d lookVec = player.getRotationVec(1.0f);
        int xFactor = lookVec.x > 0 ? 1 : -1;
        int zFactor = lookVec.z > 0 ? 1 : -1;

        if (onFace != null) {
            switch (onFace.getAxis()) {
            case X:
                xFactor = onFace.getDirection().offset();
                break;
            case Z:
                zFactor = onFace.getDirection().offset();
                break;
            case Y:
            }
        }

        if (player.getHorizontalFacing().getAxis() == Direction.Axis.X) {
            return startPos.add((offsetPos.getX() - 1) * xFactor * offset.depthFactor, (offsetPos.getY() - 1) * offset.heightFactor,
                    (offsetPos.getZ() - 1) * zFactor * offset.widthFactor);
        } else {
            return startPos.add((offsetPos.getZ() - 1) * xFactor * offset.widthFactor, (offsetPos.getY() - 1) * offset.heightFactor,
                    (offsetPos.getX() - 1) * zFactor * offset.depthFactor);
        }
    }

    /**
     * Order of preference for selection adjustment based on player facing.
     */
    public static Direction[] faceCheckOrder(PlayerEntity player, Direction onFace) {
        if (onFace == null) {
            HorizontalFace playerFacing = HorizontalFace.find(player.getHorizontalFacing());
            Direction[] result = new Direction[6];
            result[0] = playerFacing.left().face;
            result[1] = playerFacing.right().face;
            result[2] = playerFacing.face.getOpposite();
            result[3] = playerFacing.face;
            result[4] = Direction.UP;
            result[5] = Direction.DOWN;
            return result;
        }

        switch (onFace) {
        case DOWN:
            final Direction[] DOWN_RESULT = { Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, };
            return DOWN_RESULT;

        case UP:
            final Direction[] UP_RESULT = { Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN };
            return UP_RESULT;

        case EAST:
            final Direction[] EAST_RESULT = { Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN };
            return EAST_RESULT;

        case NORTH:
            final Direction[] NORTH_RESULT = { Direction.NORTH, Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.UP, Direction.DOWN };
            return NORTH_RESULT;

        case SOUTH:
            final Direction[] SOUTH_RESULT = { Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.UP, Direction.DOWN };
            return SOUTH_RESULT;

        case WEST:
        default:
            final Direction[] WEST_RESULT = { Direction.WEST, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.UP, Direction.DOWN };
            return WEST_RESULT;

        }
    }

    public static void placeVirtualBlock(World world, ItemStack stack, PlayerEntity player, BlockPos pos) { //, Build build) {
        if (!player.canModifyWorld()) { // || build == null || !build.isOpen())
            return;
        }
        
        BlockSoundGroup soundtype = XmStackHelper.getStackSubstance(stack).soundType;

        PlacementItem item = PlacementItem.getPlacementItem(stack);
        if (item == null)
            return;

        BlockState placedState = item.getPlacementBlockStateFromStack(stack);

        if (placeBlockAt(stack, player, world, pos, null, 0, 0, 0, placedState)) {
            //build.addPosition(pos);

            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

//            BlockEntity blockTE = world.getBlockEntity(pos);
//            if (blockTE != null && blockTE instanceof VirtualBlockEntity) {
//                ((VirtualBlockEntity) blockTE).setBuild(build);
//            }
        }
    }

    private static boolean placeBlockAt(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ,
            BlockState newState) {
        // world.setBlockState returns false if the state was already the requested
        // state
        // this is OK normally, but if we need to update the TileEntity it is the
        // opposite of OK
        boolean wasUpdated = world.setBlockState(pos, newState, 3) || world.getBlockState(pos) == newState;

        if (!wasUpdated)
            return false;

        newState.getBlock().onPlaced(world, pos, newState, player, stack);
        return true;
    }
}
