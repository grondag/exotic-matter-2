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

package grondag.xm.orientation.api;

import java.util.Locale;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;

import grondag.xm.orientation.impl.HorizontalFaceHelper;

/**
 * A subset of {@link Direction}, includes only the face in the horizontal
 * plane.
 */
@Experimental
public enum HorizontalFace implements StringRepresentable {
	NORTH(Direction.NORTH),
	EAST(Direction.EAST),
	SOUTH(Direction.SOUTH),
	WEST(Direction.WEST);

	public final Direction face;
	private final String serializedName;
	public final Vec3i vector;

	HorizontalFace(Direction face) {
		serializedName = name().toLowerCase(Locale.ROOT);
		this.face = face;
		vector = face.getNormal();
	}

	public HorizontalFace left() {
		return ordinal() == 0 ? HorizontalFace.values()[3] : HorizontalFace.values()[ordinal() - 1];
	}

	public HorizontalFace right() {
		return ordinal() == 3 ? HorizontalFace.values()[0] : HorizontalFace.values()[ordinal() + 1];
	}

	public static final int COUNT = HorizontalFaceHelper.COUNT;

	/**
	 * Will return null if input is not a horizontal face.
	 */
	@Nullable
	public static HorizontalFace find(Direction face) {
		return HorizontalFaceHelper.find(face);
	}

	public static HorizontalFace fromOrdinal(int ordinal) {
		return HorizontalFaceHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<HorizontalFace> consumer) {
		HorizontalFaceHelper.forEach(consumer);
	}

	@Override
	public String getSerializedName() {
		return serializedName;
	}
}
