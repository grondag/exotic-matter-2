package grondag.xm2.placement;

/**
 * Alternate offsets directions for block placement regions. Used to find
 * regsions that don't contain obstacles.
 */
public enum OffsetPosition {
    FLIP_NONE(1, 1, 1), FLIP_WIDTH(-1, 1, 1), FLIP_DEPTH(1, -1, 1), FLIP_BOTH(-1, -1, 1), FLIP_HEIGHT(1, 1, -1);

    public final int widthFactor;
    public final int depthFactor;
    public final int heightFactor;

    /**
     * Contains all values except the default value.
     */
    public static final OffsetPosition[] ALTERNATES = { FLIP_WIDTH, FLIP_DEPTH, FLIP_BOTH, FLIP_HEIGHT };

    private OffsetPosition(int widthFactor, int depthFactor, int heightFactor) {
        this.widthFactor = widthFactor;
        this.depthFactor = depthFactor;
        this.heightFactor = heightFactor;
    }
}
