/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.api.block.base;

import static net.minecraft.world.level.block.StairBlock.WATERLOGGED;

import java.util.function.Predicate;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import grondag.xm.SidedHelper;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.block.XmProperties;
import grondag.xm.api.collision.CollisionDispatcher;
import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import grondag.xm.api.primitive.simple.Stair;
import grondag.xm.api.util.WorldHelper;
import grondag.xm.orientation.api.CubeRotation;
import grondag.xm.orientation.api.DirectionHelper;
import grondag.xm.orientation.api.FaceEdge;
import grondag.xm.orientation.api.HorizontalEdge;

// WIP: Fabric deps
@Experimental
public class StairLike extends Block implements SimpleWaterloggedBlock {
	protected final Block baseBlock;
	protected final BlockState baseBlockState;

	public enum Shape {
		STRAIGHT,
		INSIDE_CORNER,
		OUTSIDE_CORNER;
	}

	public final Shape shape;

	protected final Predicate<Player> modKeyTest;
	protected final Predicate<Player> forceKeyTest;

	public StairLike(BlockState blockState, Properties settings, Shape shape, Predicate<Player> modKeyTest, Predicate<Player> forceKeyTest) {
		super(FabricBlockSettings.copyOf(settings).dynamicShape());
		registerDefaultState(stateDefinition.any()
				.setValue(WATERLOGGED, false));
		baseBlock = blockState.getBlock();
		baseBlockState = blockState;
		this.shape = shape;
		SidedHelper.mapRenderLayerLike(this, baseBlockState);
		this.modKeyTest = modKeyTest;
		this.forceKeyTest = forceKeyTest;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState_1) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockView, BlockPos pos, CollisionContext entityContext) {
		return CollisionDispatcher.shapeFor(XmBlockState.modelState(blockState, blockView, pos, true));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void animateTick(BlockState blockState, Level world, BlockPos blockPos, RandomSource random) {
		baseBlock.animateTick(blockState, world, blockPos, random);
	}

	@Override
	public void attack(BlockState blockState, Level world, BlockPos blockPos, Player playerEntity) {
		baseBlockState.attack(world, blockPos, playerEntity);
	}

	@Override
	public void destroy(LevelAccessor iWorld, BlockPos blockPos, BlockState blockState) {
		baseBlock.destroy(iWorld, blockPos, blockState);
	}

	@Override
	public float getExplosionResistance() {
		return baseBlock.getExplosionResistance();
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!state.is(state.getBlock())) {
			baseBlockState.neighborChanged(world, pos, Blocks.AIR, pos, false);
			baseBlock.onPlace(baseBlockState, world, pos, oldState, false);
		}
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean notify) {
		if (!state.is(newState.getBlock())) {
			baseBlockState.onRemove(world, pos, newState, notify);
		}
	}

	@Override
	public void stepOn(Level world, BlockPos blockPos, BlockState state, Entity entity) {
		baseBlock.stepOn(world, blockPos, state, entity);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return baseBlock.isRandomlyTicking(blockState);
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverWorld, BlockPos blockPos, RandomSource random) {
		baseBlock.randomTick(blockState, serverWorld, blockPos, random);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverWorld, BlockPos blockPos, RandomSource random) {
		baseBlock.tick(blockState, serverWorld, blockPos, random);
	}

	@Override
	public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player playerEntity, InteractionHand hand, BlockHitResult blockHitResult) {
		return baseBlockState.use(world, playerEntity, hand, blockHitResult);
	}

	@Override
	public void wasExploded(Level world, BlockPos blockPos, Explosion explosion) {
		baseBlock.wasExploded(world, blockPos, explosion);
	}

	//UGLY: It was bad in the previous versions, too.  There must be a better model for this, but I haven't found it yet.
	//TODO: consider splitting this mess into a utility class for reuse - like it was in prior version
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		final BlockPos pos = context.getClickedPos();
		final Player player = context.getPlayer();
		final FluidState fluidState = context.getLevel().getFluidState(pos);
		final Direction onFace = context.getClickedFace().getOpposite();
		BlockState result = defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

		Direction bottomFace = Direction.DOWN;
		Direction backFace = Direction.SOUTH;

		if (player != null) {
			final Direction[] faces = context.getNearestLookingDirections();
			final int xIndex = faces[0].getAxis() == Axis.X ? 0 : (faces[1].getAxis() == Axis.X ? 1 : 2);
			final int yIndex = faces[0].getAxis() == Axis.Y ? 0 : (faces[1].getAxis() == Axis.Y ? 1 : 2);
			final int zIndex = faces[0].getAxis() == Axis.Z ? 0 : (faces[1].getAxis() == Axis.Z ? 1 : 2);

			final boolean modKey = modKeyTest.test(player);
			final boolean forceKey = forceKeyTest.test(player);

			final Vec3 hit = context.getClickLocation();

			if (shape == Shape.STRAIGHT) {
				if (modKey) {
					// horizontal stairs
					if (onFace.getAxis() != Axis.Y) {
						bottomFace = onFace;

						if (forceKey) {
							backFace = WorldHelper.closestAdjacentFace(onFace, hit.x, hit.y, hit.z);
						} else {
							if (onFace.getAxis() == Axis.X) {
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
										: player.getDirection();
					}
				} else {
					// vertical (normal)
					if (onFace.getAxis() == Axis.Y) {
						bottomFace = onFace;
						backFace = forceKey
								? WorldHelper.closestAdjacentFace(onFace, hit.x, hit.y, hit.z)
										: player.getDirection();
					} else {
						backFace = onFace;

						if (forceKey) {
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
							final Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, (float) hit.x, (float) hit.y, (float) hit.z);
							bottomFace = pair.getLeft();
							final Direction rightFace = FaceEdge.fromWorld(onFace, bottomFace).counterClockwise().toWorld(bottomFace);
							backFace = rightFace == pair.getRight() ? onFace : pair.getRight();
						} else {
							bottomFace = player.getDirection();
							final int otherIndex = bottomFace.getAxis() == Axis.X ? zIndex : xIndex;
							final Direction otherFace = faces[otherIndex];
							final Direction rightFace = FaceEdge.fromWorld(onFace, bottomFace).counterClockwise().toWorld(bottomFace);
							backFace = rightFace == otherFace ? onFace : otherFace;
						}
					} else {
						// placed on bottom (horizontal) face directly
						bottomFace = onFace;

						if (forceKey) {
							final Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, (float) hit.x, (float) hit.y, (float) hit.z);
							boolean leftRightOrder = DirectionHelper.counterClockwise(pair.getLeft(), onFace.getAxis()) == pair.getRight();

							if (onFace.getAxisDirection() == AxisDirection.NEGATIVE) {
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
					if (forceKey) {
						if (onFace.getAxis() == Axis.Y) {
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
						final HorizontalEdge edge = HorizontalEdge.fromRotation(player.getYRot());
						backFace = bottomFace == Direction.DOWN ? edge.left.face : edge.right.face;
					}
				}
			}
		}

		result = result.setValue(XmProperties.ROTATION, ObjectUtils.defaultIfNull(CubeRotation.find(bottomFace, backFace), CubeRotation.DOWN_WEST));
		return result;
	}

	@Override
	public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
		if (blockState.getValue(WATERLOGGED).booleanValue()) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(XmProperties.ROTATION, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState blockState_1) {
		return blockState_1.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState_1);
	}

	@Override
	public boolean isPathfindable(BlockState blockState_1, BlockGetter blockView_1, BlockPos blockPos_1, PathComputationType blockPlacementEnvironment_1) {
		return false;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(XmProperties.ROTATION, state.getValue(XmProperties.ROTATION).rotate(rotation));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrir) {
		return state.rotate(Rotation.CLOCKWISE_180);
	}

	public static SimplePrimitiveStateMutator MODELSTATE_FROM_BLOCKSTATE = (modelState, blockState) -> {
		final Block rawBlock = blockState.getBlock();

		if (!(rawBlock instanceof final StairLike block)) {
			return modelState;
		}

		Stair.setCorner(block.shape != Shape.STRAIGHT, modelState);
		Stair.setInsideCorner(block.shape == Shape.INSIDE_CORNER, modelState);
		modelState.orientationIndex(blockState.getValue(XmProperties.ROTATION).ordinal());
		return modelState;
	};
}
