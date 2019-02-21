package grondag.brocade.painting;

import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.state.ISuperModelState;

/**
 * Logic to apply color, brightness, glow and other attributes that depend on
 * quad, surface, or model state to each vertex in the quad. Applied after UV
 * coordinates have been assigned.
 * <p>
 * 
 * While intended to assign color values, could also be used to transform UV,
 * normal or other vertex attributes.
 */
public abstract class VertexProcessor {
    // 0 is reserved for default instance because model state default ordinal value
    // is zero
    private static int nextOrdinal = 1;

    public final String registryName;
    public final int ordinal;

    protected VertexProcessor(String registryName) {
        this(registryName, nextOrdinal++);
    }

    /**
     * For default instance only
     */
    protected VertexProcessor(String registryName, int ordinal) {
        this.ordinal = ordinal;
        this.registryName = registryName;
    }

    public abstract void process(IMutablePolygon result, int layerIndex, ISuperModelState modelState,
            PaintLayer paintLayer);
}
