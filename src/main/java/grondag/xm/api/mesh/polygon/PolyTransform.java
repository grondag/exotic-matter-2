package grondag.xm.api.mesh.polygon;

import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.orientation.CubeRotation;
import grondag.xm.mesh.helper.PolyTransformImpl;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

@FunctionalInterface
public interface PolyTransform {
    void apply(MutablePolygon poly);
    
    /**
     * Find appropriate transformation assuming base model is oriented with as follows:
     * Axis = Y with positive orientation if orientation applies.<p>
     * 
     * For the default rotation, generally, {@code DOWN} is considered the "bottom"
     * and {@code SOUTH} is the "back" when facing the "front" of the primitive.<p>
     * 
     * For primitives oriented to a corner, the default corner is "bottom, right, back"
     * in the frame just described, or {@code DOWN}, {@code SOUTH}, {@code EAST} in terms
     * of specific faces.
     */
    @SuppressWarnings("rawtypes")
    static PolyTransform get(PrimitiveModelState modelState) {
        return PolyTransformImpl.get(modelState);
    }

    static PolyTransform forEdgeRotation(int ordinal) {
        return PolyTransformImpl.forEdgeRotation(ordinal);
    }
    
    static PolyTransform get(CubeRotation corner) {
        return PolyTransformImpl.get(corner);
    }
    
    public static PolyTransform get(Axis axis) {
        return PolyTransformImpl.get(axis);
    }
    
    public static PolyTransform get(Direction face) {
        return PolyTransformImpl.get(face);
    }
}