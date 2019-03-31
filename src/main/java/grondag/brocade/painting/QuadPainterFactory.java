package grondag.brocade.painting;

import grondag.brocade.painting.QuadPainter.IPaintMethod;
import grondag.brocade.primitives.stream.IMutablePolyStream;
import grondag.brocade.model.state.ISuperModelState;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.model.texture.TextureScale;

public class QuadPainterFactory {
    private static IPaintMethod DO_NOTHING = new IPaintMethod() {
        @Override
        public void paintQuads(IMutablePolyStream stream, ISuperModelState modelState, PaintLayer paintLayer) {
            // Live up to our name...
        }
    };

    public static IPaintMethod getPainter(ISuperModelState modelState, Surface surface, PaintLayer paintLayer) {
        if (surface.isLayerDisabled(paintLayer))
            return DO_NOTHING;

        ITexturePalette texture = modelState.getTexture(paintLayer);

        switch (surface.topology) {

        case TILED:
            switch (texture.textureLayout()) {
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
            switch (texture.textureLayout()) {
            case SIMPLE:
            case BIGTEX_ANIMATED:
            case SPLIT_X_8:
                return (texture.textureScale() == TextureScale.SINGLE) ? CubicQuadPainterTiles::paintQuads
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
