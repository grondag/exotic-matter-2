package grondag.xm2.api.model;

import java.util.function.Consumer;

import grondag.xm2.api.surface.XmSurfaceList;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.model.state.ModelState;
import grondag.xm2.model.varia.BlockOrientationType;
import net.minecraft.util.Identifier;

public interface ModelPrimitive {
	/** 
	 * Used for registration and serialization of model state.
	 */
	Identifier id();
	
	/**
	 * Used for fast, transient serialization. Recommended that
	 * implementations override this and cache value to avoid map lookups.
	 */
	default int index() {
		return ModelPrimitiveRegistry.INSTANCE.indexOf(this);
	}
	
	XmSurfaceList surfaces();
	
    /**
     * Override if shape has an orientation to be selected during placement.
     */
    default BlockOrientationType orientationType(ModelState modelState) {
        return BlockOrientationType.NONE;
    }

	int stateFlags(ModelState modelState);
	
	 /**
     * Output polygons must be quads or tris.
     * Consumer MUST NOT hold references to any of the polys received.
     */
    void produceQuads(ModelState modelState, Consumer<IPolygon> target);
    
    // UGLY: really needed?
    /**
     * When ModelState primitive is set, the primitive-specific elements will 
     * be set by applying this consumer to a mutable model state instance. 
     * Only need to change if shape needs some preset state.
     */
    default void applyDefaultState(ModelState modelState) {
    	//NOOP
    }
    
    /**
     * If true, shape can be placed on itself to become bigger.
     */
    default boolean isAdditive() {
        return false;
    }

    /**
     * Override to true for blocks like stairs and wedges. CubicPlacementHandler
     * will know they need to be placed in a corner instead of a face.
     */
    default boolean isAxisOrthogonalToPlacementFace() {
        return false;
    }
}