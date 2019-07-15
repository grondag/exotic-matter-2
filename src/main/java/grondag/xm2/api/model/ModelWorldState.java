package grondag.xm2.api.model;

import blue.endless.jankson.annotation.Nullable;
import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;

/**
 * Block-specific elements of model state, determined from BlockState or block
 * state of neighboring blocks, or bloc position. For static model state, may be
 * serialized vs. derived from world.
 *
 */
public interface ModelWorldState {
    default int posX() {
        return 0;
    }

    default int posY() {
        return 0;
    }

    default int posZ() {
        return 0;
    }

    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    default boolean hasSpecies() {
        return false;
    }
    
    /**
     * Will return 0 if model state does not include species. This is more
     * convenient than checking each place species is used.
     * 
     * @return
     */
    default int species() {
        return 0;
    }

    default @Nullable SimpleJoinState simpleJoin() {
        return null;
    }

    default @Nullable CornerJoinState cornerJoin() {
        return null;
    }

    default @Nullable SimpleJoinState masonryJoin() {
        return null;
    }
}
