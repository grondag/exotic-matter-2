package grondag.exotic_matter;

import java.util.Random;

import org.junit.Test;

import grondag.brocade.painting.Surface;
import grondag.brocade.painting.SurfaceTopology;
import grondag.brocade.primitives.FaceVertex;
import grondag.brocade.primitives.QuadHelper;
import grondag.brocade.primitives.polygon.IMutablePolygon;
import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.stream.SimpleMutablePolygon;
import grondag.brocade.primitives.vertex.Vec3f;
import grondag.fermion.world.Rotation;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;;

public class PolyTest {
    private IMutablePolygon newPoly(int vertexCount) {
        // return COMMON_POOL.newPaintable(4);
        SimpleMutablePolygon streamPoly = new SimpleMutablePolygon();
        streamPoly.prepare(1, vertexCount);
        return streamPoly;
    }

    @Test
    public void test() {

        IMutablePolygon quad = newPoly(4).setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0.5, Direction.NORTH);

        // basic properties
        assert quad.layerCount() == 1;
        assert !quad.isLockUV(0);
        assert quad.shouldContractUVs(0);
        assert quad.getNominalFace() == Direction.UP;
        assert quad.getRotation(0) == Rotation.ROTATE_NONE;
        assert quad.getRenderLayer(0) == BlockRenderLayer.SOLID;

        quad.setLockUV(0, true);
        quad.setShouldContractUVs(0, false);
        quad.setNominalFace(Direction.DOWN);
        quad.setRotation(0, Rotation.ROTATE_270);
        quad.setRenderLayer(0, BlockRenderLayer.TRANSLUCENT);

        assert quad.isLockUV(0);
        assert !quad.shouldContractUVs(0);
        assert quad.getNominalFace() == Direction.DOWN;
        assert quad.getRotation(0) == Rotation.ROTATE_270;
        assert quad.getRenderLayer(0) == BlockRenderLayer.TRANSLUCENT;
        quad.release();

