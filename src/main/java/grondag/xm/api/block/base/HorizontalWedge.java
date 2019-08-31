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

import org.apiguardian.api.API;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.block.XmProperties;
import grondag.xm.api.collision.CollisionDispatcher;
import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import grondag.xm.api.orientation.HorizontalEdge;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

@API(status = EXPERIMENTAL)
public class HorizontalWedge extends Block implements Waterloggable {
    protected final Block baseBlock;
    protected final BlockState baseBlockState;
    
    
    public HorizontalWedge(BlockState blockState) {
        super(FabricBlockSettings.copy(blockState.getBlock()).dynamicBounds().build());
        this.setDefaultState(this.stateFactory.getDefaultState().with(XmProperties.HORIZONTAL_EDGE, HorizontalEdge.SOUTH_WEST).with(WATERLOGGED, false));
        this.baseBlock = blockState.getBlock();
        this.baseBlockState = blockState;
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

    @Override
    public BlockState getPlacementState(ItemPlacementContext itemPlacementContext) {
        final FluidState fluidState = itemPlacementContext.getWorld().getFluidState(itemPlacementContext.getBlockPos());
        final Direction[] faces = itemPlacementContext.getPlacementDirections();
        final Direction face1 = faces[0] == Direction.EAST || faces[1] == Direction.EAST || faces[2] == Direction.EAST ? Direction.EAST : Direction.WEST;
        final Direction face2 = faces[0] == Direction.NORTH || faces[1] == Direction.NORTH || faces[2] == Direction.NORTH ? Direction.NORTH : Direction.SOUTH;
        
        return this.getDefaultState()
                .with(XmProperties.HORIZONTAL_EDGE, HorizontalEdge.find(face1, face2))
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState blockState_1, Direction direction_1, BlockState blockState_2, IWorld iWorld_1, BlockPos blockPos_1, BlockPos blockPos_2) {
        if ((Boolean)blockState_1.get(WATERLOGGED)) {
            iWorld_1.getFluidTickScheduler().schedule(blockPos_1, Fluids.WATER, Fluids.WATER.getTickRate(iWorld_1));
        }
        return super.getStateForNeighborUpdate(blockState_1, direction_1, blockState_2, iWorld_1, blockPos_1, blockPos_2);
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> stateFactory$Builder_1) {
        stateFactory$Builder_1.add(XmProperties.HORIZONTAL_EDGE, WATERLOGGED);
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
        return state.with(XmProperties.HORIZONTAL_EDGE, state.get(XmProperties.HORIZONTAL_EDGE).rotate(rotation));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(BlockRotation.CLOCKWISE_180);
    }

    public static SimplePrimitiveStateMutator MODELSTATE_MUTATOR = XmProperties.HORIZONTAL_EDGE_MODIFIER;
}
