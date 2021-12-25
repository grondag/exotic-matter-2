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

package grondag.xm.texture;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.orientation.api.ClockwiseRotation;

@Internal
public class TextureOrientationHelper {
	private TextureOrientationHelper() {
	}

	private static final TextureOrientation[] VALUES = TextureOrientation.values();
	public static final int COUNT = VALUES.length;
	private static final TextureOrientation[] LOOKUP = new TextureOrientation[4 * 2 * 2];

	static {
		for (final TextureOrientation o : VALUES) {
			LOOKUP[index(o)] = o;
		}
	}

	private static int index(TextureOrientation o) {
		return index(o.rotation, o.flipU, o.flipV);
	}

	private static int index(ClockwiseRotation r, boolean flipU, boolean flipV) {
		return (r.ordinal() << 2) | (flipU ? 1 : 0) | (flipV ? 2 : 0);
	}

	public static TextureOrientation find(ClockwiseRotation rotation, boolean flipU, boolean flipV) {
		if (rotation == null) {
			rotation = ClockwiseRotation.ROTATE_NONE;
		}

		return LOOKUP[index(rotation, flipU, flipV)];
	}

	public static final TextureOrientation fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<TextureOrientation> consumer) {
		for (final TextureOrientation val : VALUES) {
			consumer.accept(val);
		}
	}

	public static TextureOrientation clockwise(TextureOrientation o) {
		return find(o.rotation.clockwise(), o.flipU, o.flipV);
	}

	public static TextureOrientation clockwise(TextureOrientation o, int offset) {
		return find(o.rotation.clockwise(offset), o.flipU, o.flipV);
	}
}
