package grondag.brocade.dispatch;

//TODO: remove
public enum SideShape {
    SOLID(true, true), PARTIAL(true, false), MISSING(false, false);

    /** true if torch can be placed on this side */
    public final boolean holdsTorch;

    /**
     * True if side blocks rendering of face it is against. Does NOT account for
     * translucency - only geometry.
     */
    public final boolean occludesOpposite;

    private SideShape(boolean holdsTorch, boolean occludesOpposite) {
        this.holdsTorch = holdsTorch;
        this.occludesOpposite = occludesOpposite;

    }
}
