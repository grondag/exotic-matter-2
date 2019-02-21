package grondag.brocade.world;

import net.minecraft.util.math.Direction;

public enum SimpleJoinFaceState {
    NO_FACE(0), NONE(0), // must be after NO_FACE, overwrites NO_FACE in lookup table, should never be
                         // checked by lookup
    TOP(FaceSide.TOP.bitFlag), BOTTOM(FaceSide.BOTTOM.bitFlag), LEFT(FaceSide.LEFT.bitFlag),
    RIGHT(FaceSide.RIGHT.bitFlag), TOP_BOTTOM(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag),
    LEFT_RIGHT(FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag),
    TOP_BOTTOM_RIGHT(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag),
    TOP_BOTTOM_LEFT(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag),
    TOP_LEFT_RIGHT(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag),
    BOTTOM_LEFT_RIGHT(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag),
    TOP_LEFT(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag), TOP_RIGHT(FaceSide.TOP.bitFlag | FaceSide.RIGHT.bitFlag),
    BOTTOM_LEFT(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag),
    BOTTOM_RIGHT(FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag),
    ALL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag);

    private static final SimpleJoinFaceState[] LOOKUP = new SimpleJoinFaceState[16];

    private final int bitFlags;

    static {
        for (SimpleJoinFaceState state : SimpleJoinFaceState.values()) {
            LOOKUP[state.bitFlags] = state;
        }
    }

    private SimpleJoinFaceState(int faceBits) {
        this.bitFlags = faceBits;

    }

    private static SimpleJoinFaceState find(int faceBits) {
        return LOOKUP[(faceBits & 15)];
    }

    public static SimpleJoinFaceState find(Direction face, SimpleJoin join) {
        int faceFlags = 0;

        SimpleJoinFaceState fjs;

        if (join.isJoined(face)) {
            fjs = SimpleJoinFaceState.NO_FACE;
        } else {
            for (FaceSide fside : FaceSide.values()) {
                if (join.isJoined(fside.getRelativeFace(face))) {
                    faceFlags |= fside.bitFlag;
                }
            }

            fjs = SimpleJoinFaceState.find(faceFlags);
        }
        return fjs;
    }

    public static SimpleJoinFaceState find(Direction face, NeighborBlocks.NeighborTestResults tests) {
        int faceFlags = 0;

        SimpleJoinFaceState fjs;

        if (tests.result(face)) {
            fjs = SimpleJoinFaceState.NO_FACE;
        } else {
            for (FaceSide fside : FaceSide.values()) {
                Direction joinFace = fside.getRelativeFace(face);
                if (tests.result(joinFace) && !tests.result(BlockCorner.find(face, joinFace))) {
                    faceFlags |= fside.bitFlag;
                }
            }

            fjs = SimpleJoinFaceState.find(faceFlags);
        }
        return fjs;
    }

    public boolean isJoined(FaceSide side) {
        return (this.bitFlags & side.bitFlag) == side.bitFlag;
    }

    public boolean isJoined(Direction toFace, Direction onFace) {
        FaceSide side = FaceSide.lookup(toFace, onFace);
        return side == null ? false : this.isJoined(side);
    }
}
