package grondag.xm2.api.model;

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
        return primitive().orientationType(this);
    }

    default boolean isAxisOrthogonalToPlacementFace() {
        return primitive().isAxisOrthogonalToPlacementFace();
    }
    
    /**
     * Returns a copy of this model state with only the bits that matter for
     * geometry. Used as lookup key for block damage models.
     */
    ModelState geometricState();
    
    Direction.Axis getAxis();

    boolean isAxisInverted();

    /**
     * Usage is determined by shape. Limited to 44 bits and does not update from
     * world.
     */
    long getStaticShapeBits();

    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    ClockwiseRotation getAxisRotation();

    /**
     * Multiblock shapes also get a full 64 bits of information - does not update
     * from world
     */
    long getMultiBlockBits();

    TerrainState getTerrainState();

    long getTerrainStateKey();

    int getTerrainHotness();
}
