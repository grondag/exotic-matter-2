package grondag.xm2.api.model;

import grondag.xm2.model.varia.BlockOrientationType;

public interface ModelPrimitiveState {
    ModelPrimitive primitive();
    
    default boolean hasAxisOrientation() {
        return primitive().hasAxisOrientation(this);
    }

    default boolean hasAxisRotation() {
        return primitive().hasAxisRotation(this);
    }

    default boolean hasAxis() {
        return primitive().hasAxis(this);
    }
    
    default BlockOrientationType orientationType() {
        return primitive().orientationType(this);
    }

    default boolean isAxisOrthogonalToPlacementFace() {
        return primitive().isAxisOrthogonalToPlacementFace();
    }
}
