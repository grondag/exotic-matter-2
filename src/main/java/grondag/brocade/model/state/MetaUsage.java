package grondag.brocade.model.state;

/**
 * Defines how difference shapes/blocks use block/item metadata. <br>
 * <br>
 * 
 * For SuperBlocks with tile entities, metadata is redundant of information
 * already in modelState. However it may still be stored in block metadata so
 * that it can be searched/retrieved without the need for modelState. <br>
 * <br>
 * 
 * For SuperBlocks without tile entities, metadata may be used to partially
 * derive modelstate for a given world location.
 */
public enum MetaUsage {
    /**
     * Metadata is used to segregate visual block/border boundaries.
     * get/setMetaData() acts as an alias for get/setSpecies()
     */
    SPECIES,

    /** metadata drives some aspect of geometry - usually height/thickness */
    SHAPE,

    /** metadata is not used/related to modelstate */
    NONE
}
