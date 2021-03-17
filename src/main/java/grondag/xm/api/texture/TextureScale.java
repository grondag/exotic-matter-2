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

import static grondag.xm.api.modelstate.ModelStateFlags.NONE;
import static grondag.xm.api.modelstate.ModelStateFlags.POSITION;

import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public enum TextureScale {
	/** 1x1 */
	SINGLE(0, NONE),

	/** 2x2 */
	TINY(1, POSITION),

	/** 4x4 */
	SMALL(2, POSITION),

	/** 8x8 */
	MEDIUM(3, POSITION),

	/** 16x16 */
	LARGE(4, POSITION),

	/** 32x32 */
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

	/** mask to derive a value within the number of slice counts (sliceCount - 1) */
	public final int sliceCountMask;

	/** number of texture subdivisions as an exponent of 2 */
	public final int power;

	/**
	 * identifies the world state needed to drive texture random rotation/selection
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
