package grondag.brocade.model.varia;

import net.minecraft.util.EnumFacing;

public class SimpleQuadBounds {
    public EnumFacing face;
    public float x0;
    public float y0;
    public float x1;
    public float y1;
    public float depth;
    public EnumFacing topFace;

    public SimpleQuadBounds(EnumFacing face, float x0, float y0, float x1, float y1, float depth, EnumFacing topFace) {
        this.face = face;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.depth = depth;
        this.topFace = topFace;
    }
}