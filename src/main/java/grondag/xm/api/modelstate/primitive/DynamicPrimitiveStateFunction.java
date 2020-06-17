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
package grondag.xm.api.modelstate.primitive;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.modelstate.WorldToModelStateImpl;

@API(status = EXPERIMENTAL)
public interface DynamicPrimitiveStateFunction extends PrimitiveStateFunction {
	static DynamicPrimitiveStateFunction ofDefaultState(PrimitiveState defaultState) {
		return builder().withDefaultState(defaultState).build();
	}

	static Builder builder() {
		return WorldToModelStateImpl.builder();
	}

	PrimitiveState getDefaultState();

	void setDefaultState(PrimitiveState state);

	public interface Builder extends PrimitiveStateFunction.Builder {
		@Override
		Builder withJoin(BlockTest<PrimitiveState> joinTest);

		@Override
		Builder withUpdate(PrimitiveStateMutator update);

		@Override
		DynamicPrimitiveStateFunction build();

		@Override
		Builder clear();

		@Override
		Builder withDefaultState(PrimitiveState defaultState);
	}
}
