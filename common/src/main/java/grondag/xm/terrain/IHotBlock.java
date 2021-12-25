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
