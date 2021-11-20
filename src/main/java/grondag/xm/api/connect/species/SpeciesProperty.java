/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.connect.species;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import grondag.xm.connect.SpeciesImpl;

@Experimental
public class SpeciesProperty {
	private SpeciesProperty() { }

	public static final IntegerProperty SPECIES = IntegerProperty.create("xm_species", 0, 15);

	public static SimplePrimitiveStateMutator SPECIES_MODIFIER = (modelState, blockState) -> {
		final int species = blockState.getValue(SPECIES);
		modelState.species(species);
		return modelState;
	};

	public static SpeciesFunction speciesForBlock(final Block block) {
		return (world, blockState, pos) -> {
			if (blockState.getBlock() == block) {
				final Comparable<?> result = blockState.getValues().get(SPECIES);
				return result == null ? SpeciesFunction.NO_SPECIES : (Integer) result;
			} else {
				return SpeciesFunction.NO_SPECIES;
			}
		};
	}

	public static SpeciesFunction speciesForBlockType(Class<?> clazz) {
		return (world, blockState, pos) -> {
			if (clazz.isInstance(blockState.getBlock())) {
				final Comparable<?> result = blockState.getValues().get(SPECIES);
				return result == null ? SpeciesFunction.NO_SPECIES : (Integer) result;
			} else {
				return SpeciesFunction.NO_SPECIES;
			}
		};
	}

	/** True when blocks are same block and same species property. */
	public static <T extends ModelState> BlockTest<T> matchBlockAndSpecies() {
		return SpeciesImpl.sameBlockAndSpecies();
	}
}
