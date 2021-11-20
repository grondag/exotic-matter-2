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
