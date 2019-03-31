package grondag.brocade.primitives;

import java.util.Random;

import org.junit.jupiter.api.Test;

import grondag.brocade.primitives.polygon.IMutablePolygon;

class TriangleBoxTestPerf {
    @Test
    void test() {
        for (int i = 0; i < 100; i++)
            doit();
    }

    // best
//    true count = 49995645  nanos per call: ~60
    // might be at timer resolution

    private void doit() {
        final int samples = 100000000;
        long elapsed = 0;
        final Random r = new Random(42);

        final float[] polyData = new float[36];
        int trueCount = 0;

        IMutablePolygon poly = PolyFactory.COMMON_POOL.newPaintable(3);

        for (int i = 0; i < samples; i++) {
            poly.setVertex(0, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0, 0, 0);
            poly.setVertex(1, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0, 0, 0);
            poly.setVertex(2, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0, 0, 0);
            TriangleBoxTest.packPolyData(poly.getPos(0), poly.getPos(1), poly.getPos(2), polyData);
            elapsed -= System.nanoTime();
            if (TriangleBoxTest.triBoxOverlap(0.25f, 0.25f, 0.25f, 0.25f, polyData))
                trueCount++;
            elapsed += System.nanoTime();
        }

        poly.release();
        System.out.println("true count = " + trueCount + "  nanos per call: " + elapsed / samples);
        System.out.println("");
    }
}
