package grondag.exotic_matter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import grondag.brocade.primitives.vertex.Vec3f;

class VertexTest {

    @Test
    void test() {
        Vec3f.Mutable testPoint = new Vec3f.Mutable(0, 0, 0);
        testPoint.load(.5f, .5f, .5f);

        assertTrue(testPoint.isOnLine(0, 0, 0, 1, 1, 1));
        assertTrue(testPoint.isOnLine(.5f, 0, .5f, .5f, 1, .5f));
        assertFalse(testPoint.isOnLine(.7f, 2, .1f, 0, -1, .25f));

        testPoint.load(.6f, .4f, .5333333333f);
        assertTrue(testPoint.isOnLine(0.6f, 0.4f, 0.4f, 0.6f, .4f, .6f));
    }

}
