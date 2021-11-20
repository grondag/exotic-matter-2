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

/**
 * Terrain state is longer than 64 bits and we don't always want/need to
 * instantiate a TerrainState object to pass the raw bits around. Implement this
 * for objects that can consume the raw state directly. (Including terrain
 * state.)
 */
@Internal
@FunctionalInterface
public interface ITerrainBitConsumer<T> {
	T apply(long terrainBits, int hotnessBits);
}
