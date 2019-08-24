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
package grondag.xm.api.block;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Predicate;

import org.apiguardian.api.API;

import grondag.fermion.position.BlockRegion;
import grondag.xm.XmConfig;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.BlockTestContext;
import grondag.xm.api.modelstate.ModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
public class SpeciesHelper {
    private  SpeciesHelper() {}
    
    /**
     * Returns species at position if it could join with the given block/modelState
     * Returns -1 if no XM block at position or if join not possible.
     */
    public static int getJoinableSpecies(BlockView world, BlockPos pos, Predicate<BlockState> joinTest) {
        BlockState state = world.getBlockState(pos);
        if(joinTest.test(state)) {
            return state.get(XmProperties.SPECIES);
        } else {
            return -1;
        }
    }
    
    public static int speciesForPlacement(BlockView world, BlockPos onPos, Direction onFace, SpeciesMode mode, Predicate<BlockState> joinTest) {
        return speciesForPlacement(world, onPos, onFace, mode, joinTest, null);
    }
    
    public static int speciesForPlacement(BlockView world, BlockPos onPos, Direction onFace, SpeciesMode mode, Predicate<BlockState> joinTest, BlockRegion region) {
        // ways this can happen:
        // have a species we want to match because we clicked on a face
        // break with everything - need to know adjacent species
        // match with most - need to know adjacent species

        boolean shouldBreak = mode != SpeciesMode.MATCH_MOST;

        // if no region provided or species mode used clicked block then
        // result is based on the clicked face
        if (((mode == SpeciesMode.MATCH_CLICKED || mode == SpeciesMode.MATCH_MOST) && onPos != null && onFace != null)) {
            int clickedSpecies = getJoinableSpecies(world, onPos, joinTest);

            if (clickedSpecies >= 0)
                return clickedSpecies;
        }

        // PERF: avoid allocation - but not urgent; not hot
        if(region == null) {
            region = BlockRegion.of(onPos.offset(onFace));
        }
        int[] adjacentCount = new int[16];
        int[] surfaceCount = new int[16];

        /** limit block positions checked for very large regions */
        int checkCount = 0;

        for (BlockPos pos : region.adjacentPositions()) {
            int adjacentSpecies = SpeciesHelper.getJoinableSpecies(world, pos, joinTest);
            if (adjacentSpecies >= 0 && adjacentSpecies <= 15)
                adjacentCount[adjacentSpecies]++;
            if (checkCount++ >= XmConfig.BLOCKS.maxPlacementCheckCount)
                break;
        }

        for (BlockPos pos : region.surfacePositions()) {
            int interiorSpecies = SpeciesHelper.getJoinableSpecies(world, pos, joinTest);
            if (interiorSpecies >= 0 && interiorSpecies <= 15)
                surfaceCount[interiorSpecies]++;
            if (checkCount++ >= XmConfig.BLOCKS.maxPlacementCheckCount)
                break;
        }

        if (shouldBreak) {
            // find a species that matches as few things as possible
            int bestSpecies = 0;
            int bestCount = adjacentCount[0] + surfaceCount[0];

            for (int i = 1; i < 16; i++) {
                int tryCount = adjacentCount[i] + surfaceCount[i];
                if (tryCount < bestCount) {
                    bestCount = tryCount;
                    bestSpecies = i;
                }
            }
            return bestSpecies;
        } else {
            // find the most common species and match with that
            // give preference to species that are included in the region surface if any
            int bestSpecies = 0;
            int bestCount = surfaceCount[0];

            for (int i = 1; i < 16; i++) {
                if (surfaceCount[i] > bestCount) {
                    bestCount = surfaceCount[i];
                    bestSpecies = i;
                }
            }

            if (bestCount == 0) {
                for (int i = 1; i < 16; i++) {
                    if (adjacentCount[i] > bestCount) {
                        bestCount = adjacentCount[i];
                        bestSpecies = i;
                    }
                }
            }
            return bestSpecies;
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static boolean blockAndSpeciesTest(BlockTestContext ctx) {
        return ctx.fromBlockState().getBlock() == ctx.toBlockState().getBlock()
                && ctx.fromBlockState().get(XmProperties.SPECIES) == ctx.toBlockState().get(XmProperties.SPECIES);
    }
    
    @SuppressWarnings("rawtypes")
    public static BlockTest SAME_BLOCK_AND_SPECIES = SpeciesHelper::blockAndSpeciesTest;
        
    
    /** True when blocks are same block and same species property */
    @SuppressWarnings("unchecked")
    public static <T extends ModelState> BlockTest<T> sameBlockAndSpecies() {
        return SAME_BLOCK_AND_SPECIES;
    }
}
