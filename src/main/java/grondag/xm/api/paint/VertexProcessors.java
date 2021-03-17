/*******************************************************************************
 * Copyright 2020 grondag
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
package grondag.xm.api.paint;

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fermion.color.ColorHelper;

/**
 * Vertex processors to support common use cases.
 */
@Experimental
public interface VertexProcessors {
	VertexProcessor SPECIES_VARIATION = (poly, modelState, surface, paint, textureIndex) -> {
		final int mix = HashCommon.mix(modelState.species());
		final int value = mix & 0xF;
		final int mixColor = 0xFFFFFFFF - (mix & 0x0F0000) - value - (value << 8);
		final int color = ColorHelper.multiplyColor(mixColor, paint.textureColor(textureIndex));

		for (int i = 0; i < poly.vertexCount(); i++) {
			final int c = ColorHelper.multiplyColor(color, poly.color(i, textureIndex));
			poly.color(i, textureIndex, c);
		}
	};
}
