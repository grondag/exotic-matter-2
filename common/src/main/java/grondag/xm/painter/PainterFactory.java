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
