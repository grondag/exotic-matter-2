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
package grondag.xm.api.texture;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fermion.orientation.api.ClockwiseRotation;
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

	public static TextureOrientation find(ClockwiseRotation rotation, boolean flipU, boolean flipV) {
		return TextureOrientationHelper.find(rotation, flipU, flipV);
	}

	public static final TextureOrientation fromOrdinal(int ordinal) {
		return TextureOrientationHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<TextureOrientation> consumer) {
		TextureOrientationHelper.forEach(consumer);
	}
}
