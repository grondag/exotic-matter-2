package grondag.brocade.api;

public enum TextureGroup {
    STATIC_TILES, STATIC_BORDERS, STATIC_DETAILS, DYNAMIC_TILES, DYNAMIC_BORDERS, DYNAMIC_DETAILS, HIDDEN_TILES,
    HIDDEN_BORDERS, HIDDEN_DETAILS, ALWAYS_HIDDEN;

    /** used as a fast way to filter textures from a list */
    public final int bitFlag;

    private TextureGroup() {
        this.bitFlag = (1 << this.ordinal());
    }

    public static int makeTextureGroupFlags(TextureGroup... groups) {
        int flags = 0;
        for (TextureGroup group : groups) {
            flags |= group.bitFlag;
        }
        return flags;
    }
}