        // convexity & area tests
        quad = newPoly(4).setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0.5, Direction.NORTH);
        assert quad.isConvex();
        assert Math.abs(quad.getArea() - 1.0) < QuadHelper.EPSILON;
        quad.release();

        quad = newPoly(3).setupFaceQuad(Direction.UP, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0),
                new FaceVertex(1, 1, 0), Direction.NORTH);
        assert quad.getFaceNormal().equals(Vec3f.forFace(Direction.UP));
        assert quad.getFaceNormal().equals(quad.computeFaceNormal());
        assert quad.isConvex();
        assert quad.vertexCount() == 3;

        assert Math.abs(quad.getArea() - 0.5) < QuadHelper.EPSILON;
        quad.release();

        quad = newPoly(4).setupFaceQuad(Direction.UP, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0),
                new FaceVertex(1, 1, 0), new FaceVertex(0.9f, 0.1f, 0), Direction.NORTH);
        assert !quad.isConvex();
        quad.release();

        // normal facing calculation
        quad = newPoly(4).setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0.5, Direction.NORTH);
        assert quad.getNormalFace() == Direction.UP;
        quad.release();

        quad = newPoly(4).setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0.5, Direction.NORTH);
        assert quad.getNormalFace() == Direction.DOWN;
        quad.release();

        quad = newPoly(4).setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0.5, Direction.UP);
        assert quad.getNormalFace() == Direction.EAST;
        quad.release();

        quad = newPoly(4).setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0.5, Direction.NORTH);
        assert quad.getNormalFace() == Direction.DOWN;
        quad.release();

        quad = newPoly(4).setupFaceQuad(Direction.SOUTH, new FaceVertex(0, 0, 0.1f), new FaceVertex(1, 0, 0.1f),
                new FaceVertex(1, 1, 0), new FaceVertex(0.9f, 0.1f, 0), Direction.UP);
        assert quad.getNormalFace() == Direction.SOUTH;
        quad.release();

        // exercise all the constructors, getters and setters
        for (int vertexCount = 3; vertexCount < 17; vertexCount++) {
            testMutable(vertexCount, 1);
            testMutable(vertexCount, 2);
            testMutable(vertexCount, 3);
        }
    }

    private final Random r = new Random();

    private void testMutable(int vertexCount, int layerCount) {
        IMutablePolygon poly = newPoly(vertexCount);

        assert poly.vertexCount() == vertexCount;

        poly.setLayerCount(layerCount);
        assert poly.layerCount() == layerCount;

        Surface s = Surface.builder(SurfaceTopology.CUBIC).build();
        poly.setSurface(s);
        assert poly.getSurface() == s;

        Direction face = Direction.VALUES[r.nextInt(6)];
        poly.setNominalFace(face);
        assert poly.getNominalFace() == face;

        for (int v = 0; v < vertexCount; v++) {
            int g = r.nextInt(256);
            poly.setVertexGlow(v, g);

            float x = r.nextFloat();
            float y = r.nextFloat();
            float z = r.nextFloat();
            poly.setVertexPos(v, x, y, z);

            Vec3f vec = poly.getPos(v);
            assert vec.x() == x;
            assert vec.y() == y;
            assert vec.z() == z;

            poly.setVertexPos(v, -1.01f, 0, 1.01f);
            vec = poly.getPos(v);
            assert vec.x() == -1.01f;
            assert vec.y() == 0;
            assert vec.z() == 1.01f;

            poly.setVertexPos(v, Vec3f.create(x, y, z));
            assert poly.getVertexX(v) == x;
            assert poly.getVertexY(v) == y;
            assert poly.getVertexZ(v) == z;

            assert !poly.hasVertexNormal(v);

            x = r.nextFloat();
            y = r.nextFloat();
            z = r.nextFloat();
            poly.setVertexNormal(v, x, y, z);

            vec = poly.getVertexNormal(v);
            assert vec.x() == x;
            assert vec.y() == y;
            assert vec.z() == z;
            assert poly.hasVertexNormal(v);

            poly.setVertexNormal(v, null);
            assert poly.getVertexNormalX(v) == poly.getFaceNormalX();
            assert poly.getVertexNormalY(v) == poly.getFaceNormalY();
            assert poly.getVertexNormalZ(v) == poly.getFaceNormalZ();
            assert !poly.hasVertexNormal(v);

            poly.setVertexNormal(v, Vec3f.create(x, y, z));
            assert poly.getVertexNormalX(v) == x;
            assert poly.getVertexNormalY(v) == y;
            assert poly.getVertexNormalZ(v) == z;

            assert poly.getVertexGlow(v) == g;

        }

        testMutableLayer(poly, vertexCount, 0);
        if (layerCount > 1) {
            testMutableLayer(poly, vertexCount, 1);
            if (layerCount == 3)
                testMutableLayer(poly, vertexCount, 2);
        }

        IMutablePolygon copy = poly.claimCopy();

        comparePolys(poly, copy);

        IPolygon a = poly.toPainted();
        comparePolys(poly, a);

        poly.release();
        IPolygon b = copy.toPainted();
        copy.release();

        comparePolys(a, b);
    }

    private void testMutableLayer(IMutablePolygon poly, int vertexCount, int layer) {
        int color = r.nextInt();
        poly.setColor(layer, color);

        boolean e = r.nextBoolean();
        poly.setEmissive(layer, e);

        boolean l = r.nextBoolean();
        poly.setLockUV(layer, l);

        float u0 = r.nextFloat();
        float u1 = r.nextFloat();
        float v0 = r.nextFloat();
        float v1 = r.nextFloat();

        poly.setMinU(layer, u0);
        poly.setMaxU(layer, u1);
        poly.setMinV(layer, v0);
        poly.setMaxV(layer, v1);

        BlockRenderLayer bl = BlockRenderLayer.values()[r.nextInt(4)];
        poly.setRenderLayer(layer, bl);

        Rotation rot = Rotation.VALUES[r.nextInt(4)];
        poly.setRotation(layer, rot);

        boolean cuv = r.nextBoolean();
        poly.setShouldContractUVs(layer, cuv);

        String tex = Double.toString(r.nextDouble());
        poly.setTextureName(layer, tex);

        int salt = r.nextInt(256);
        poly.setTextureSalt(salt);

        assert poly.isEmissive(layer) == e;
        assert poly.isLockUV(layer) == l;
        assert poly.getMinU(layer) == u0;
        assert poly.getMaxU(layer) == u1;
        assert poly.getMinV(layer) == v0;
        assert poly.getMaxV(layer) == v1;
        assert poly.getRenderLayer(layer) == bl;
        assert poly.getRotation(layer) == rot;
        assert poly.shouldContractUVs(layer) == cuv;
        assert poly.textureName(layer) == tex;
        assert poly.getTextureSalt() == salt;

        for (int v = 0; v < vertexCount; v++) {
            assert poly.getVertexColor(layer, v) == color;
            int c = r.nextInt();
            poly.setVertexColor(layer, v, c);
            float u = r.nextFloat();
            poly.setVertexU(layer, v, u);
            float vVal = r.nextFloat();
            poly.setVertexV(layer, v, vVal);

            assert poly.getVertexColor(layer, v) == c;
            assert poly.getVertexU(layer, v) == u;
            assert poly.getVertexV(layer, v) == vVal;
        }
    }

    private void comparePolys(IPolygon a, IPolygon b) {
        assert a.vertexCount() == b.vertexCount();
        assert a.layerCount() == b.layerCount();
        assert a.getSurface() == b.getSurface();
        assert a.getNominalFace() == b.getNominalFace();

        for (int v = 0; v < a.vertexCount(); v++) {
            assert a.getVertexX(v) == b.getVertexX(v);
            assert a.getVertexY(v) == b.getVertexY(v);
            assert a.getVertexZ(v) == b.getVertexZ(v);
            assert a.getPos(v).equals(b.getPos(v));

            assert a.hasVertexNormal(v) == b.hasVertexNormal(v);

            assert a.getVertexNormalX(v) == b.getVertexNormalX(v);
            assert a.getVertexNormalY(v) == b.getVertexNormalY(v);
            assert a.getVertexNormalZ(v) == b.getVertexNormalZ(v);
            assert a.getVertexNormal(v).equals(b.getVertexNormal(v));

            assert a.getVertexGlow(v) == b.getVertexGlow(v);
        }

        compareLayers(a, b, 0);
        if (a.layerCount() > 1) {
            compareLayers(a, b, 1);
            if (a.layerCount() == 3)
                compareLayers(a, b, 2);
        }
    }

    private void compareLayers(IPolygon a, IPolygon b, int layer) {
        assert a.isEmissive(layer) == b.isEmissive(layer);
        assert a.isLockUV(layer) == b.isLockUV(layer);
        assert a.getMinU(layer) == b.getMinU(layer);
        assert a.getMaxU(layer) == b.getMaxU(layer);
        assert a.getMinV(layer) == b.getMinV(layer);
        assert a.getMaxV(layer) == b.getMaxV(layer);
        assert a.getRenderLayer(layer) == b.getRenderLayer(layer);
        assert a.getRotation(layer) == b.getRotation(layer);
        assert a.shouldContractUVs(layer) == b.shouldContractUVs(layer);
        assert a.textureName(layer) == b.textureName(layer);
        assert a.getTextureSalt() == b.getTextureSalt();

        for (int v = 0; v < a.vertexCount(); v++) {
            assert a.getVertexColor(layer, v) == b.getVertexColor(layer, v);
            assert a.getVertexU(layer, v) == b.getVertexU(layer, v);
            assert a.getVertexV(layer, v) == b.getVertexV(layer, v);
        }
    }

}