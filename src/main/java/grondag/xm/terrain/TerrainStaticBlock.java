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

package grondag.xm.terrain;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.init.XmPrimitives;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.World;

public class TerrainStaticBlock extends Block implements IHotBlock {

    public TerrainStaticBlock(Settings blockSettings, ModelState defaultModelState, BlockEntityType<?> blockEntityType, boolean isFiller) {
        super(blockSettings); //, adjustShape(defaultModelState, isFiller), blockEntityType);
    }

    // PERF: sucks
    @SuppressWarnings("unused")
    private static ModelState.Mutable adjustShape(ModelState stateIn, boolean isFiller) {
        ModelState.Mutable result = isFiller ? XmPrimitives.TERRAIN_FILLER.newState() : XmPrimitives.TERRAIN_HEIGHT.newState();
        result.copyFrom(stateIn);
        result.setStatic(true);
        return result;
    }

//  TODO: remove or restore
//    @Override
//    public int quantityDropped(ExtendedBlockView world, BlockPos pos, BlockState state) {
//        double volume = 0;
//        ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, state, pos, true);
//        for (BoundingBox box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState)) {
//            volume += Useful.volumeAABB(box);
//        }
//
//        return (int) Math.min(9, volume * 9);
//    }

    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them
     * static.
     */
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        TerrainDynamicBlock.freezeNeighbors(world, pos, state);
        super.onBreak(world, pos, state, player);
    }

    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them
     * static.
     */
    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        TerrainDynamicBlock.freezeNeighbors(worldIn, pos, state);
    }

    /**
     * Convert this block to a dynamic version of itself if one is known.
     */
    public void makeDynamic(BlockState state, World world, BlockPos pos) {
        BlockState newState = dynamicState(state, world, pos);
        if (newState != state)
            world.setBlockState(pos, newState, 3);
    }

    /**
     * Returns dynamic version of self if one is known. Otherwise returns self.
     */
    public BlockState dynamicState(BlockState state, ExtendedBlockView world, BlockPos pos) {
        Block dynamicVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getDynamicBlock(this);
        if (dynamicVersion == null || state.getBlock() != this)
            return state;
        // TODO: transfer heat block state?
        return dynamicVersion.getDefaultState().with(TerrainBlock.TERRAIN_TYPE, state.get(TerrainBlock.TERRAIN_TYPE));
    }

    public void setModelState(World world, BlockPos pos, ModelState.Mutable myModelState) {
        // TODO Auto-generated method stub
        
    }
}
