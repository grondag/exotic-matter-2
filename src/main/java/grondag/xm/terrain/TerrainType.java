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
package grondag.xm.terrain;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.StringIdentifiable;

@Internal
public enum TerrainType implements StringIdentifiable {
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
		name = name().toLowerCase();
		this.height = filler ? 0 : height;
		fillOffset = filler ? height : 0;
		isFiller = filler;
		isHeight = !filler;
	}

	@Override
	public String asString() {
		return name;
	}
}
