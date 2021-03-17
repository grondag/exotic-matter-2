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
package grondag.xm.terrain;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface IHotBlock {
	/**
	 * Highest heat value that can be returned from {@link #heatLevel()}.
	 * Corresponds to molten lava.
	 */
	int MAX_HEAT = 5;

	/**
	 * Count of allowed values returned from {@link #heatLevel()}, including zero.
	 * Equivalently, {@link #MAX_HEAT} + 1;
	 */
	int HEAT_LEVEL_COUNT = MAX_HEAT + 1;

	default int heatLevel() {
		return 0;
	}

	default boolean isHot() {
		return heatLevel() != 0;
	}

}
