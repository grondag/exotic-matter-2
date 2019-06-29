package grondag.brocade.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;
import net.fabricmc.indigo.renderer.IndigoRenderer;
import net.fabricmc.indigo.renderer.RenderMaterialImpl;
import net.fabricmc.indigo.renderer.helper.ColorHelper;
import net.fabricmc.indigo.renderer.helper.ColorHelper.ShadeableQuad;

//TODO: move to FREX
public class QuadRotation implements QuadTransform {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Matrix4f matrix = new Matrix4f();
        private boolean relight = false;
        private Builder() {}
        
        /** If true, will undo and redo diffuse shading. Use to transform baked quads. Default is false. */
        public Builder relight(boolean relight) {
            this.relight = relight;
            return this;
        }
        
        public Builder xDegrees(int d) {
            return xRadians((float)Math.toRadians(d));
        }
        
        public Builder yDegrees(int d) {
            return yRadians((float)Math.toRadians(d));
        }
        
        public Builder zDegrees(int d) {
            return zRadians((float)Math.toRadians(d));
        }
        
        public Builder xRadians(float r) {
            matrix.rotate(r, 1, 0, 0);
            return this;
        }
        
        public Builder yRadians(float r) {
            matrix.rotate(r, 0, 1, 0);
            return this;
        }
        
        public Builder zRadians(float r) {
            matrix.rotate(r, 0, 0, 1);
            return this;
        }
    }
    
    private final Matrix4f matrix;
    private final boolean relight;

    private QuadRotation(Builder builder) {
        this.matrix = new Matrix4f(builder.matrix);
        this.relight = builder.relight;
    }
    
    private static final boolean isIndigo = RendererAccess.INSTANCE.getRenderer().getClass() == IndigoRenderer.class;
    
    private static final ThreadLocal<Vector3f> VEC3 = ThreadLocal.withInitial(Vector3f::new);
    
    @Override
    public boolean transform(MutableQuadView quad) {
      //  Direction oldFace = quad.nominalFace();
        
        // TODO: Find a better way than this hack.
        // For Indigo, need to reverse and re-apply diffuse shading
        final boolean reshade = relight && isIndigo && ((RenderMaterialImpl.Value)quad.material()).disableDiffuse(0) == false;
        if(reshade) {
            ColorHelper.applyDiffuseShading((ShadeableQuad) quad, true);
        }
        
        Vector3f vec = VEC3.get();
        
        applyRotation(0, vec, quad);
        applyRotation(1, vec, quad);
        applyRotation(2, vec, quad);
        applyRotation(3, vec, quad);
        
        if(reshade) {
            ColorHelper.applyDiffuseShading((ShadeableQuad) quad, false);
        }
        
       // Direction newFace = quad.nominalFace();
        
        return true;
    }
    
    private void applyRotation(int vertexIndex, Vector3f vec, MutableQuadView quad) {
        matrix.transformPosition(quad.x(vertexIndex) - 0.5f, quad.y(vertexIndex) - 0.5f, quad.z(vertexIndex) - 0.5f, vec);
        quad.pos(vertexIndex, vec.x() + 0.5f, vec.y() + 0.5f, vec.z() + 0.5f);
    }
    
}
