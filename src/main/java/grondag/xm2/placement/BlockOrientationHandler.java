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

import grondag.fermion.world.WorldHelper;
import grondag.xm2.api.connect.model.BlockCorner;
import grondag.xm2.api.connect.model.BlockEdge;
import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.model.MutableModelState;
import grondag.xm2.api.model.MutablePrimitiveModelState;
import grondag.xm2.api.model.PrimitiveModelState;
import grondag.xm2.block.XmBlockState;
import grondag.xm2.block.XmBlockStateAccess;
import grondag.xm2.block.XmStackHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
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
        if (!(stack.getItem() instanceof PlacementItem))
            return;

        PlacementItem item = (PlacementItem) stack.getItem();

        if (item.isBlockOrientationFixed(stack)) {
            applyFixedOrientation(stack);
        } else if (item.isBlockOrientationMatchClosest(stack)) {
            applyClosestOrientation(stack, player, pPos);
        } else if (item.isBlockOrientationDynamic(stack)) {
            applyDynamicOrientation(stack, player, pPos);
        }
    }

    private static void applyFixedOrientation(ItemStack stack) {
        if (!PlacementItem.isPlacementItem(stack))
            return;

        PlacementItem item = (PlacementItem) stack.getItem();

        MutablePrimitiveModelState modelState = XmStackHelper.getStackModelState(stack);

        if (modelState.hasAxis()) {
            modelState.setAxis(item.getBlockPlacementAxis(stack));

            if (modelState.hasAxisOrientation()) {
                modelState.setAxisInverted(item.getBlockPlacementAxisIsInverted(stack));
            }
        }
        if (modelState.hasAxisRotation()) {
            modelState.setAxisRotation(item.getBlockPlacementRotation(stack));
        }

        XmStackHelper.setStackModelState(stack, modelState);
    }

    private static void applyClosestOrientation(ItemStack stack, PlayerEntity player, PlacementPosition pPos) {
        // find closest instance, starting with block placed on
        MutablePrimitiveModelState outputModelState = XmStackHelper.getStackModelState(stack);
        PrimitiveModelState closestModelState = null;
        World world = player.world;
        BlockState onBlockState = world.getBlockState(pPos.onPos);

        closestModelState = XmBlockStateAccess.modelState(onBlockState, world, pPos.onPos, true);
        // can't use onBlock as reference if is of a different type
        if (closestModelState != null
                && closestModelState.primitive() != outputModelState.primitive()) {
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
                            XmBlockState testBlockState = XmBlockStateAccess.get(world.getBlockState(testPos));
                            if (testBlockState != null) {
                                double distSq = location.squaredDistanceTo(pPos.onPos.getX() + 0.5 + x,
                                        pPos.onPos.getY() + 0.5 + y, pPos.onPos.getZ() + 0.5 + z);
                                if (distSq < closestDistSq) {
                                    PrimitiveModelState testModelState = testBlockState.getModelState(world, testPos, true);
                                    if (testModelState.primitive() == outputModelState.primitive()) {
                                        closestDistSq = distSq;
                                        closestModelState = testModelState.toImmutable();
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

        XmStackHelper.setStackModelState(stack, outputModelState);
    }

    // FIX: pretty sure this doesn't work now
    /** handle hit-sensitive placement for stairs, wedges */
    public static void applyDynamicOrientation(ItemStack stack, PlayerEntity player, PlacementPosition pPos) {
        MutablePrimitiveModelState outputModelState = XmStackHelper.getStackModelState(stack);

        boolean isRotationDone = false;

        if (outputModelState.isAxisOrthogonalToPlacementFace()) {
            Direction adjacentFace = WorldHelper.closestAdjacentFace(pPos.onFace, (float) pPos.hitX, (float) pPos.hitY,
                    (float) pPos.hitZ);

            BlockEdge edge = BlockEdge.find(pPos.onFace.getOpposite(), adjacentFace);

            outputModelState.setAxis(edge.face1.getAxis());

            if (outputModelState.hasAxisRotation()) {
                outputModelState.setAxisRotation(edge.rotation);
                isRotationDone = true;
            }
        } else {
            outputModelState.setAxis(pPos.onFace.getAxis());
            if (outputModelState.hasAxisOrientation()) {
                outputModelState.setAxisInverted(pPos.onFace.getDirection() == AxisDirection.NEGATIVE);
            }
        }

        if (!isRotationDone
                && outputModelState.hasAxisRotation()) {
            outputModelState.setAxisRotation(
                    ClockwiseRotation.fromHorizontalFacing(player.getHorizontalFacing().getOpposite()));
        }

        XmStackHelper.setStackModelState(stack, outputModelState);
    }

    public static final EnumProperty<Axis> AXIS_PROP = EnumProperty.of("xm2_axis", Axis.class);
    public static final EnumProperty<Direction> FACE_PROP = EnumProperty.of("xm2_face", Direction.class);
    public static final EnumProperty<BlockEdge> EDGE_PROP = EnumProperty.of("xm2_edge", BlockEdge.class);
    public static final EnumProperty<BlockCorner> CORNER_PROP = EnumProperty.of("xm2_corner", BlockCorner.class);

    /** returns updated block state based on placement context */
    public static BlockState axisBlockState(BlockState stateIn, ItemPlacementContext context) {
        // TODO: implement
        return stateIn;
    }

    /** returns updated block state based on placement context */
    public static BlockState faceBlockState(BlockState stateIn, ItemPlacementContext context) {
        // TODO: implement
        return stateIn;
    }

    /** returns updated block state based on placement context */
    public static BlockState edgeBlockState(BlockState stateIn, ItemPlacementContext context) {
        final Direction onFace = context.getSide();
        final BlockPos pos = context.getBlockPos();
        final Vec3d hit = context.getHitPos();

        final Direction adjacentFace;

        if (onFace.getAxis() == Axis.Y) {
            adjacentFace = context.getPlayerFacing();
        } else {

            adjacentFace = WorldHelper.closestAdjacentFace(onFace, (float) (hit.x - pos.getX()),
                    (float) (hit.y - pos.getY()), (float) (hit.z - pos.getZ()));
        }

        final BlockEdge edge = BlockEdge.find(onFace.getOpposite(), adjacentFace);

        return edge == null ? stateIn : stateIn.with(EDGE_PROP, edge);
    }

    /** returns updated block state based on placement context */
    public static BlockState cornerBlockState(BlockState stateIn, ItemPlacementContext context) {
        // TODO: implement
        return stateIn;
    }

    /** updates model state from block state */
    public static void axisModelState(BlockState stateIn, MutableModelState modelState) {
        // TODO: implement
    }

    /** updates model state from block state */
    public static void faceModelState(BlockState stateIn, MutableModelState modelState) {
        // TODO: implement
    }

    /** updates model state from block state */
    public static void edgeModelState(BlockState stateIn, MutableModelState modelState) {
        final BlockEdge edge = (BlockEdge) stateIn.getEntries().get(EDGE_PROP);
        if (edge != null) {
            modelState.setAxis(edge.face1.getAxis());
            modelState.setAxisInverted(edge.face1.getDirection() != AxisDirection.NEGATIVE);
            modelState.setAxisRotation(edge.rotation);
        }
    }

    /** updates model state from block state */
    public static void cornerModelState(BlockState stateIn, MutableModelState modelState) {
        // TODO: implement
    }
}
