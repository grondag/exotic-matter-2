package grondag.xm2.api.model;

import javax.annotation.Nullable;

import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.model.varia.BlockOrientationType;
import grondag.xm2.terrain.TerrainState;
import net.minecraft.util.math.Direction;

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
        return primitive().orientationType((ModelState)this);
    }

    default boolean isAxisOrthogonalToPlacementFace() {
        return primitive().isAxisOrthogonalToPlacementFace();
    }
    
    /**
     * Returns a copy of this model state with only the bits that matter for
     * geometry. Used as lookup key for block damage models.
     */
    default ModelState geometricState() {
        return primitive().geometricState((ModelState)this);
    }
    
    default Direction.Axis getAxis() {
        return Direction.Axis.Y;
    }

    default boolean isAxisInverted() {
        return false;
    }

    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    default ClockwiseRotation getAxisRotation() {
        return ClockwiseRotation.ROTATE_NONE;
    }

    default @Nullable TerrainState getTerrainState() {
        return null;
    };

    default long getTerrainStateKey() {
        return 0;
    }

    default int getTerrainHotness() {
        return 0;
    }
}
