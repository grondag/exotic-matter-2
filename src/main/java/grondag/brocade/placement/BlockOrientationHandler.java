package grondag.brocade.placement;

import grondag.brocade.block.ISuperBlock;
import grondag.brocade.block.SuperBlockStackHelper;
import grondag.brocade.connect.api.model.BlockEdge;
import grondag.brocade.connect.api.model.ClockwiseRotation;
import grondag.brocade.model.state.ISuperModelState;
import grondag.fermion.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Handles configuration of block orientation before placement based on stack
 * settings and placement context.
 */
public class BlockOrientationHandler {
    /**
     * Updates model state in provided stack if necessary based on other inputs.
     * Prior to calling, should already be verified that onFace and onPos can be
     * placed on (are solid) and that the space in which the block will be placed is
     * empty or replaceable.
     */
    public static void configureStackForPlacement(ItemStack stack, PlayerEntity player, PlacementPosition pPos) {
        // does not attempt to configure non super-blocks
        if (!(stack.getItem() instanceof IPlacementItem))
            return;

        IPlacementItem item = (IPlacementItem) stack.getItem();

        if (item.isBlockOrientationFixed(stack)) {
            applyFixedOrientation(stack);
        } else if (item.isBlockOrientationMatchClosest(stack)) {
            applyClosestOrientation(stack, player, pPos);
        } else if (item.isBlockOrientationDynamic(stack)) {
            applyDynamicOrientation(stack, player, pPos);
        }
    }

    private static void applyFixedOrientation(ItemStack stack) {
        if (!IPlacementItem.isPlacementItem(stack))
            return;

        IPlacementItem item = (IPlacementItem) stack.getItem();

        ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(stack);

        if (modelState.hasAxis()) {
            modelState.setAxis(item.getBlockPlacementAxis(stack));

            if (modelState.hasAxisOrientation()) {
                modelState.setAxisInverted(item.getBlockPlacementAxisIsInverted(stack));
            }
        }
        if (modelState.hasAxisRotation()) {
            modelState.setAxisRotation(item.getBlockPlacementRotation(stack));
        }

        SuperBlockStackHelper.setStackModelState(stack, modelState);
    }

    private static void applyClosestOrientation(ItemStack stack, PlayerEntity player, PlacementPosition pPos) {
        // find closest instance, starting with block placed on
        ISuperModelState outputModelState = SuperBlockStackHelper.getStackModelState(stack);
        ISuperModelState closestModelState = null;
        World world = player.world;
        BlockState onBlockState = world.getBlockState(pPos.onPos);
        Block onBlock = onBlockState.getBlock();

        if (onBlock instanceof ISuperBlock) {
            closestModelState = ((ISuperBlock)onBlock).getModelStateAssumeStateIsCurrent(onBlockState, world, pPos.onPos, true);

            // can't use onBlock as reference if is of a different type
            if (closestModelState.getShape() != outputModelState.getShape())
                closestModelState = null;
        }

        // block placed on was bust, so look around
        if (closestModelState == null) {
            Vec3d location = new Vec3d(pPos.onPos.getX() + pPos.hitX, pPos.onPos.getY() + pPos.hitY,
                    pPos.onPos.getZ() + pPos.hitZ);

            double closestDistSq = Double.MAX_VALUE;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if ((x | y | z) != 0) {
                            BlockPos testPos = pPos.onPos.add(x, y, z);
                            BlockState testBlockState = world.getBlockState(testPos);
                            if (testBlockState.getBlock() instanceof ISuperBlock) {
                                double distSq = location.squaredDistanceTo(pPos.onPos.getX() + 0.5 + x,
                                        pPos.onPos.getY() + 0.5 + y, pPos.onPos.getZ() + 0.5 + z);
                                if (distSq < closestDistSq) {
                                    ISuperBlock testBlock = (ISuperBlock) testBlockState.getBlock();
                                    ISuperModelState testModelState = testBlock.getModelStateAssumeStateIsCurrent(testBlockState, world, testPos, true);
                                    if (testModelState.getShape() == outputModelState.getShape()) {
                                        closestDistSq = distSq;
                                        closestModelState = testModelState;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // if didn't find any matching shapes nearby, fall back to dyanmic orientation
        if (closestModelState == null) {
            applyDynamicOrientation(stack, player, pPos);
        } else {
            if (outputModelState.hasAxis()) {
                outputModelState.setAxis(closestModelState.getAxis());

                if (outputModelState.hasAxisOrientation()) {
                    outputModelState.setAxisInverted(closestModelState.isAxisInverted());
                }
            }
            if (outputModelState.hasAxisRotation()) {
                outputModelState.setAxisRotation(closestModelState.getAxisRotation());
            }
        }

        SuperBlockStackHelper.setStackModelState(stack, outputModelState);
    }

    /** handle hit-sensitive placement for stairs, wedges */
    public static void applyDynamicOrientation(ItemStack stack, PlayerEntity player, PlacementPosition pPos) {
        ISuperModelState outputModelState = SuperBlockStackHelper.getStackModelState(stack);

        boolean isRotationDone = false;

        if (outputModelState.isAxisOrthogonalToPlacementFace()) {
            Direction adjacentFace = WorldHelper.closestAdjacentFace(pPos.onFace, (float) pPos.hitX, (float) pPos.hitY,
                    (float) pPos.hitZ);

            BlockEdge corner = BlockEdge.find(pPos.onFace.getOpposite(), adjacentFace);

            outputModelState.setAxis(corner.parallelAxis);

            if (outputModelState.hasAxisRotation()) {
                outputModelState.setAxisRotation(corner.rotation);
                isRotationDone = true;
            }
        } else {
            outputModelState.setAxis(pPos.onFace.getAxis());
            if (outputModelState.hasAxisOrientation()) {
                outputModelState.setAxisInverted(pPos.onFace.getDirection() == AxisDirection.NEGATIVE);
            }
        }

        if (!isRotationDone && outputModelState.hasAxisRotation()) {
            outputModelState.setAxisRotation(ClockwiseRotation.fromHorizontalFacing(player.getHorizontalFacing().getOpposite()));
        }

        SuperBlockStackHelper.setStackModelState(stack, outputModelState);
    }
}
