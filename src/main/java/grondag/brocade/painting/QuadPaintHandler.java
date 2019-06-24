package grondag.brocade.painting;

import java.util.function.Consumer;

import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.IMutablePolyStream;
import grondag.brocade.primitives.stream.PolyStreams;
import grondag.brocade.state.ImmutableMeshStateImpl;
import grondag.frex.Frex;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;

@Environment(EnvType.CLIENT)
public class QuadPaintHandler implements Consumer<IPolygon> {
    private static final ThreadLocal<QuadPaintHandler> POOL = ThreadLocal.withInitial(QuadPaintHandler::new);
    
    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
    
    private static final boolean FREX_ACTIVE = Frex.isAvailable();
    
    public static Mesh paint(ImmutableMeshStateImpl meshState) {
        return POOL.get().handlePaint(meshState);
    }
    
    private final MeshBuilder builder = RENDERER.meshBuilder();
    private final IMutablePolyStream work = PolyStreams.claimMutable(0);
    private final QuadEmitter emitter = builder.getEmitter();
    private ImmutableMeshStateImpl modelState;
    private MaterialFinder finder = RENDERER.materialFinder();
    
    private  Mesh handlePaint(ImmutableMeshStateImpl modelState) {
        this.modelState = modelState;
        modelState.getShape().meshFactory().produceShapeQuads(modelState, this);
        work.clear();
        return builder.build();
    }

    @Override
    public void accept(IPolygon poly) {
        final ImmutableMeshStateImpl modelState = this.modelState;
        final QuadEmitter emitter = this.emitter;
        final IMutablePolyStream stream = this.work;
        IMutablePolygon editor = stream.editor();
        
        Surface surface = poly.getSurface();

        int address = stream.writerAddress();
        stream.appendCopy(poly);
        stream.moveEditor(address);
        
        // assign three layers for painting and then correct after paint occurs
        editor.setLayerCount(3);
        
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
        
        for (PaintLayer paintLayer : PaintLayer.VALUES)
            if (modelState.isLayerEnabled(paintLayer) && !surface.isLayerDisabled(paintLayer)
                    && stream.editorOrigin())
                QuadPainterFactory.getPainter(modelState, surface, paintLayer).paintQuads(stream, modelState,
                        paintLayer);

        if (stream.editorOrigin()) {
            do {
                // omit polys that weren't textured by any painter
                if (editor.getTextureName(0) != null) {
                    final int layerCount = editor.getTextureName(1) == null ? 1
                            : editor.getTextureName(2) == null ? 2 : 3;

                    editor.setLayerCount(layerCount);
                }
                polyToMesh(editor, emitter);
            } while (stream.editorNext());
        }
    }
    
    
    private void polyToMesh(IMutablePolygon poly, QuadEmitter emitter) {
        if(!FREX_ACTIVE) {
            //TODO: handle non-FREX output
            throw new UnsupportedOperationException("Brocade currently requires FREX renderer.");
        }
        
        emitter.cullFace(poly.getActualFace());
        emitter.nominalFace(poly.getNominalFace());
        emitter.tag(poly.getTag());
        
        final int depth = poly.layerCount();
        final MaterialFinder finder = this.finder;
        
        finder.clear().spriteDepth(depth);
        
        finder.blendMode(0, poly.getRenderLayer(0));
        if(poly.isEmissive(0)) {
            finder.disableAo(0, true).disableDiffuse(0, true).emissive(0, true);
        }
        bakeSprite(0, poly);
        
        if(depth > 1) {
            bakeSprite(1, poly);
            finder.blendMode(1, poly.getRenderLayer(1));
            if(poly.isEmissive(1)) {
                finder.disableAo(1, true).disableDiffuse(1, true).emissive(1, true);
            }
            if(depth == 3) {
                bakeSprite(2, poly);
                finder.blendMode(2, poly.getRenderLayer(2));
                if(poly.isEmissive(2)) {
                    finder.disableAo(2, true).disableDiffuse(2, true).emissive(2, true);
                }
            }
        }
        emitter.material(finder.find());
        
        for(int v = 0; v < 4; v++) {   
            emitter.pos(v, poly.getVertexX(v), poly.getVertexY(v), poly.getVertexZ(v));
            if(poly.hasVertexNormal(v)) {
                emitter.normal(v, poly.getVertexNormalX(v), poly.getVertexNormalY(v), poly.getVertexNormalZ(v));
            }
            
            emitter.sprite(v, 0, poly.getVertexU(0, v), poly.getVertexV(0, v));
            emitter.spriteColor(v, 0, poly.getVertexColor(0, v));
            
            if(depth > 1) {
                emitter.sprite(v, 1, poly.getVertexU(1, v), poly.getVertexV(1, v));
                emitter.spriteColor(v, 1, poly.getVertexColor(1, v));
                
                if(depth == 3) {
                    emitter.sprite(v, 2, poly.getVertexU(2, v), poly.getVertexV(2, v));
                    emitter.spriteColor(v, 2, poly.getVertexColor(2, v));
                }
            }
        }
        emitter.emit();
    }
    
