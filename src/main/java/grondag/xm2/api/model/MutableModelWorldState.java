package grondag.xm2.api.model;

import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;

public interface MutableModelWorldState extends ModelWorldState {
    void posX(int index);

    void posY(int index);

    void posZ(int index);

    void species(int species);

    void cornerJoin(CornerJoinState join);

    void simpleJoin(SimpleJoinState join);

    void masonryJoin(SimpleJoinState join);
}
