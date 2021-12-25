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

package grondag.xm.connect;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import grondag.xm.XmConfig;
import grondag.xm.api.connect.species.SpeciesFunction;
import grondag.xm.api.connect.species.SpeciesMode;
import grondag.xm.api.connect.species.SpeciesProperty;
import grondag.xm.api.connect.world.BlockRegion;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.BlockTestContext;
import grondag.xm.api.modelstate.ModelState;

@Internal
public class SpeciesImpl {
	private SpeciesImpl() { }

	public static int speciesForPlacement(BlockGetter world, BlockPos onPos, Direction onFace, SpeciesMode mode, SpeciesFunction func, BlockRegion region) {
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
		if (region == null) {
			region = BlockRegion.of(onPos.relative(onFace));
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
			&& ctx.fromBlockState().hasProperty(SpeciesProperty.SPECIES)
			&& ctx.fromBlockState().getValue(SpeciesProperty.SPECIES) == ctx.toBlockState().getValue(SpeciesProperty.SPECIES);
	}

	@SuppressWarnings("rawtypes")
	private static final BlockTest SAME_BLOCK_AND_SPECIES = SpeciesImpl::blockAndSpeciesTest;

	@SuppressWarnings("unchecked")
	public static <T extends ModelState> BlockTest<T> sameBlockAndSpecies() {
		return SAME_BLOCK_AND_SPECIES;
	}
}
