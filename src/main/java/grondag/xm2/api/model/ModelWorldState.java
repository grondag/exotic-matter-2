package grondag.xm2.api.model;

import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;

/**
 * Block-specific elements of model state, determined from 
 * BlockState or block state of neighboring blocks, or bloc position.
 * For static model state, may be serialized vs. derived from world.
 *
 */
public interface ModelWorldState {
    int posX();

    int posY();

    int posZ();
    
    /**
     * Will return 0 if model state does not include species. This is more
     * convenient than checking each place species is used.
     * 
     * @return
     */
    int species();

    SimpleJoinState simpleJoin();

    CornerJoinState cornerJoin();

    SimpleJoinState masonryJoin();
}
