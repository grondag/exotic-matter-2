package grondag.brocade.api.texture;

import grondag.fermion.world.Rotation;

public enum TextureRotation {
    ROTATE_NONE(Rotation.ROTATE_NONE),
    ROTATE_90(Rotation.ROTATE_90),
    ROTATE_180(Rotation.ROTATE_180),
    ROTATE_270(Rotation.ROTATE_270),
    ROTATE_RANDOM(null);

    public final Rotation rotation;
    
    private TextureRotation(Rotation rotation) {
        this.rotation = rotation;
    }
}
