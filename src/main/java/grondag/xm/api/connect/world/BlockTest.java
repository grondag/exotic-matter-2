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

package grondag.xm.api.connect.world;

import grondag.xm.api.modelstate.ModelState;

/**
 * Implement to define when a block neighbor should be considered "present and
 * matching" for purposes of computing a join state. Can also be used for more
 * general purposes to retrieve values with caching and lazy evaluation via
 * {@link BlockNeighbors#result(grondag.xm2.connect.api.model.BlockCorner)} and
 * its variance.
 *
 * <p>The "from" values will always be derived from the central block. The "to"
 * values will represent a neighboring block. All inputs benefit from the lazy
 * evaluation and caching provided by {@link BlockNeighbors}.
 *
 * <p>The model state values will be non-null if {@link BlockNeighbors} was given a
 * state function when it was retrieved. (And if the function returns a non-null
 * value.) See {@link ModelStateFunction}
 */
@FunctionalInterface
public interface BlockTest<T extends ModelState> {
	boolean apply(BlockTestContext<T> context);

	@SuppressWarnings("rawtypes") BlockTest SAME_BLOCK = ctx -> ctx.fromBlockState().getBlock() == ctx.toBlockState().getBlock();

	/** True when blocks are same instance. */
	@SuppressWarnings("unchecked")
	static <T extends ModelState> BlockTest<T> sameBlock() {
		return SAME_BLOCK;
	}
}
