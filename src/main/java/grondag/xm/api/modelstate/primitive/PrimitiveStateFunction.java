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

package grondag.xm.api.modelstate.primitive;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.modelstate.ModelStateFunction;
import grondag.xm.modelstate.WorldToModelStateImpl;

@Experimental
@FunctionalInterface
public interface PrimitiveStateFunction extends ModelStateFunction<MutablePrimitiveState> {
	static PrimitiveStateFunction ofDefaultState(PrimitiveState defaultState) {
		return builder().withDefaultState(defaultState).build();
	}

	static Builder builder() {
		return WorldToModelStateImpl.builder();
	}

	public interface Builder {
		Builder withJoin(BlockTest<PrimitiveState> joinTest);

		Builder withUpdate(PrimitiveStateMutator update);

		PrimitiveStateFunction build();

		Builder clear();

		Builder withDefaultState(PrimitiveState defaultState);
	}
}
