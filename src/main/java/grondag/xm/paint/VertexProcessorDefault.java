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

package grondag.xm.paint;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.fermion.color.ColorHelper;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.VertexProcessor;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;

@Internal
public class VertexProcessorDefault implements VertexProcessor {
	private VertexProcessorDefault() { }

	public static final VertexProcessor INSTANCE = new VertexProcessorDefault();

	@Override
	@SuppressWarnings("rawtypes")
	public final void process(MutablePolygon poly, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
		final int color = paint.textureColor(textureIndex);

		// If surface is a lamp gradient then glow bits are used
		// to blend the lamp color/brighness with the nominal color/brightness.
		// This does not apply with the lamp paint layer itself (makes no sense).
		// (Generally gradient surfaces should not be painted by lamp color)
		if (surface.isLampGradient()) {
			@SuppressWarnings("unchecked")
			final XmSurface lampSurface = modelState.primitive().lampSurface(modelState);

			if (lampSurface != null) {
				final XmPaint lampPaint = modelState.paint(lampSurface.ordinal());

				if (lampPaint != null && lampPaint.emissive(0)) {
					final int lampColor = lampPaint.textureColor(0);
					final int lampBrightness = lampPaint.emissive(0) ? 0xF0 : 0;

					// keep target surface alpha
					final int alpha = color & 0xFF000000;

					for (int i = 0; i < poly.vertexCount(); i++) {
						final float w = poly.glow(i) / 255f;
						final int b = Math.round(lampBrightness * w);
						final int c = ColorHelper.interpolate(color, lampColor, w) & 0xFFFFFF;
						poly.color(i, textureIndex, c | alpha);
						poly.glow(i, b);
					}

					return;
				}
			}
		}

		//normal shaded surface - tint existing colors, usually WHITE to start with
		for (int i = 0; i < poly.vertexCount(); i++) {
			final int c = ColorHelper.multiplyColor(color, poly.color(i, textureIndex));
			poly.color(i, textureIndex, c);
		}
	}
}
