package grondag.xm2.painting;

import grondag.xm2.api.texture.TextureScale;
import grondag.xm2.api.texture.TextureSet;
import grondag.xm2.painting.QuadPainter.IPaintMethod;
import grondag.xm2.primitives.stream.IMutablePolyStream;
import grondag.xm2.state.ModelState;

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
            switch (texture.layout()) {
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
            switch (texture.layout()) {
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
