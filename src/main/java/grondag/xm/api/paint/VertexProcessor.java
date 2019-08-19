package grondag.xm.api.paint;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.paint.VertexProcessorRegistryImpl;

/**
 * Logic to apply color, brightness, glow and other attributes that depend on
 * quad, surface, or model state to each vertex in the quad. Applied after UV
 * coordinates have been assigned.
 * <p>
 * 
 * While intended to assign color values, could also be used to transform UV,
 * normal or other vertex attributes.
 */
@FunctionalInterface
public interface VertexProcessor {
    @SuppressWarnings("rawtypes")
    void process(MutablePolygon result, PrimitiveModelState modelState, XmSurface surface, XmPaint paint, int textureIndex);
    
    static VertexProcessor DEFAULT_VERTEX_PROCESSOR = VertexProcessorRegistryImpl.DEFAULT_VERTEX_PROCESSOR;
}