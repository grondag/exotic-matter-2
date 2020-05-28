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
package grondag.xm.texture;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.fermion.orientation.api.ClockwiseRotation;
import grondag.xm.api.texture.TextureOrientation;

@API(status = INTERNAL)
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
		if(rotation == null) {
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
}
