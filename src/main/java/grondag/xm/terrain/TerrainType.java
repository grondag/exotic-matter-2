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

import java.util.Locale;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.StringRepresentable;

@Internal
public enum TerrainType implements StringRepresentable {
	FILL_UP_ONE(1, true),
	FILL_UP_TWO(2, true),
	HEIGHT_1(1, false),
	HEIGHT_2(2, false),
	HEIGHT_3(3, false),
	HEIGHT_4(4, false),
	HEIGHT_5(5, false),
	HEIGHT_6(6, false),
	HEIGHT_7(7, false),
	HEIGHT_8(8, false),
	HEIGHT_9(9, false),
	HEIGHT_10(10, false),
	HEIGHT_11(11, false),
	HEIGHT_12(12, false),
	CUBE(1, true);

	public final String name;
	public final boolean isFiller;
	public final boolean isHeight;
	public final int height;
	public final int fillOffset;

	TerrainType(int height, boolean filler) {
		name = name().toLowerCase(Locale.ROOT);
		this.height = filler ? 0 : height;
		fillOffset = filler ? height : 0;
		isFiller = filler;
		isHeight = !filler;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
