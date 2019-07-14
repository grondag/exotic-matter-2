package grondag.xm2.model.api.primitive;

import grondag.xm2.model.impl.state.ModelState;
import grondag.xm2.model.impl.varia.BlockOrientationType;

public interface ModelPrimitive {

    /**
     * Override if shape has any kind of orientation to it that can be selected
     * during placement.
     */
    default BlockOrientationType orientationType(ModelState modelState) {
        return BlockOrientationType.NONE;
    }
}
