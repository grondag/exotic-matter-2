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

package grondag.xm2.painting;

import grondag.xm2.painting.QuadPainter.IPaintMethod;
import grondag.xm2.primitives.stream.IMutablePolyStream;
import grondag.xm2.state.ModelState;
import grondag.xm2.texture.api.TextureScale;
import grondag.xm2.texture.api.TextureSet;

public class QuadPainterFactory {
    private static IPaintMethod DO_NOTHING = new IPaintMethod() {
        @Override
        public void paintQuads(IMutablePolyStream stream, ModelState modelState, PaintLayer paintLayer) {
            // Live up to our name...
        }
    };

    public static IPaintMethod getPainter(ModelState modelState, Surface surface, PaintLayer paintLayer) {
        if (surface.isLayerDisabled(paintLayer))
            return DO_NOTHING;

        TextureSet texture = modelState.getTexture(paintLayer);

        switch (surface.topology) {

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
                return (texture.scale() == TextureScale.SINGLE) ? CubicQuadPainterTiles::paintQuads
                        : CubicQuadPainterBigTex::paintQuads;

            case BORDER_13:
                return surface.allowBorders ? CubicQuadPainterBorders::paintQuads : null;

            case MASONRY_5:
                return surface.allowBorders ? CubicQuadPainterMasonry::paintQuads : null;

            case QUADRANT_CONNECTED:
                return surface.allowBorders ? CubicQuadPainterQuadrants::paintQuads : null;

            default:
                return null;
            }

        default:
            return null;
        }
    }
}