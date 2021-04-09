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

package grondag.xm.connect;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import grondag.fermion.position.BlockRegion;
import grondag.xm.XmConfig;
import grondag.xm.api.connect.species.SpeciesFunction;
import grondag.xm.api.connect.species.SpeciesMode;
import grondag.xm.api.connect.species.SpeciesProperty;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.BlockTestContext;
import grondag.xm.api.modelstate.ModelState;

@Internal
public class SpeciesImpl {
	private SpeciesImpl() {}

	public static int speciesForPlacement(BlockView world, BlockPos onPos, Direction onFace, SpeciesMode mode, SpeciesFunction func, BlockRegion region) {
		// ways this can happen:
		// have a species we want to match because we clicked on a face
		// break with everything - need to know adjacent species
		// match with most - need to know adjacent species

		final boolean shouldBreak = mode != SpeciesMode.MATCH_MOST;

		// if no region provided or species mode used clicked block then
		// result is based on the clicked face
		if (((mode == SpeciesMode.MATCH_CLICKED || mode == SpeciesMode.MATCH_MOST) && onPos != null && onFace != null)) {
			final int clickedSpecies = func.species(world, onPos);

			if (clickedSpecies >= 0) {
				return clickedSpecies;
			}
		}

		// PERF: avoid allocation - but not urgent; not hot
		if(region == null) {
			region = BlockRegion.of(onPos.offset(onFace));
		}
		final int[] adjacentCount = new int[16];
		final int[] surfaceCount = new int[16];

		/** limit block positions checked for very large regions */
		int checkCount = 0;

		for (final BlockPos pos : region.adjacentPositions()) {
			final int adjacentSpecies = func.species(world, pos);
			if (adjacentSpecies >= 0 && adjacentSpecies <= 15) {
				adjacentCount[adjacentSpecies]++;
			}
			if (checkCount++ >= XmConfig.maxPlacementCheckCount) {
				break;
			}
		}

		for (final BlockPos pos : region.surfacePositions()) {
			final int interiorSpecies = func.species(world, pos);
			if (interiorSpecies >= 0 && interiorSpecies <= 15) {
				surfaceCount[interiorSpecies]++;
			}
			if (checkCount++ >= XmConfig.maxPlacementCheckCount) {
				break;
			}
		}

		if (shouldBreak) {
			// find a species that matches as few things as possible
			int bestSpecies = 0;
			int bestCount = adjacentCount[0] + surfaceCount[0];

			for (int i = 1; i < 16; i++) {
				final int tryCount = adjacentCount[i] + surfaceCount[i];
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
				&& ctx.fromBlockState().contains(SpeciesProperty.SPECIES)
				&& ctx.fromBlockState().get(SpeciesProperty.SPECIES) == ctx.toBlockState().get(SpeciesProperty.SPECIES);
	}

	@SuppressWarnings("rawtypes")
	private static final BlockTest SAME_BLOCK_AND_SPECIES = SpeciesImpl::blockAndSpeciesTest;

	@SuppressWarnings("unchecked")
	public static <T extends ModelState> BlockTest<T> sameBlockAndSpecies() {
		return SAME_BLOCK_AND_SPECIES;
	}
}
