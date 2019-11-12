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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.xm.virtual.VirtualBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Base class for generic tool to mark real-world blocks for removal. Has
 * several selection modes. All actions are immediately submitted as jobs.
 * Re-skin within mods to match theme of mod and add features if appropriate.
 */
@API(status = Status.DEPRECATED)
@Deprecated
public class ExcavationMarker extends Item implements PlacementItem {
    public static final int FEATURE_FLAGS = PlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(PlacementItemFeature.FIXED_REGION,
            PlacementItemFeature.REGION_SIZE, PlacementItemFeature.FILTER_MODE);

    public ExcavationMarker(Item.Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        // TODO: logic here is too elaborate for what this item does, and probably
        // needed by Virtual Block

        if (player == null) {
            return new TypedActionResult<>(ActionResult.PASS, null, false);
        }

        ItemStack stackIn = player.getStackInHand(hand);

        if (stackIn.isEmpty() || stackIn.getItem() != this) {
            return new TypedActionResult<>(ActionResult.PASS, stackIn, false);
        }

        PlacementResult result = PlacementHandler.doRightClickBlock(player, null, null, null, stackIn, this);

        if (!result.shouldInputEventsContinue()) {
            result.apply(stackIn, player);
//            this.doPlacements(result, stackIn, world, player);
            return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand), true);
        }

        if (world.isClient) {
            final HitResult hit = MinecraftClient.getInstance().hitResult;
            if(hit.getType() == HitResult.Type.BLOCK) {
                final BlockPos blockpos = ((BlockHitResult)hit).getBlockPos();
                // if trying to place a block but too close, is annoying to get GUI
                // so only display if clicking on air
                if (blockpos != null && !VirtualBlock.isVirtualBlock(world.getBlockState(blockpos).getBlock())
                        && world.getBlockState(blockpos).getMaterial().isReplaceable() && this.isVirtual(stackIn)) {
                    this.displayGui(player);
                    return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand), true);
                }
            }
        }

        return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand), false);
    }



    @Override
    public ActionResult useOnBlock(ItemUsageContext context ) {
        // TODO: logic here is too elaborate for what this item does, and probably
        // needed by Virtual Block
        final PlayerEntity playerIn = context.getPlayer();
        final Hand hand = context.getHand();
        final Direction facing = context.getSide();

        if (playerIn == null)
            return ActionResult.FAIL;

        final ItemStack stackIn = playerIn.getStackInHand(hand);
        if (stackIn.isEmpty() || stackIn.getItem() != this)
            return ActionResult.FAIL;

        final PlacementResult result = PlacementHandler.doRightClickBlock(playerIn, context.getBlockPos(), facing, context.getHitPos(), stackIn, this);

        result.apply(stackIn, playerIn);

        // we don't return pass because don't want GUI displayed or other events to
        // process
        return ActionResult.SUCCESS;
    }

    @Override
    public TargetMode getTargetMode(ItemStack stack) {
        return TargetMode.FILL_REGION;
    }

    @Override
    public RegionOrientation getRegionOrientation(ItemStack stack) {
        return RegionOrientation.XYZ;
    }

    @Override
    public FilterMode getFilterMode(ItemStack stack) {
        final FilterMode result = PlacementItem.super.getFilterMode(stack);
        return result == FilterMode.REPLACE_ALL || result == FilterMode.REPLACE_SOLID ? result : FilterMode.REPLACE_SOLID;
    }

    @Override
    public boolean cycleFilterMode(ItemStack stack, boolean reverse) {
        boolean done = false;
        do {
            PlacementItem.super.cycleFilterMode(stack, reverse);
            final FilterMode result = PlacementItem.super.getFilterMode(stack);
            done = result == FilterMode.REPLACE_ALL || result == FilterMode.REPLACE_SOLID;
        } while (!done);
        return true;
    }

    @Override
    public int featureFlags(ItemStack stack) {
        return FEATURE_FLAGS;
    }

    @Override
    public boolean isExcavator(ItemStack placedStack) {
        return true;
    }

    @Override
    public boolean isVirtual(ItemStack stack) {
        return false;
    }

}
