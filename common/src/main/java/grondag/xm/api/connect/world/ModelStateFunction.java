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

package grondag.xm.api.connect.world;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;

/**
 * Provide an instance to {@link BlockNeighbors} when it is retrieved in order
 * to retrieve values with lazy evaluation and caching. The resulting values
 * will also be provided to {@link BlockTest}.
 */
@Experimental
@FunctionalInterface
public interface ModelStateFunction {
	ModelState get(BlockGetter world, BlockState blockState, BlockPos pos);

	/**
	 * Use this as factory for model state block tests that DON'T need to refresh
	 * from world.
	 */
	ModelStateFunction STATIC = (w, b, p) -> XmBlockState.modelState(b, w, p, false);
	/**
	 * Use this as factory for model state block tests that DO need to refresh from
	 * world.
	 */
	ModelStateFunction DYNAMIC = (w, b, p) -> XmBlockState.modelState(b, w, p, true);
}
