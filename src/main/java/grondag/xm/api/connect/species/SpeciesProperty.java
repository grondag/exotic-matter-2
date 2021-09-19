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

package grondag.xm.api.connect.species;

import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.primitive.SimplePrimitiveStateMutator;
import grondag.xm.connect.SpeciesImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@Experimental
public class SpeciesProperty {
	private SpeciesProperty() {}

	public static final IntegerProperty SPECIES = IntegerProperty.create("xm_species", 0, 15);

	public static SimplePrimitiveStateMutator SPECIES_MODIFIER = (modelState, blockState) -> {
		final int species = blockState.getValue(SPECIES);
		modelState.species(species);
		return modelState;
	};

	public static SpeciesFunction speciesForBlock(final Block block) {
		return (world, blockState, pos) -> {
			if(blockState.getBlock() == block) {
				final Comparable<?> result = blockState.getValues().get(SPECIES);
				return result == null ? SpeciesFunction.NO_SPECIES : (Integer) result;
			} else
				return SpeciesFunction.NO_SPECIES;
		};
	}

	public static SpeciesFunction speciesForBlockType(Class<?> clazz) {
		return (world, blockState, pos) -> {
			if(clazz.isInstance(blockState.getBlock())) {
				final Comparable<?> result = blockState.getValues().get(SPECIES);
				return result == null ? SpeciesFunction.NO_SPECIES : (Integer) result;
			} else
				return SpeciesFunction.NO_SPECIES;
		};
	}

	/** True when blocks are same block and same species property */
	public static <T extends ModelState> BlockTest<T> matchBlockAndSpecies() {
		return SpeciesImpl.sameBlockAndSpecies();
	}
}
