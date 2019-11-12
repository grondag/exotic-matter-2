package grondag.xm.api.texture;

import grondag.fermion.spatial.Rotation;

public enum TextureTransform {
    IDENTITY(Rotation.ROTATE_NONE, false),
    ROTATE_90(Rotation.ROTATE_90, false),
    ROTATE_180(Rotation.ROTATE_180, false),
    ROTATE_270(Rotation.ROTATE_270, false),
    ROTATE_RANDOM(Rotation.ROTATE_NONE, true),
    /** Use for tiles that must remain consistent for the same species */
    ROTATE_BIGTEX(Rotation.ROTATE_NONE, true),
    /** Rotate 180 and allow horizontal texture flip */
    STONE_LIKE(Rotation.ROTATE_NONE, true);

    public final Rotation baseRotation;
    public final boolean hasRandom;

    private TextureTransform(Rotation baseRotation, boolean hasRandom) {
        this.baseRotation = baseRotation;
        this.hasRandom = hasRandom;
    }
}
