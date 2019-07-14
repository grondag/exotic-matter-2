package grondag.xm2.api.model;

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
}
