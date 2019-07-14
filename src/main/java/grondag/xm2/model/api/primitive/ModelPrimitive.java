package grondag.xm2.model.api.primitive;

import java.util.function.Consumer;

import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.model.impl.state.ModelState;
import grondag.xm2.model.impl.varia.BlockOrientationType;
import grondag.xm2.surface.api.XmSurfaceList;
import net.minecraft.util.Identifier;

public interface ModelPrimitive {
	/** 
	 * Used for registration and serialization of model state.
	 */
	Identifier id();
	
	XmSurfaceList surfaces();
	
    /**
     * Override if shape has any kind of orientation to it that can be selected
     * during placement.
     */
    default BlockOrientationType orientationType(ModelState modelState) {
        return BlockOrientationType.NONE;
    }

	int stateFlags(ModelState modelState);
	
	 /**
     * Generator will output polygons and they will be quads or tris.
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
