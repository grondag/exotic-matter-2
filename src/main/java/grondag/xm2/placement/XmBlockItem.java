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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

//TODO: still needed?
public class XmBlockItem extends BlockItem implements PlacementItem {

    public static final int FEATURE_FLAGS = PlacementItem.BENUMSET_FEATURES.getFlagsForIncludedValues(PlacementItemFeature.BLOCK_ORIENTATION,
            PlacementItemFeature.SPECIES_MODE);

    public XmBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public int featureFlags(ItemStack stack) {
        return FEATURE_FLAGS;
    }

//    public List<String> getWailaBody(ItemStack itemStack, List<String> current tip, IWailaDataAccessor accessor, IWailaConfigHandler config)
//    {
//        List<String> retVal = new ArrayList<String>();
//        this.addInformation(itemStack, null, retVal, false);
//        return retVal;
//    }

    /**
     * <i>Grondag: don't want to overflow size limits or burden network by sending
     * details of embedded storage that will never be used on the client
     * anyway.</i><br>
     * <br>
     * 
     * {@inheritDoc}
     */

    // TODO: remove or rework
//    @Override
//    public final CompoundTag getNBTShareTag(ItemStack stack) {
//        return SuperTileEntity.withoutServerTag(super.getNBTShareTag(stack));
//    }

    @Override
    public boolean isVirtual(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isExcavator(ItemStack placedStack) {
        return false;
    }

    @Override
    public boolean place(ItemPlacementContext context, BlockState newState) {
        return super.place(context, newState);

//        // world.setBlockState returns false if the state was already the requested
//        // state
//        // this is OK normally, but if we need to update the TileEntity it is the
//        // opposite of OK
//        BlockPos pos = context.getBlockPos();
//        World world = context.getWorld();
//        
//        boolean wasUpdated = world.setBlockState(pos, newState, 11) || world.getBlockState(pos) == newState;
//
//        if (!wasUpdated)
//            return false;
//
//        // TODO: remove if not needed
////        this.block.onBlockPlacedBy(world, pos, newState, player, stack);
//        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return super.use(world, player, hand);
//        return new TypedActionResult<ItemStack>(ActionResult.PASS, player.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return super.useOnBlock(context);
//        ItemStack stackIn = context.getStack();
//        World worldIn = context.getWorld();
//        BlockPos pos = context.getBlockPos();
//        PlayerEntity player = context.getPlayer();
//        Direction facing = context.getSide();
//        ItemPlacementContext pctx = new ItemPlacementContext(context);
//        
//        // NB: block is to limit scope of vars
//        {
//            
//            BlockState currentState = worldIn.getBlockState(pos);
//            Block block = currentState.getBlock();
//
//            // this check will probably need to be adjusted for additive blocks
//            // but not having this for normal blocks means we replace cables or
//            // other block that don't fully occlude the targeted face.
//            if (!block.canReplace(currentState, pctx))
//                return ActionResult.FAIL;
//
//            if (stackIn.isEmpty())
//                return ActionResult.FAIL;
//
//        }
//
//        MeshState modelState = BrocadeBlockStackHelper.getStackModelState(stackIn);
//        if (modelState == null)
//            return ActionResult.FAIL;
//
//        BoundingBox shape = CollisionBoxDispatcher.getOutlineShape(modelState).getBoundingBox();
//
//        if (!worldIn.doesNotCollide(shape.offset(pos)))
//            return ActionResult.FAIL;
//
//        BlockState placedState = PlacementItem.getPlacementBlockStateFromStackStatically(stackIn);
//
//        /**
//         * Adjust block rotation if supported.
//         */
//        final Vec3d hitPos = context.getHitPos();
//        
//        ItemStack placedStack = stackIn.copy();
//        if (!modelState.isStatic()) {
//            PlacementItem item = (PlacementItem) placedStack.getItem();
//
//            BlockOrientationHandler.applyDynamicOrientation(placedStack, player,
//                    new PlacementPosition(player, pos, facing, new Vec3d(hitPos.x, hitPos.y, hitPos.z),
//                            item.getFloatingSelectionRange(placedStack), item.isExcavator(placedStack)));
//        }
//
//        if (place(pctx, placedState)) {
//            placedState = worldIn.getBlockState(pos);
//            BlockSoundGroup soundtype = placedState.getBlock().getSoundGroup(placedState);
//            worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
//                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
//            if (!(player.isCreative()))
//                stackIn.decrement(1);
//        }
//
//        return ActionResult.SUCCESS;
    }
}
