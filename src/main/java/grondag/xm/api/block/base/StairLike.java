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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;

import grondag.fermion.modkeys.impl.ModKeysAccess;
import grondag.fermion.world.WorldHelper;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.block.XmProperties;
import grondag.xm.api.collision.CollisionDispatcher;
import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import grondag.xm.api.orientation.CubeRotation;
import grondag.xm.api.orientation.FaceEdge;
import grondag.xm.api.orientation.HorizontalEdge;
import grondag.xm.api.primitive.simple.Stair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlacementEnvironment;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateFactory;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

@API(status = EXPERIMENTAL)
public class StairLike extends Block implements Waterloggable {
    protected final Block baseBlock;
    protected final BlockState baseBlockState;
    
    public static enum Shape {
        STRAIGHT,
        INSIDE_CORNER,
        OUTSIDE_CORNER;
    }
    
    public final Shape shape;
    
    public StairLike(BlockState blockState, Settings settings, Shape shape) {
        super(FabricBlockSettings.copyOf(settings).dynamicBounds().build());
        this.setDefaultState(this.stateFactory.getDefaultState()
                .with(WATERLOGGED, false));
        this.baseBlock = blockState.getBlock();
        this.baseBlockState = blockState;
        this.shape = shape;
    }

    @Override
    public boolean hasSidedTransparency(BlockState blockState_1) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos pos, EntityContext entityContext) {
        return CollisionDispatcher.shapeFor(XmBlockState.modelState(blockState, blockView, pos, true));
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState blockState_1, World world_1, BlockPos blockPos_1, Random random_1) {
        this.baseBlock.randomDisplayTick(blockState_1, world_1, blockPos_1, random_1);
    }

    @Override
    public void onBlockBreakStart(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1) {
        this.baseBlockState.onBlockBreakStart(world_1, blockPos_1, playerEntity_1);
    }

    @Override
    public void onBroken(IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1) {
        this.baseBlock.onBroken(iWorld_1, blockPos_1, blockState_1);
    }

    @Override
    public float getBlastResistance() {
        return this.baseBlock.getBlastResistance();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return this.baseBlock.getRenderLayer();
    }

    @Override
    public int getTickRate(ViewableWorld viewableWorld_1) {
        return this.baseBlock.getTickRate(viewableWorld_1);
    }

    @Override
    public void onBlockAdded(BlockState blockState, World world, BlockPos pos, BlockState blockStateOther, boolean notify) {
        if (blockState.getBlock() != blockStateOther.getBlock()) {
            this.baseBlockState.neighborUpdate(world, pos, Blocks.AIR, pos, false);
            this.baseBlock.onBlockAdded(this.baseBlockState, world, pos, blockStateOther, false);
        }
    }

    @Override
    public void onBlockRemoved(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean notify) {
        if (newState.getBlock() != oldState.getBlock()) {
            this.baseBlockState.onBlockRemoved(world, pos, oldState, notify);
        }
    }

    @Override
    public void onSteppedOn(World world_1, BlockPos blockPos_1, Entity entity_1) {
        this.baseBlock.onSteppedOn(world_1, blockPos_1, entity_1);
    }

    @Override
    public void onScheduledTick(BlockState blockState_1, World world_1, BlockPos blockPos_1, Random random_1) {
        this.baseBlock.onScheduledTick(blockState_1, world_1, blockPos_1, random_1);
    }

    @Override
    public boolean activate(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1, Hand hand_1, BlockHitResult blockHitResult_1) {
        return this.baseBlockState.activate(world_1, playerEntity_1, hand_1, blockHitResult_1);
    }

    @Override
    public void onDestroyedByExplosion(World world_1, BlockPos blockPos_1, Explosion explosion_1) {
        this.baseBlock.onDestroyedByExplosion(world_1, blockPos_1, explosion_1);
    }

    //UGLY: It was bad in the previous versions, too.  There must be a better model for this, but I haven't found it yet.
    //TODO: consider splitting this mess into a utility class for reuse - like it was in prior version
    //TODO: make modifier key mappings configurable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        final BlockPos pos = context.getBlockPos();
        final PlayerEntity player = context.getPlayer();
        final FluidState fluidState = context.getWorld().getFluidState(pos);
        final Direction onFace = context.getSide().getOpposite();
        BlockState result = this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        
        Direction bottomFace = Direction.DOWN;
        Direction backFace = Direction.SOUTH;
        if(player != null) {
            final Direction[] faces = context.getPlacementDirections();
            final int xIndex = faces[0].getAxis() == Axis.X ? 0 : (faces[1].getAxis() == Axis.X ? 1 : 2);
            final int yIndex = faces[0].getAxis() == Axis.Y ? 0 : (faces[1].getAxis() == Axis.Y ? 1 : 2);
            final int zIndex = faces[0].getAxis() == Axis.Z ? 0 : (faces[1].getAxis() == Axis.Z ? 1 : 2);
            
            final boolean modKey = ModKeysAccess.isSuperPressed(player);
            final boolean forceKey = ModKeysAccess.isControlPressed(player);
            
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
                            Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, (float)hit.x, (float)hit.y, (float)hit.z);
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
                            Pair<Direction, Direction> pair = WorldHelper.closestAdjacentFaces(onFace, (float)hit.x, (float)hit.y, (float)hit.z);
                            boolean leftRightOrder = pair.getLeft().rotateClockwise(onFace.getAxis()) == pair.getRight();
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
                        HorizontalEdge edge = HorizontalEdge.fromRotation(player.yaw);
                        backFace = bottomFace == Direction.DOWN ? edge.left.face : edge.right.face;
                    }
                }
            }
        }
        result = result.with(XmProperties.ROTATION, ObjectUtils.defaultIfNull(CubeRotation.find(bottomFace, backFace), CubeRotation.DOWN_WEST));
        return result;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState blockState_1, Direction direction_1, BlockState blockState_2, IWorld iWorld_1, BlockPos blockPos_1, BlockPos blockPos_2) {
        if ((Boolean)blockState_1.get(WATERLOGGED)) {
            iWorld_1.getFluidTickScheduler().schedule(blockPos_1, Fluids.WATER, Fluids.WATER.getTickRate(iWorld_1));
        }
        return super.getStateForNeighborUpdate(blockState_1, direction_1, blockState_2, iWorld_1, blockPos_1, blockPos_2);
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        builder.add(XmProperties.ROTATION, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState blockState_1) {
        return (Boolean)blockState_1.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(blockState_1);
    }

    @Override
    public boolean canPlaceAtSide(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, BlockPlacementEnvironment blockPlacementEnvironment_1) {
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
        
        StairLike block = (StairLike)rawBlock;
        Stair.setCorner(block.shape != Shape.STRAIGHT, modelState);
        Stair.setInsideCorner(block.shape == Shape.INSIDE_CORNER, modelState);
        modelState.orientationIndex(blockState.get(XmProperties.ROTATION).ordinal());
        return modelState;
    };
}
