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

package grondag.xm.api.texture;

import grondag.xm.orientation.api.ClockwiseRotation;

public enum TextureTransform {
	IDENTITY(ClockwiseRotation.ROTATE_NONE, false),
	ROTATE_90(ClockwiseRotation.ROTATE_90, false),
	ROTATE_180(ClockwiseRotation.ROTATE_180, false),
	ROTATE_270(ClockwiseRotation.ROTATE_270, false),
	ROTATE_RANDOM(ClockwiseRotation.ROTATE_NONE, true),
	/** Use for tiles that must remain consistent for the same species. */
	ROTATE_BIGTEX(ClockwiseRotation.ROTATE_NONE, true),
	/** Rotate 180 and allow horizontal texture flip. */
	STONE_LIKE(ClockwiseRotation.ROTATE_NONE, true),
	DIAGONAL(ClockwiseRotation.ROTATE_NONE, false),;

	public final ClockwiseRotation baseRotation;
	public final boolean hasRandom;

	TextureTransform(ClockwiseRotation baseRotation, boolean hasRandom) {
		this.baseRotation = baseRotation;
		this.hasRandom = hasRandom;
	}
}
