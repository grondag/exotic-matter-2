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

package grondag.xm.api.paint;

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.util.ColorUtil;

/**
 * Vertex processors to support common use cases.
 */
@Experimental
public interface VertexProcessors {
	VertexProcessor SPECIES_VARIATION = (poly, modelState, surface, paint, textureIndex) -> {
		final int mix = HashCommon.mix(modelState.species());
		final int value = mix & 0xF;
		final int mixColor = 0xFFFFFFFF - (mix & 0x0F0000) - value - (value << 8);
		final int color = ColorUtil.multiplyColor(mixColor, paint.textureColor(textureIndex));

		for (int i = 0; i < poly.vertexCount(); i++) {
			final int c = ColorUtil.multiplyColor(color, poly.color(i, textureIndex));
			poly.color(i, textureIndex, c);
		}
	};
}
