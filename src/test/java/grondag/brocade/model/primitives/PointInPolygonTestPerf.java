package grondag.exotic_matter.model.primitives;

import java.util.Random;

import org.junit.jupiter.api.Test;

import grondag.exotic_matter.model.primitives.PointInPolygonTest.DiscardAxis;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.vertex.IVec3f;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;

class PointInPolygonTestPerf {

    @Test
    void test() {
        doit();
        doit();
        doit();
    }

    // best
//    nanos per call: 120
//    nanos per call: 106
//    nanos per call: 104

//    nanos per call: 120
//    nanos per call: 103
//    nanos per call: 102

    private void doit() {
        final int samples = 10000000;
        long elapsed = 0;
        final Random r = new Random(42);
        int falsePositiveCount = 0;
        int falseNegativeCount = 0;
//        double matchArea = 0;
//        double missArea = 0;

        IMutablePolygon poly = PolyFactory.COMMON_POOL.newPaintable(3);
        for (int i = 0; i < samples; i++) {
            poly.setVertex(0, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0, 0, 0);
            poly.setVertex(1, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0, 0, 0);
            poly.setVertex(2, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0, 0, 0);
            Vec3f p = Vec3f.create(r.nextFloat(), r.nextFloat(), r.nextFloat());
            elapsed -= System.nanoTime();
            boolean b = PointInPolygonTest.isPointInPolygon(p, poly);
            elapsed += System.nanoTime();
//            double area = poly.getArea();
//            if(Double.isNaN(area))
//                area = 0;
            if (b == accuratePointInTriangle(p, poly)) {
//                matchArea += area;
            } else {
//                missArea += area;
                if (b)
                    falseNegativeCount++;
                else
                    falsePositiveCount++;
            }
        }
        poly.release();
        System.out.println("nanos per call: " + elapsed / samples);
        System.out.println("False positive rate %: " + falsePositiveCount * 100 / samples);
        System.out.println("False negative rate %: " + falseNegativeCount * 100 / samples);
//        if(matchCount != samples)
//        {
//            System.out.println("Match avg area: " + matchArea / matchCount);
//            System.out.println("Miss avg area: " + missArea / (samples - matchCount));
//        }
        System.out.println("");
    }

    //////////////// Below is adapted from
    //////////////// http://totologic.blogspot.com/2014/01/accurate-point-in-triangle-test.html
    //////////////// for testing

    final static float EPSILON = 0.001f;
    final static float EPSILON_SQUARE = EPSILON * EPSILON;

    public static float side(float x1, float y1, float x2, float y2, float x, float y) {
        return (y2 - y1) * (x - x1) + (-x2 + x1) * (y - y1);
    }

    public static boolean naivePointInTriangle(float x1, float y1, float x2, float y2, float x3, float y3, float x,
            float y) {
        boolean checkSide1 = side(x1, y1, x2, y2, x, y) >= 0;
        boolean checkSide2 = side(x2, y2, x3, y3, x, y) >= 0;
        boolean checkSide3 = side(x3, y3, x1, y1, x, y) >= 0;
        return checkSide1 && checkSide2 && checkSide3;
    }

    public static boolean pointInTriangleBoundingBox(float x1, float y1, float x2, float y2, float x3, float y3,
            float x, float y) {
        float xMin = Math.min(x1, Math.min(x2, x3)) - EPSILON;
        float xMax = Math.max(x1, Math.max(x2, x3)) + EPSILON;
        float yMin = Math.min(y1, Math.min(y2, y3)) - EPSILON;
        float yMax = Math.max(y1, Math.max(y2, y3)) + EPSILON;

        return !(x < xMin || xMax < x || y < yMin || yMax < y);
    }

    public static float distanceSquarePointToSegment(float x1, float y1, float x2, float y2, float x, float y) {
        float p1_p2_squareLength = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        float dotProduct = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / p1_p2_squareLength;
        if (dotProduct < 0) {
            return (x - x1) * (x - x1) + (y - y1) * (y - y1);
        } else if (dotProduct <= 1) {
            float p_p1_squareLength = (x1 - x) * (x1 - x) + (y1 - y) * (y1 - y);
            return p_p1_squareLength - dotProduct * dotProduct * p1_p2_squareLength;
        } else {
            return (x - x2) * (x - x2) + (y - y2) * (y - y2);
        }
    }

    public static boolean accuratePointInTriangle(float x, float y, float x1, float y1, float x2, float y2, float x3,
            float y3) {
        if (!pointInTriangleBoundingBox(x1, y1, x2, y2, x3, y3, x, y))
            return false;

        if (naivePointInTriangle(x1, y1, x2, y2, x3, y3, x, y))
            return true;

        if (distanceSquarePointToSegment(x1, y1, x2, y2, x, y) <= EPSILON_SQUARE)
            return true;
        if (distanceSquarePointToSegment(x2, y2, x3, y3, x, y) <= EPSILON_SQUARE)
            return true;
        if (distanceSquarePointToSegment(x3, y3, x1, y1, x, y) <= EPSILON_SQUARE)
            return true;

        return false;
    }

    public static boolean accuratePointInTriangle(IVec3f point, IMutablePolygon quad) {
        // faster to check in 2 dimensions, so throw away the axis
        // that is most orthogonal to our plane
        final PointInPolygonTest.DiscardAxis d = DiscardAxis.get(quad.getFaceNormal());
        final IVec3f v0 = quad.getPos(0);
        final IVec3f v1 = quad.getPos(1);
        final IVec3f v2 = quad.getPos(2);

        return accuratePointInTriangle(d.x(point), d.y(point), d.x(v0), d.y(v0), d.x(v1), d.y(v1), d.x(v2), d.y(v2));

    }
}
