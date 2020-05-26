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
package grondag.xm.api.block.base;

import static net.minecraft.block.StairsBlock.WATERLOGGED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Random;
import java.util.function.Predicate;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import grondag.fermion.spatial.DirectionHelper;
import grondag.fermion.world.WorldHelper;
import grondag.xm.SidedHelper;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.block.XmProperties;
import grondag.xm.api.collision.CollisionDispatcher;
import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import grondag.xm.api.orientation.CubeRotation;
import grondag.xm.api.orientation.FaceEdge;
import grondag.xm.api.orientation.HorizontalEdge;
import grondag.xm.api.primitive.simple.Stair;

@API(status = EXPERIMENTAL)
public class StairLike extends Block implements Waterloggable {
	protected final Block baseBlock;
	protected final BlockState baseBlockState;

	public enum Shape {
		STRAIGHT,
		INSIDE_CORNER,
		OUTSIDE_CORNER;
	}

	public final Shape shape;

	protected final Predicate<PlayerEntity> modKeyTest;
	protected final Predicate<PlayerEntity> forceKeyTest;

	public StairLike(BlockState blockState, Settings settings, Shape shape, Predicate<PlayerEntity> modKeyTest, Predicate<PlayerEntity> forceKeyTest) {
		super(FabricBlockSettings.copyOf(settings).dynamicBounds());
		setDefaultState(stateManager.getDefaultState()
				.with(WATERLOGGED, false));
		baseBlock = blockState.getBlock();
		baseBlockState = blockState;
		this.shape = shape;
		SidedHelper.mapRenderLayerLike(this, baseBlockState);
		this.modKeyTest = modKeyTest;
		this.forceKeyTest = forceKeyTest;
	}

	@Override
	public boolean hasSidedTransparency(BlockState blockState_1) {
		return true;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos pos, ShapeContext entityContext) {
		return CollisionDispatcher.shapeFor(XmBlockState.modelState(blockState, blockView, pos, true));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState blockState, World world, BlockPos blockPos, Random random) {
		baseBlock.randomDisplayTick(blockState, world, blockPos, random);
	}

	@Override
	public void onBlockBreakStart(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity) {
		baseBlockState.onBlockBreakStart(world, blockPos, playerEntity);
	}

	@Override
	public void onBroken(WorldAccess iWorld, BlockPos blockPos, BlockState blockState) {
		baseBlock.onBroken(iWorld, blockPos, blockState);
	}

	@Override
	public float getBlastResistance() {
		return baseBlock.getBlastResistance();
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!state.isOf(state.getBlock())) {
			baseBlockState.neighborUpdate(world, pos, Blocks.AIR, pos, false);
			baseBlock.onBlockAdded(baseBlockState, world, pos, oldState, false);
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean notify) {
		if (!state.isOf(newState.getBlock())) {
			baseBlockState.onStateReplaced(world, pos, newState, notify);
		}
	}

	@Override
	public void onSteppedOn(World world, BlockPos blockPos, Entity entity) {
		baseBlock.onSteppedOn(world, blockPos, entity);
	}

	@Override
	public boolean hasRandomTicks(BlockState blockState) {
		return baseBlock.hasRandomTicks(blockState);
	}

	@Override
	public void randomTick(BlockState blockState, ServerWorld serverWorld, BlockPos blockPos, Random random) {
		baseBlock.randomTick(blockState, serverWorld, blockPos, random);
	}

