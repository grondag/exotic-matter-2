package grondag.xm2.painting;

import java.util.function.Consumer;

import grondag.frex.Frex;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.IMutablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ImmutableMeshStateImpl;
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
        return builder.build();
    }

    @Override
    public void accept(IPolygon poly) {
        final ImmutableMeshStateImpl modelState = this.modelState;
        final QuadEmitter emitter = this.emitter;
        final IMutablePolyStream stream = this.work;
        IMutablePolygon editor = stream.editor();
        
        Surface surface = poly.getSurface();

        stream.appendCopy(poly);
        stream.editorOrigin();
        
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
            final int c = editor.spriteColor(i, 0);
            editor.spriteColor(i, 1, c);
            editor.spriteColor(i, 2, c);

            final float u = editor.spriteU(i, 0);
            final float v = editor.spriteV(i, 0);
            editor.sprite(i, 1, u, v);
            editor.sprite(i, 2, u, v);
        }
        
        for (PaintLayer paintLayer : PaintLayer.VALUES) {
            if (modelState.isLayerEnabled(paintLayer) && !surface.isLayerDisabled(paintLayer)
                    && stream.editorOrigin())
                QuadPainterFactory.getPainter(modelState, surface, paintLayer)
                    .paintQuads(stream, modelState, paintLayer);
        }
        
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
        
        stream.clear();
    }
    
    
    private void polyToMesh(IMutablePolygon poly, QuadEmitter emitter) {
        if(!FREX_ACTIVE) {
            //TODO: handle non-FREX output
            throw new UnsupportedOperationException("Exotic Matter currently requires FREX renderer.");
        }
        
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
        emitter.cullFace(poly.cullFace());
        emitter.nominalFace(poly.nominalFace());
        emitter.tag(poly.tag());
        
        for(int v = 0; v < 4; v++) {   
            emitter.pos(v, poly.x(v), poly.y(v), poly.z(v));
            if(poly.hasNormal(v)) {
                emitter.normal(v, poly.normalX(v), poly.normalY(v), poly.normalZ(v));
            }
            
            emitter.sprite(v, 0, poly.spriteU(v, 0), poly.spriteV(v, 0));
            emitter.spriteColor(v, 0, poly.spriteColor(v, 0));
            
            if(depth > 1) {
                emitter.sprite(v, 1, poly.spriteU(v, 1), poly.spriteV(v, 1));
                emitter.spriteColor(v, 1, poly.spriteColor(v, 1));
                
                if(depth == 3) {
                    emitter.sprite(v, 2, poly.spriteU(v, 2), poly.spriteV(v, 2));
                    emitter.spriteColor(v, 2, poly.spriteColor(v, 2));
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
        final int vCount = poly.vertexCount();
        for(int v = 0; v < vCount; v++) {
            poly.sprite(v, spriteIndex, 
                    minU + spanU * poly.spriteU(v, spriteIndex),
                    minV + spanV * poly.spriteV(v, spriteIndex));
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
        for(int v = 0; v < vCount; v++) {
            poly.sprite(v, spriteIndex, 
                    spriteMinU + spriteSpanU * poly.spriteU(v, spriteIndex),
                    spriteMinV + spriteSpanV * poly.spriteV(v, spriteIndex));
        }
        
    }

    private void contractUVs(int spriteIndex, Sprite sprite, IMutablePolygon poly) {
        //TODO: implement UV contract - refer to vanilla for example
    }

    private void applyTextureRotation(int spriteIndex, IMutablePolygon poly) {
        final int vCount = poly.vertexCount();
        switch(poly.getRotation(spriteIndex))
        {
        case ROTATE_NONE:
        default:
            break;
            
        case ROTATE_90:
            for(int i = 0; i < vCount; i++) {
                final float uOld = poly.spriteU(i, spriteIndex);
                final float vOld = poly.spriteV(i, spriteIndex);
                poly.sprite(i, spriteIndex, vOld, 1 - uOld);
            }
            break;

        case ROTATE_180:
            for(int i = 0; i < vCount; i++) {
                final float uOld = poly.spriteU(i, spriteIndex);
                final float vOld = poly.spriteV(i, spriteIndex);
                poly.sprite(i, spriteIndex, 1 - uOld, 1 - vOld);
            }
            break;
        
        case ROTATE_270:
            for(int i = 0; i < vCount; i++) {
                final float uOld = poly.spriteU(i, spriteIndex);
                final float vOld = poly.spriteV(i, spriteIndex);
                poly.sprite(i, spriteIndex, 1 - vOld, uOld);
            }
         break;
        
        }        
    }
    
}
