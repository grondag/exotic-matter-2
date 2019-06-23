package grondag.brocade.painting;

import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.IMutablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;
import grondag.brocade.state.ISuperModelState;

/**
 * Low-garbage consumer for quads from mesh generators that manages
 * instantiation and processing of painters and then passes through painted quad
 * to another consumer.
 *
 */
public class QuadPaintManager implements Consumer<IPolygon> {
    private static ThreadLocal<QuadPaintManager> managers = ThreadLocal.withInitial(() -> new QuadPaintManager());

    public static final QuadPaintManager get() {
        return managers.get();
    }

    private final IdentityHashMap<Surface, IMutablePolyStream> surfaces = new IdentityHashMap<Surface, IMutablePolyStream>();

    @Override
    public void accept(IPolygon poly) {
        IMutablePolyStream stream = surfaces.computeIfAbsent(poly.getSurface(), p -> PolyStreams.claimMutable(0));

        int address = stream.writerAddress();
        stream.appendCopy(poly);
        stream.moveEditor(address);
        IMutablePolygon editor = stream.editor();

        // expects all input polys to be single-layer
        assert editor.layerCount() == 1;

        // assign three layers for painting and then correct after paint occurs
        editor.setLayerCount(3);

        // should have no textures assigned at start
        assert editor.getTextureName(0) == null;
        assert editor.getTextureName(1) == null;
        assert editor.getTextureName(2) == null;

        // Copy generator UVs (quad and vertex)
        // from layer 0 to upper layers.
        float f = editor.getMinU(0);
        editor.setMinU(1, f);
        editor.setMinU(2, f);
        f = editor.getMaxU(0);
        editor.setMaxU(1, f);
        editor.setMaxU(2, f);
        f = editor.getMinV(0);
        editor.setMinV(1, f);
        editor.setMinV(2, f);
        f = editor.getMaxV(0);
        editor.setMaxV(1, f);
        editor.setMaxV(2, f);

        final int vertexCount = editor.vertexCount();
        for (int i = 0; i < vertexCount; i++) {
            int c = editor.getVertexColor(0, i);
            editor.setVertexColor(1, i, c);
            editor.setVertexColor(2, i, c);

            float u = editor.getVertexU(0, i);
            float v = editor.getVertexV(0, i);
            editor.setVertexUV(1, i, u, v);
            editor.setVertexUV(2, i, u, v);
        }
    }

    public void producePaintedQuads(final ISuperModelState modelState, final boolean isItem, final Consumer<IPolygon> target) {
        for (Entry<Surface, IMutablePolyStream> entry : surfaces.entrySet()) {
            Surface surface = entry.getKey();
            IMutablePolyStream stream = entry.getValue();

            for (PaintLayer paintLayer : PaintLayer.VALUES)
                if (modelState.isLayerEnabled(paintLayer) && !surface.isLayerDisabled(paintLayer)
                        && stream.editorOrigin())
                    QuadPainterFactory.getPainter(modelState, surface, paintLayer).paintQuads(stream, modelState,
                            paintLayer);

            if (stream.editorOrigin()) {
                final IMutablePolygon editor = stream.editor();
                do {
                    // omit polys that weren't textured by any painter
                    if (editor.getTextureName(0) != null) {
                        final int layerCount = editor.getTextureName(1) == null ? 1
                                : editor.getTextureName(2) == null ? 2 : 3;

                        editor.setLayerCount(layerCount);
                    }

                    target.accept(editor);
                } while (stream.editorNext());
            }

            stream.release();
        }

        surfaces.clear();
    }
}
