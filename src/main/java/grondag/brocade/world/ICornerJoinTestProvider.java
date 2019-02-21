package grondag.brocade.world;

import net.minecraft.util.math.Direction;

/**
 * Tests return true if the face or corner block is "joined."
 * 
 *
 */

public interface ICornerJoinTestProvider {
    /**
     * For faces on the origin block. True here means the block is obscured by a
     * like block placed against it. True will result will mean no border is
     * rendered.
     */
    boolean result(Direction face);

    /**
     * For blocks diagonally adjacent to an edge of the origin block. Indicate if a
     * corner is necessary or if an adjacent like block is joined to a like block
     * placed in front of it and should not be joined.
     */
    boolean result(BlockCorner corner);

    /**
     * Convenience for {@link #result(BlockCorner)}
     */
    public default boolean result(Direction face1, Direction face2) {
        BlockCorner corner = BlockCorner.find(face1, face2);
        return result(corner);
    }

    /**
     * For blocks diagonally adjacent to a corner of the origin block. Indicate if
     * an adjacent like block is joined to a like block placed in front of it and
     * should not be joined.
     */
    public boolean result(FarCorner corner);

    /**
     * Convenience for {@link #result(FarCorner)}
     */
    public default boolean result(Direction face1, Direction face2, Direction face3) {
        FarCorner corner = FarCorner.find(face1, face2, face3);
        return result(corner);
    }
}