	@Override
	public void scheduledTick(BlockState blockState, ServerWorld serverWorld, BlockPos blockPos, Random random) {
		baseBlock.scheduledTick(blockState, serverWorld, blockPos, random);
	}

	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult) {
		return baseBlockState.onUse(world, playerEntity, hand, blockHitResult);
	}

	@Override
	public void onDestroyedByExplosion(World world, BlockPos blockPos, Explosion explosion) {
		baseBlock.onDestroyedByExplosion(world, blockPos, explosion);
	}

	//UGLY: It was bad in the previous versions, too.  There must be a better model for this, but I haven't found it yet.
	//TODO: consider splitting this mess into a utility class for reuse - like it was in prior version
	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		final BlockPos pos = context.getBlockPos();
		final PlayerEntity player = context.getPlayer();
		final FluidState fluidState = context.getWorld().getFluidState(pos);
		final Direction onFace = context.getSide().getOpposite();
		BlockState result = getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);

		Direction bottomFace = Direction.DOWN;
		Direction backFace = Direction.SOUTH;
		if(player != null) {
			final Direction[] faces = context.getPlacementDirections();
			final int xIndex = faces[0].getAxis() == Axis.X ? 0 : (faces[1].getAxis() == Axis.X ? 1 : 2);
			final int yIndex = faces[0].getAxis() == Axis.Y ? 0 : (faces[1].getAxis() == Axis.Y ? 1 : 2);
			final int zIndex = faces[0].getAxis() == Axis.Z ? 0 : (faces[1].getAxis() == Axis.Z ? 1 : 2);

			final boolean modKey = modKeyTest.test(player);
			final boolean forceKey = forceKeyTest.test(player);

			final Vec3d hit = context.getHitPos();
			if(shape == Shape.STRAIGHT) {
				if (modKey) {
					// horizontal stairs
					if (onFace.getAxis() != Axis.Y) {
						bottomFace = onFace;
						if (forceKey) {
							backFace = WorldHelper.closestAdjacentFace(onFace, hit.x, hit.y, hit.z);
						} else {
							if(onFace.getAxis() == Axis.X) {
								backFace = yIndex < zIndex ? faces[yIndex] : faces[zIndex];
							} else {
								backFace = yIndex < xIndex ? faces[yIndex] : faces[xIndex];
							}
						}
					} else {
						// placed on up or down
						backFace = onFace;
						bottomFace = forceKey
								? WorldHelper.closestAdjacentFace(onFace, hit.x, hit.y, hit.z)
										: player.getHorizontalFacing();
					}
				} else {
					// vertical (normal)
					if (onFace.getAxis() == Axis.Y) {
						bottomFace = onFace;
						backFace = forceKey
								? WorldHelper.closestAdjacentFace(onFace, hit.x, hit.y, hit.z)
										: player.getHorizontalFacing();
					} else {
						backFace = onFace;
						if( forceKey) {
							final Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, hit.x, hit.y, hit.z);
							bottomFace = pair.getLeft().getAxis() == Axis.Y ? pair.getLeft() : pair.getRight();
						} else {
							bottomFace = faces[yIndex];
						}
					}
				}
			} else {
				// CORNER
				if (modKey) {
					// Horizontal
					if (onFace.getAxis() == Axis.Y) {
						// placed on up or down
						if (forceKey) {
							final Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, (float)hit.x, (float)hit.y, (float)hit.z);
							bottomFace = pair.getLeft();
							final Direction rightFace = FaceEdge.fromWorld(onFace, bottomFace).counterClockwise().toWorld(bottomFace);
							backFace = rightFace == pair.getRight() ? onFace : pair.getRight();
						} else {
							bottomFace = player.getHorizontalFacing();
							final int otherIndex = bottomFace.getAxis() == Axis.X ? zIndex : xIndex;
							final Direction otherFace = faces[otherIndex];
							final Direction rightFace = FaceEdge.fromWorld(onFace, bottomFace).counterClockwise().toWorld(bottomFace);
							backFace = rightFace == otherFace ? onFace : otherFace;
						}
					} else {
						// placed on bottom (horizontal) face directly
						bottomFace = onFace;
						if (forceKey) {
							final Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, (float)hit.x, (float)hit.y, (float)hit.z);
							boolean leftRightOrder = DirectionHelper.counterClockwise(pair.getLeft(), onFace.getAxis()) == pair.getRight();
							if (onFace.getDirection() == AxisDirection.NEGATIVE) {
								leftRightOrder = !leftRightOrder;
							}
							backFace = leftRightOrder ? pair.getRight() : pair.getLeft();
						} else {
							final int firstIndex = onFace.getAxis() == Axis.X ? Math.min(yIndex, zIndex) : Math.min(yIndex, xIndex);
							final int secondIndex = onFace.getAxis() == Axis.X ? Math.max(yIndex, zIndex) : Math.max(yIndex, xIndex);
							final Direction firstFace = faces[firstIndex];
							final Direction secondFace = faces[secondIndex];
							final Direction rightFace = FaceEdge.fromWorld(firstFace, bottomFace).counterClockwise().toWorld(bottomFace);
							backFace = rightFace == secondFace ? firstFace : secondFace;
						}
					}
				} else {
					// vertical (normal)
					if(forceKey) {
						if(onFace.getAxis() == Axis.Y) {
							bottomFace = onFace;
							backFace = WorldHelper.closestAdjacentFace(onFace, hit.x, hit.y, hit.z);
						} else {
							final Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, hit.x, hit.y, hit.z);
							final boolean isLeftY = pair.getLeft().getAxis() == Axis.Y;
							bottomFace = isLeftY ? pair.getLeft() : pair.getRight();
							final Direction neighborFace = isLeftY ? pair.getRight() : pair.getLeft();
							final HorizontalEdge edge = HorizontalEdge.find(onFace, neighborFace);
							backFace = bottomFace == Direction.DOWN ? edge.left.face : edge.right.face;
						}
					} else {
						bottomFace = faces[yIndex];
						final HorizontalEdge edge = HorizontalEdge.fromRotation(player.yaw);
						backFace = bottomFace == Direction.DOWN ? edge.left.face : edge.right.face;
					}
				}
			}
		}
		result = result.with(XmProperties.ROTATION, ObjectUtils.defaultIfNull(CubeRotation.find(bottomFace, backFace), CubeRotation.DOWN_WEST));
		return result;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState blockState_1, Direction direction_1, BlockState blockState_2, WorldAccess iWorld_1, BlockPos blockPos_1, BlockPos blockPos_2) {
		if (blockState_1.get(WATERLOGGED)) {
			iWorld_1.getFluidTickScheduler().schedule(blockPos_1, Fluids.WATER, Fluids.WATER.getTickRate(iWorld_1));
		}
		return super.getStateForNeighborUpdate(blockState_1, direction_1, blockState_2, iWorld_1, blockPos_1, blockPos_2);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(XmProperties.ROTATION, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState blockState_1) {
		return blockState_1.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(blockState_1);
	}

	@Override
	public boolean canPathfindThrough(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, NavigationType blockPlacementEnvironment_1) {
		return false;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(XmProperties.ROTATION, state.get(XmProperties.ROTATION).rotate(rotation));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirrir) {
		return state.rotate(BlockRotation.CLOCKWISE_180);
	}

	public static SimplePrimitiveStateMutator MODELSTATE_FROM_BLOCKSTATE = (modelState, blockState) -> {
		final Block rawBlock = blockState.getBlock();
		if(!(rawBlock instanceof StairLike)) {
			return modelState;
		}

		final StairLike block = (StairLike)rawBlock;
		Stair.setCorner(block.shape != Shape.STRAIGHT, modelState);
		Stair.setInsideCorner(block.shape == Shape.INSIDE_CORNER, modelState);
		modelState.orientationIndex(blockState.get(XmProperties.ROTATION).ordinal());
		return modelState;
	};
}
