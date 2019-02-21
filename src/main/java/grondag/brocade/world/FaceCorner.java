package grondag.brocade.world;

public enum FaceCorner {
    TOP_LEFT(FaceSide.LEFT, FaceSide.TOP), TOP_RIGHT(FaceSide.TOP, FaceSide.RIGHT),
    BOTTOM_LEFT(FaceSide.BOTTOM, FaceSide.LEFT), BOTTOM_RIGHT(FaceSide.RIGHT, FaceSide.BOTTOM);

    private static FaceCorner[][] LOOKUP = new FaceCorner[4][4];

    /**
     * Side that is counterclockwise from the other side.
     */
    public final FaceSide leftSide;

    /**
     * Side that is clockwise from the other side.
     */
    public final FaceSide rightSide;

    public final int bitFlag;

    static {
        for (FaceCorner corner : FaceCorner.values()) {
            LOOKUP[corner.leftSide.ordinal()][corner.rightSide.ordinal()] = corner;
            LOOKUP[corner.rightSide.ordinal()][corner.leftSide.ordinal()] = corner;
        }
    }

    private FaceCorner(FaceSide leftSide, FaceSide rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.bitFlag = 1 << this.ordinal();
    }

    public static FaceCorner find(FaceSide side1, FaceSide side2) {
        return LOOKUP[side1.ordinal()][side2.ordinal()];
    }
}
