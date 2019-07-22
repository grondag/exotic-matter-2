package grondag.xm2.placement;

import grondag.xm2.block.virtual.VirtualBlock;
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
            return new TypedActionResult<>(ActionResult.PASS, null);
        }
        
        ItemStack stackIn = player.getStackInHand(hand);

        if (stackIn.isEmpty() || stackIn.getItem() != this) {
            return new TypedActionResult<>(ActionResult.PASS, stackIn);
        }
        
        PlacementResult result = PlacementHandler.doRightClickBlock(player, null, null, null, stackIn, this);

        if (!result.shouldInputEventsContinue()) {
            result.apply(stackIn, player);
//            this.doPlacements(result, stackIn, world, player);
            return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
        }

        if (world.isClient) {
            HitResult hit = MinecraftClient.getInstance().hitResult;
            if(hit.getType() == HitResult.Type.BLOCK) {
                BlockPos blockpos = ((BlockHitResult)hit).getBlockPos();
                // if trying to place a block but too close, is annoying to get GUI
                // so only display if clicking on air
                if (blockpos != null && !VirtualBlock.isVirtualBlock(world.getBlockState(blockpos).getBlock())
                        && world.getBlockState(blockpos).getMaterial().isReplaceable() && this.isVirtual(stackIn)) {
                    this.displayGui(player);
                    return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
                }
            }
        }

        return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand));
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

        ItemStack stackIn = playerIn.getStackInHand(hand);
        if (stackIn.isEmpty() || stackIn.getItem() != this)
            return ActionResult.FAIL;

        PlacementResult result = PlacementHandler.doRightClickBlock(playerIn, context.getBlockPos(), facing, context.getHitPos(), stackIn, this);

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
        FilterMode result = PlacementItem.super.getFilterMode(stack);
        return result == FilterMode.REPLACE_ALL || result == FilterMode.REPLACE_SOLID ? result : FilterMode.REPLACE_SOLID;
    }

    @Override
    public boolean cycleFilterMode(ItemStack stack, boolean reverse) {
        boolean done = false;
        do {
            PlacementItem.super.cycleFilterMode(stack, reverse);
            FilterMode result = PlacementItem.super.getFilterMode(stack);
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
