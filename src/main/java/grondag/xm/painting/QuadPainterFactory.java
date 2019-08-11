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

package grondag.xm.painting;

import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.model.state.BaseModelState;
import grondag.xm.painting.QuadPainter.IPaintMethod;

@SuppressWarnings("rawtypes")
public class QuadPainterFactory {
    public static IPaintMethod getPainter(BaseModelState modelState, XmSurface surface, XmPaint paint, int textureDepth) {

        TextureSet texture = paint.texture(textureDepth);

        switch (surface.topology()) {

        case TILED:
            switch (texture.map().layout()) {
            case SIMPLE:
            case BIGTEX_ANIMATED:
            case SPLIT_X_8:
                return SurfaceQuadPainterTiled::paintQuads;

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
                return (texture.scale() == TextureScale.SINGLE) ? CubicQuadPainterTiles::paintQuads : CubicQuadPainterBigTex::paintQuads;

            case BORDER_13:
                return surface.allowBorders() ? CubicQuadPainterBorders::paintQuads : null;

            case MASONRY_5:
                return surface.allowBorders() ? CubicQuadPainterMasonry::paintQuads : null;

            case QUADRANT_CONNECTED:
                return surface.allowBorders() ? CubicQuadPainterQuadrants::paintQuads : null;

            default:
                return null;
            }

        default:
            return null;
        }
    }
}
