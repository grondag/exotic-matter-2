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

package grondag.xm.orientation.impl;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

import grondag.xm.orientation.api.HorizontalFace;

@Internal
public abstract class HorizontalFaceHelper {
	private HorizontalFaceHelper() {
	}

	private static final HorizontalFace[] VALUES = HorizontalFace.values();
	public static final int COUNT = VALUES.length;

	private static final HorizontalFace[] HORIZONTAL_FACE_LOOKUP = new HorizontalFace[6];

	static {
		for (final HorizontalFace hFace : HorizontalFace.values()) {
			HORIZONTAL_FACE_LOOKUP[hFace.face.ordinal()] = hFace;
		}
	}

	public static HorizontalFace find(Direction face) {
		return HORIZONTAL_FACE_LOOKUP[face.ordinal()];
	}

	public static final HorizontalFace fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<HorizontalFace> consumer) {
		for (final HorizontalFace val : VALUES) {
			consumer.accept(val);
		}
	}
}
