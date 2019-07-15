package grondag.xm2.api.model;

import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;

public interface MutableModelWorldState extends ModelWorldState {
    default void posX(int index) { }

    default void posY(int index) { };

    default void posZ(int index) { };

    default void species(int species) { }

    default void cornerJoin(CornerJoinState join) { }

    default void simpleJoin(SimpleJoinState join) { }

    default void masonryJoin(SimpleJoinState join) { }
}
