package grondag.xm2.primitives;

import grondag.fermion.world.Rotation;

public class FaceQuadInputs {
    public final int textureOffset;
    public final Rotation rotation;
    public final boolean flipU;
    public final boolean flipV;

    public FaceQuadInputs(int textureOffset, Rotation rotation, boolean flipU, boolean flipV) {
        this.textureOffset = textureOffset;
        this.rotation = rotation;
        this.flipU = flipU;
        this.flipV = flipV;
    }
}