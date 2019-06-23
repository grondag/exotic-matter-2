package grondag.brocade.model.dispatch;

import net.minecraft.util.math.Direction;

public class SimpleQuadBounds {
    public Direction face;
    public float x0;
    public float y0;
    public float x1;
    public float y1;
    public float depth;
    public Direction topFace;

    public SimpleQuadBounds(Direction face, float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        this.face = face;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.depth = depth;
        this.topFace = topFace;
    }
}