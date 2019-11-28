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

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import grondag.fermion.world.WorldHelper;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.orientation.CubeCorner;
import grondag.xm.api.orientation.CubeEdge;
import grondag.xm.relics.XmStackHelper;

/**
 * Handles configuration of block orientation before placement based on stack
 * settings and placement context.
 */
@SuppressWarnings("rawtypes")
@API(status = Status.DEPRECATED)
@Deprecated
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

		final PlacementItem item = (PlacementItem) stack.getItem();

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
		//TODO: remove or reimplement
		//        PlacementItem item = (PlacementItem) stack.getItem();
		//
		//        PrimitiveModelState.Mutable modelState = XmStackHelper.getStackModelState(stack);

		//        if (modelState.hasAxis()) {
		//            modelState.axis(item.getBlockPlacementAxis(stack));
		//
		//            if (modelState.hasAxisOrientation()) {
		//                modelState.setAxisInverted(item.getBlockPlacementAxisIsInverted(stack));
		//            }
		//        }
		//        if (modelState.hasAxisRotation()) {
		//            modelState.axisRotation(item.getBlockPlacementRotation(stack));
		//        }

		//        XmStackHelper.setStackModelState(stack, modelState);
	}

	private static void applyClosestOrientation(ItemStack stack, PlayerEntity player, PlacementPosition pPos) {
		// find closest instance, starting with block placed on
		final MutableBaseModelState outputModelState = XmItem.modelState(stack);
		MutableBaseModelState closestModelState = null;
		final World world = player.world;
		final BlockState onBlockState = world.getBlockState(pPos.onPos);

		closestModelState = XmBlockState.modelState(onBlockState, world, pPos.onPos, true);
		// can't use onBlock as reference if is of a different type
		if (closestModelState != null && closestModelState.primitive() != outputModelState.primitive()) {
			closestModelState = null;
		}

		// block placed on was bust, so look around
		if (closestModelState == null) {
			final Vec3d location = new Vec3d(pPos.onPos.getX() + pPos.hitX, pPos.onPos.getY() + pPos.hitY, pPos.onPos.getZ() + pPos.hitZ);

			double closestDistSq = Double.MAX_VALUE;
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if ((x | y | z) != 0) {
							final BlockPos testPos = pPos.onPos.add(x, y, z);
							final XmBlockState testBlockState = XmBlockState.get(world.getBlockState(testPos));
							if (testBlockState != null) {
								final double distSq = location.squaredDistanceTo(pPos.onPos.getX() + 0.5 + x, pPos.onPos.getY() + 0.5 + y,
										pPos.onPos.getZ() + 0.5 + z);
								if (distSq < closestDistSq) {
									final MutableBaseModelState testModelState = testBlockState.modelState(world, testPos, true);
									if (testModelState.primitive() == outputModelState.primitive()) {
										closestDistSq = distSq;
										closestModelState = testModelState;
									} else {
										testModelState.release();
									}
								}
							}
						}
					}
				}
			}
		}

		//TODO: remove or reimplement
		// if didn't find any matching shapes nearby, fall back to dyanmic orientation
		//        if (closestModelState == null) {
		//            applyDynamicOrientation(stack, player, pPos);
		//        } else {
		//            if (outputModelState.hasAxis()) {
		//                outputModelState.axis(closestModelState.axis());
		//
		//                if (outputModelState.hasAxisOrientation()) {
		//                    outputModelState.setAxisInverted(closestModelState.isAxisInverted());
		//                }
		//            }
		//            if (outputModelState.hasAxisRotation()) {
		//                outputModelState.axisRotation(closestModelState.axisRotation());
		//            }
		//            closestModelState.release();
		//        }

		XmStackHelper.setStackModelState(stack, outputModelState);
		outputModelState.release();
	}

	// FIX: pretty sure this doesn't work now
	/** handle hit-sensitive placement for stairs, wedges */
	public static void applyDynamicOrientation(ItemStack stack, PlayerEntity player, PlacementPosition pPos) {
		final MutableBaseModelState outputModelState = XmItem.modelState(stack);

		//TODO: remove or reimplement
		//        boolean isRotationDone = false;
		//
		//        if (outputModelState.isAxisOrthogonalToPlacementFace()) {
		//            Direction adjacentFace = WorldHelper.closestAdjacentFace(pPos.onFace, (float) pPos.hitX, (float) pPos.hitY, (float) pPos.hitZ);
		//
		//            BlockEdge edge = BlockEdge.find(pPos.onFace.getOpposite(), adjacentFace);
		//
		//            outputModelState.axis(edge.face1.getAxis());
		//
		//            if (outputModelState.hasAxisRotation()) {
		//                outputModelState.axisRotation(edge.rotation);
		//                isRotationDone = true;
		//            }
		//        } else {
		//            outputModelState.axis(pPos.onFace.getAxis());
		//            if (outputModelState.hasAxisOrientation()) {
		//                outputModelState.setAxisInverted(pPos.onFace.getDirection() == AxisDirection.NEGATIVE);
		//            }
		//        }
		//
		//        if (!isRotationDone && outputModelState.hasAxisRotation()) {
		//            outputModelState.axisRotation(ClockwiseRotation.fromHorizontalFacing(player.getHorizontalFacing().getOpposite()));
		//        }

		XmStackHelper.setStackModelState(stack, outputModelState);
		outputModelState.release();
	}

	public static final EnumProperty<Axis> AXIS_PROP = EnumProperty.of("xm2_axis", Axis.class);
	public static final EnumProperty<Direction> FACE_PROP = EnumProperty.of("xm2_face", Direction.class);
	public static final EnumProperty<CubeEdge> EDGE_PROP = EnumProperty.of("xm2_edge", CubeEdge.class);
	public static final EnumProperty<CubeCorner> CORNER_PROP = EnumProperty.of("xm2_corner", CubeCorner.class);

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

			adjacentFace = WorldHelper.closestAdjacentFace(onFace, (float) (hit.x - pos.getX()), (float) (hit.y - pos.getY()), (float) (hit.z - pos.getZ()));
		}

		final CubeEdge edge = CubeEdge.find(onFace.getOpposite(), adjacentFace);

		return edge == null ? stateIn : stateIn.with(EDGE_PROP, edge);
	}

	/** returns updated block state based on placement context */
	public static BlockState cornerBlockState(BlockState stateIn, ItemPlacementContext context) {
		// TODO: implement
		return stateIn;
	}

	/** updates model state from block state */
	public static void axisModelState(BlockState stateIn, MutableBaseModelState modelState) {
		// TODO: implement
	}

	/** updates model state from block state */
	public static void faceModelState(BlockState stateIn, MutableBaseModelState modelState) {
		// TODO: implement
	}

	/** updates model state from block state */
	public static void edgeModelState(BlockState stateIn, MutableBaseModelState modelState) {
		final CubeEdge edge = (CubeEdge) stateIn.getEntries().get(EDGE_PROP);
		if (edge != null) {
			//TODO: remove or reimplement
			//            modelState.axis(edge.face1.getAxis());
			//            modelState.setAxisInverted(edge.face1.getDirection() != AxisDirection.NEGATIVE);
			//            modelState.axisRotation(edge.rotation);
		}
	}

	/** updates model state from block state */
	public static void cornerModelState(BlockState stateIn, MutableBaseModelState modelState) {
		// TODO: implement
	}
}
