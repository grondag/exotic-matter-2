package grondag.xm2.api.model;

import grondag.xm2.model.varia.BlockOrientationType;

public interface ModelPrimitiveState extends ModelState {
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
    
    @Override
    ImmutableModelPrimitiveState toImmutable();

    @Override
    MutableModelPrimitiveState mutableCopy();
    
    /**
     * Returns a copy of this model state with only the bits that matter for
     * geometry. Used as lookup key for block damage models.
     */
    @Override
    ModelPrimitiveState geometricState();
}
