package grondag.xm2.api.model;

import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.terrain.TerrainState;
import net.minecraft.util.math.Direction;

public interface MutableModelPrimitiveState extends ModelPrimitiveState {
    void setAxis(Direction.Axis axis);

    void setAxisInverted(boolean isInverted);

    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    void setAxisRotation(ClockwiseRotation rotation);

    default void setTerrainState(TerrainState flowState) { }

    default void setTerrainStateKey(long terrainStateKey) { }
}
