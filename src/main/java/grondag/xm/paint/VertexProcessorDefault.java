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
	private VertexProcessorDefault() {}
	public final static VertexProcessor INSTANCE = new VertexProcessorDefault();

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
				if(lampPaint != null && lampPaint.emissive(0)) {
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
