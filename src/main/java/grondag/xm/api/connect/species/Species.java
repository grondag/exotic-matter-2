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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.fermion.position.BlockRegion;
import grondag.xm.connect.SpeciesImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
public class Species {
    private  Species() {}

    public static int speciesForPlacement(BlockView world, BlockPos onPos, Direction onFace, SpeciesMode mode, SpeciesFunction func) {
        return SpeciesImpl.speciesForPlacement(world, onPos, onFace, mode, func, null);
    }

    public static int speciesForPlacement(BlockView world, BlockPos onPos, Direction onFace, SpeciesMode mode, SpeciesFunction func, BlockRegion region) {
        return SpeciesImpl.speciesForPlacement(world, onPos, onFace, mode, func, region);
    }
}
