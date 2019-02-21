package grondag.brocade.world;

import net.minecraft.util.math.Direction;

public enum FaceSide {
    TOP(Direction.NORTH, Direction.NORTH, Direction.UP, Direction.UP, Direction.UP, Direction.UP),
    BOTTOM(Direction.SOUTH, Direction.SOUTH, Direction.DOWN, Direction.DOWN, Direction.DOWN, Direction.DOWN),
    LEFT(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST),
    RIGHT(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public static final FaceSide[] VALUES = FaceSide.values();
    public static final int COUNT = VALUES.length;

    // find the side for a given face orthogonal to a face
    private final static FaceSide FACE_LOOKUP[][] = new FaceSide[6][6];

    static {
        for (Direction onFace : Direction.values()) {
            for (Direction sideFace : Direction.values()) {
                FaceSide match = null;

                for (FaceSide side : FaceSide.values()) {
                    if (side.getRelativeFace(onFace) == sideFace) {
                        match = side;
                    }
                }

                FACE_LOOKUP[onFace.ordinal()][sideFace.ordinal()] = match;
            }
        }
    }

    /**
     * Determines if the given sideFace is TOP, BOTTOM, DEFAULT_LEFT or
     * DEFAULT_RIGHT of onFace. If none (sideFace on same orthogonalAxis as onFace),
     * return null;
     */
    public static FaceSide lookup(Direction sideFace, Direction onFace) {
        return FACE_LOOKUP[onFace.ordinal()][sideFace.ordinal()];
    }

    // for a given face, which face is at the position identified by this enum?
    private final Direction RELATIVE_LOOKUP[] = new Direction[6];

    private FaceSide(Direction up, Direction down, Direction east, Direction west, Direction north, Direction south) {
        RELATIVE_LOOKUP[Direction.UP.ordinal()] = up;
        RELATIVE_LOOKUP[Direction.DOWN.ordinal()] = down;
        RELATIVE_LOOKUP[Direction.EAST.ordinal()] = east;
        RELATIVE_LOOKUP[Direction.WEST.ordinal()] = west;
        RELATIVE_LOOKUP[Direction.NORTH.ordinal()] = north;
        RELATIVE_LOOKUP[Direction.SOUTH.ordinal()] = south;

        this.bitFlag = 1 << this.ordinal();
    }

    public final int bitFlag;

    public FaceSide getClockwise() {
        switch (this) {
        case BOTTOM:
            return LEFT;
        case LEFT:
            return TOP;
        case RIGHT:
            return BOTTOM;
        case TOP:
            return RIGHT;
        default:
            return null;
        }
    }

    public FaceSide getCounterClockwise() {
        switch (this) {
        case BOTTOM:
            return RIGHT;
        case LEFT:
            return BOTTOM;
        case RIGHT:
            return TOP;
        case TOP:
            return LEFT;
        default:
            return null;
        }
    }

    /**
     * Returns the face that is at the side identified by this enum on the given
     * face.
     */
    public Direction getRelativeFace(Direction face) {
        return RELATIVE_LOOKUP[face.ordinal()];
    }
}
