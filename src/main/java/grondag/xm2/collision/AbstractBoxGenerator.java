package grondag.xm2.collision;

import java.util.function.Consumer;

import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.vertex.IVec3f;

public abstract class AbstractBoxGenerator implements Consumer<IPolygon> {
    // diameters
    static final float D1 = 0.5f;
    static final float D2 = D1 * 0.5f;
    static final float D3 = D2 * 0.5f;
    static final float D4 = D3 * 0.5f;

    // radii
    static final float R1 = D1 * 0.5f;
    static final float R2 = D2 * 0.5f;
    static final float R3 = D3 * 0.5f;
    static final float R4 = D4 * 0.5f;

    // center offsets, low and high
    static final float CLOW1 = 0.25f;
    static final float CHIGH1 = CLOW1 + D1;

    static final float CLOW2 = CLOW1 * 0.5f;
    static final float CHIGH2 = CLOW2 + D2;

    static final float CLOW3 = CLOW2 * 0.5f;
    static final float CHIGH3 = CLOW3 + D3;

    static final float CLOW4 = CLOW3 * 0.5f;
    static final float CHIGH4 = CLOW4 + D4;

    @Override
    public final void accept(IPolygon poly) {
        acceptTriangle(poly.getPos(0), poly.getPos(1), poly.getPos(2));

        if (poly.vertexCount() == 4)
            acceptTriangle(poly.getPos(0), poly.getPos(2), poly.getPos(3));
    }

    protected abstract void acceptTriangle(IVec3f v0, IVec3f v1, IVec3f v2);
}
