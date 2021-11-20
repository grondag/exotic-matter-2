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

package grondag.xm.api.texture;

import static grondag.xm.api.modelstate.ModelStateFlags.NONE;
import static grondag.xm.api.modelstate.ModelStateFlags.POSITION;

import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public enum TextureScale {
	/** 1x1. */
	SINGLE(0, NONE),

	/** 2x2. */
	TINY(1, POSITION),

	/** 4x4. */
	SMALL(2, POSITION),

	/** 8x8. */
	MEDIUM(3, POSITION),

	/** 16x16. */
	LARGE(4, POSITION),

	/** 32x32. */
	GIANT(5, POSITION);

	/**
	 * UV length for each subdivision of the texture. Used by BigTex painter. Is
	 * simply 1/{@link #sliceCount}.
	 */
	public final float sliceIncrement;

	/**
	 * Number of texture subdivisions for BigTex (each division is one block face).
	 * Equivalently, the uv width/block faces covered by the texture if rendered at
	 * 1:1 blockface:uv-distance scale.
	 */
	public final int sliceCount;

	/** Mask to derive a value within the number of slice counts (sliceCount - 1). */
	public final int sliceCountMask;

	/** Number of texture subdivisions as an exponent of 2. */
	public final int power;

	/**
	 * Identifies the world state needed to drive texture random rotation/selection.
	 */
	public final int modelStateFlag;

	TextureScale(int power, int modelStateFlag) {
		this.power = power;
		sliceCount = 1 << power;
		sliceCountMask = sliceCount - 1;
		sliceIncrement = 1f / sliceCount;
		this.modelStateFlag = modelStateFlag;
	}
}
