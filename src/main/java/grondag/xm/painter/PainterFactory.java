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
package grondag.xm.painter;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.painter.AbstractQuadPainter.PaintMethod;

@SuppressWarnings("rawtypes")
@Internal
public class PainterFactory {
	public static PaintMethod getPainter(BaseModelState modelState, XmSurface surface, XmPaint paint, int textureDepth) {

		final TextureSet texture = paint.texture(textureDepth);

		switch (surface.topology()) {

		case TILED:
			switch (texture.map().layout()) {
			case SIMPLE:
			case BIGTEX_ANIMATED:
			case SPLIT_X_8:
				return SurfacePainterTiled::paintQuads;

			case BORDER_13:
				return null;

			case MASONRY_5:
				return null;

			default:
				return null;
			}

		case CUBIC:
			switch (texture.map().layout()) {
			case SIMPLE:
			case BIGTEX_ANIMATED:
			case SPLIT_X_8:
				return (texture.scale() == TextureScale.SINGLE) ? CubicPainterTiles::paintQuads : CubicPainterBigTex::paintQuads;

			case BORDER_13:
			case BORDER_14:
				return surface.allowBorders() ? CubicPainterBorders::paintQuads : null;

			case MASONRY_5:
				return surface.allowBorders() ? CubicPainterMasonry::paintQuads : null;

			case QUADRANT_ROTATED_CABLE:
				return surface.allowBorders() ? QuadPainterCable::paintQuads : null;

			case QUADRANT_ROTATED:
				return surface.allowBorders() ? QuadPainterRotated::paintQuads : null;

			case QUADRANT_ORIENTED_BORDER:
				return surface.allowBorders() ? QuadPainterOriented::paintBorders : null;

			case QUADRANT_ORIENTED_TILE:
				return QuadPainterOriented::paintTiles;

			default:
				return null;
			}

		default:
			return null;
		}
	}
}
