package grondag.brocade.world;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public enum FarCorner {
    UP_NORTH_EAST(Direction.UP, Direction.EAST, Direction.NORTH),
    UP_NORTH_WEST(Direction.UP, Direction.WEST, Direction.NORTH),
    UP_SOUTH_EAST(Direction.UP, Direction.EAST, Direction.SOUTH),
    UP_SOUTH_WEST(Direction.UP, Direction.WEST, Direction.SOUTH),
    DOWN_NORTH_EAST(Direction.DOWN, Direction.EAST, Direction.NORTH),
    DOWN_NORTH_WEST(Direction.DOWN, Direction.WEST, Direction.NORTH),
    DOWN_SOUTH_EAST(Direction.DOWN, Direction.EAST, Direction.SOUTH),
    DOWN_SOUTH_WEST(Direction.DOWN, Direction.WEST, Direction.SOUTH);

    public final Direction face1;
    public final Direction face2;
    public final Direction face3;
    public final int bitFlag;
    public final Vec3i directionVector;
    /**
     * Ordinal sequence that includes all faces, corner and far corners. Use to
     * index them in a mixed array.
     */
    public final int superOrdinal;
    private static final FarCorner[][][] FAR_CORNER_LOOKUP = new FarCorner[6][6][6];

    static {
        for (FarCorner corner : FarCorner.values()) {
            FAR_CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()][corner.face3.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face1.ordinal()][corner.face3.ordinal()][corner.face2.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()][corner.face3.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face2.ordinal()][corner.face3.ordinal()][corner.face1.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face3.ordinal()][corner.face2.ordinal()][corner.face1.ordinal()] = corner;
            FAR_CORNER_LOOKUP[corner.face3.ordinal()][corner.face1.ordinal()][corner.face2.ordinal()] = corner;
        }
    }

    private FarCorner(Direction face1, Direction face2, Direction face3) {
        this.face1 = face1;
        this.face2 = face2;
        this.face3 = face3;
        this.bitFlag = 1 << (NeighborBlocks.FACE_FLAGS.length + BlockCorner.values().length + this.ordinal());
        // 6 is number of possible faces
        this.superOrdinal = this.ordinal() + 6 + BlockCorner.values().length;

        Vec3i v1 = face1.getVector();
        Vec3i v2 = face2.getVector();
        Vec3i v3 = face3.getVector();
        this.directionVector = new Vec3i(v1.getX() + v2.getX() + v3.getX(), v1.getY() + v2.getY() + v3.getY(),
                v1.getZ() + v2.getZ() + v3.getZ());

    }

    public static FarCorner find(Direction face1, Direction face2, Direction face3) {
        return FarCorner.FAR_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()][face3.ordinal()];
    }
}