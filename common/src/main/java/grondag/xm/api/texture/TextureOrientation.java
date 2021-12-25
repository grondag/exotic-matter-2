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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.orientation.api.ClockwiseRotation;
import grondag.xm.texture.TextureOrientationHelper;

// TODO: Restore RANDOM_CONSISTENT to distinguish multi-block textures that can and can't have rotations intermingled per-plane.
// As is, have to assume can't, but 2x2 cobble would be an example that could.
@Experimental
public enum TextureOrientation {
	IDENTITY(ClockwiseRotation.ROTATE_NONE, false, false),
	ROTATE_90(ClockwiseRotation.ROTATE_90, false, false),
	ROTATE_180(ClockwiseRotation.ROTATE_180, false, false),
	ROTATE_270(ClockwiseRotation.ROTATE_270, false, false),

	FLIP_U(ClockwiseRotation.ROTATE_NONE, true, false),
	FLIP_U_ROTATE_90(ClockwiseRotation.ROTATE_90, true, false),
	FLIP_U_ROTATE_180(ClockwiseRotation.ROTATE_180, true, false),
	FLIP_U_ROTATE_270(ClockwiseRotation.ROTATE_270, true, false),

	FLIP_V(ClockwiseRotation.ROTATE_NONE, false, true),
	FLIP_V_ROTATE_90(ClockwiseRotation.ROTATE_90, false, true),
	FLIP_V_ROTATE_180(ClockwiseRotation.ROTATE_180, false, true),
	FLIP_V_ROTATE_270(ClockwiseRotation.ROTATE_270, false, true),

	FLIP_UV(ClockwiseRotation.ROTATE_NONE, true, true),
	FLIP_UV_ROTATE_90(ClockwiseRotation.ROTATE_90, true, true),
	FLIP_UV_ROTATE_180(ClockwiseRotation.ROTATE_180, true, true),
	FLIP_UV_ROTATE_270(ClockwiseRotation.ROTATE_270, true, true);

	public final ClockwiseRotation rotation;
	public final boolean flipU;
	public final boolean flipV;

	TextureOrientation(ClockwiseRotation rotation, boolean flipU, boolean flipV) {
		this.rotation = rotation;
		this.flipU = flipU;
		this.flipV = flipV;
	}

	public TextureOrientation clockwise() {
		return TextureOrientationHelper.clockwise(this);
	}

	public TextureOrientation clockwise(int offset) {
		return TextureOrientationHelper.clockwise(this, offset);
	}

	public static TextureOrientation find(ClockwiseRotation rotation, boolean flipU, boolean flipV) {
		return TextureOrientationHelper.find(rotation, flipU, flipV);
	}

	public static TextureOrientation fromOrdinal(int ordinal) {
		return TextureOrientationHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<TextureOrientation> consumer) {
		TextureOrientationHelper.forEach(consumer);
	}
}