    // PERF: consider in-lining all the coordinates
    private void bakeSprite(int spriteIndex, IMutablePolygon poly) {
        final float minU = poly.getMinU(spriteIndex);
        final float minV = poly.getMinV(spriteIndex);
        final float spanU = poly.getMaxU(spriteIndex) - minU;
        final float spanV = poly.getMaxV(spriteIndex) - minV;

        applyTextureRotation(spriteIndex, poly);
        
        // scale UV coordinates to size of texture sub-region
        for(int v = 0; v < 4; v++) {
            poly.setVertexUV(spriteIndex, v, 
                    minU + spanU * poly.getVertexU(spriteIndex, v),
                    minV + spanV * poly.getVertexV(spriteIndex, v));
        }
        
        final Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas().getSprite(poly.getTextureName(spriteIndex));
        
        if(poly.shouldContractUVs(spriteIndex)) {
            contractUVs(spriteIndex, sprite, poly);
        }
        
        final float spriteMinU = sprite.getMinU();
        final float spriteSpanU = sprite.getMaxU() - spriteMinU;
        final float spriteMinV = sprite.getMinV();
        final float spriteSpanV = sprite.getMaxV() - spriteMinV;
        
        // doing interpolation here vs using sprite methods to avoid wasteful multiply and divide by 16
        for(int v = 0; v < 4; v++) {
            poly.setVertexUV(spriteIndex, v, 
                    spriteMinU + spriteSpanU * poly.getVertexU(spriteIndex, v),
                    spriteMinV + spriteSpanV * poly.getVertexV(spriteIndex, v));
        }
        
    }

    private void contractUVs(int spriteIndex, Sprite sprite, IMutablePolygon poly) {
        //TODO: implement UV contract - refer to vanilla for example
    }

    private void applyTextureRotation(int spriteIndex, IMutablePolygon poly) {
        switch(poly.getRotation(spriteIndex))
        {
        case ROTATE_NONE:
        default:
            break;
            
        case ROTATE_90:
            for(int i = 0; i < 4; i++) {
                final float uOld = poly.getVertexU(spriteIndex, i);
                final float vOld = poly.getVertexV(spriteIndex, i);
                poly.setVertexUV(spriteIndex, i, vOld, 1 - uOld);
            }
            break;

        case ROTATE_180:
            for(int i = 0; i < 4; i++) {
                final float uOld = poly.getVertexU(spriteIndex, i);
                final float vOld = poly.getVertexV(spriteIndex, i);
                poly.setVertexUV(spriteIndex, i, 1 - uOld, 1 - vOld);
            }
            break;
        
        case ROTATE_270:
            for(int i = 0; i < 4; i++) {
                final float uOld = poly.getVertexU(spriteIndex, i);
                final float vOld = poly.getVertexV(spriteIndex, i);
                poly.setVertexUV(spriteIndex, i, 1 - vOld, uOld);
            }
         break;
        
        }        
    }
    
}
