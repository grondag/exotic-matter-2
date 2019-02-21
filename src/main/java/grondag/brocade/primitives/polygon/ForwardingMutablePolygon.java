package grondag.brocade.primitives.polygon;

import grondag.brocade.painting.Surface;
import grondag.brocade.primitives.FaceVertex;
import grondag.brocade.primitives.vertex.Vec3f;
import grondag.fermion.world.Rotation;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class ForwardingMutablePolygon extends ForwardingPolygon implements IMutablePolygon {
    @Override
    public IMutablePolygon setVertexLayer(int layerIndex, int vertexIndex, float u, float v, int color, int glow) {
        ((IMutablePolygon) wrapped).setVertexLayer(layerIndex, vertexIndex, u, v, color, glow);
        return this;
    }

    @Override
    public IMutablePolygon setMaxU(int layerIndex, float maxU) {
        ((IMutablePolygon) wrapped).setMaxU(layerIndex, maxU);
        return this;
    }

    @Override
    public IMutablePolygon setMaxV(int layerIndex, float maxV) {
        ((IMutablePolygon) wrapped).setMaxV(layerIndex, maxV);
        return this;
    }

    @Override
    public IMutablePolygon setMinU(int layerIndex, float minU) {
        ((IMutablePolygon) wrapped).setMinU(layerIndex, minU);
        return this;
    }

    @Override
    public IMutablePolygon setMinV(int layerIndex, float minV) {
        ((IMutablePolygon) wrapped).setMinV(layerIndex, minV);
        return this;
    }

    @Override
    public IMutablePolygon setTextureSalt(int salt) {
        ((IMutablePolygon) wrapped).setTextureSalt(salt);
        return this;
    }

    @Override
    public IMutablePolygon setLockUV(int layerIndex, boolean lockUV) {
        ((IMutablePolygon) wrapped).setLockUV(layerIndex, lockUV);
        return this;
    }

    @Override
    public IMutablePolygon setTextureName(int layerIndex, String textureName) {
        ((IMutablePolygon) wrapped).setTextureName(layerIndex, textureName);
        return this;
    }

    @Override
    public IMutablePolygon setRotation(int layerIndex, Rotation rotation) {
        ((IMutablePolygon) wrapped).setRotation(layerIndex, rotation);
        return this;
    }

    @Override
    public IMutablePolygon setShouldContractUVs(int layerIndex, boolean contractUVs) {
        ((IMutablePolygon) wrapped).setShouldContractUVs(layerIndex, contractUVs);
        return this;
    }

    @Override
    public IMutablePolygon setRenderLayer(int layerIndex, BlockRenderLayer layer) {
        ((IMutablePolygon) wrapped).setRenderLayer(layerIndex, layer);
        return this;
    }

    @Override
    public IMutablePolygon setLayerCount(int layerCount) {
        ((IMutablePolygon) wrapped).setLayerCount(layerCount);
        return this;
    }

    @Override
    public IMutablePolygon setEmissive(int textureLayerIndex, boolean emissive) {
        ((IMutablePolygon) wrapped).setEmissive(textureLayerIndex, emissive);
        return this;
    }

    @Override
    public IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color,
            int glow) {
        ((IMutablePolygon) wrapped).setVertex(vertexIndex, x, y, z, u, v, color, glow);
        return this;
    }

    @Override
    public IMutablePolygon setVertexPos(int vertexIndex, float x, float y, float z) {
        ((IMutablePolygon) wrapped).setVertexPos(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public IMutablePolygon setVertexPos(int vertexIndex, Vec3f pos) {
        ((IMutablePolygon) wrapped).setVertexPos(vertexIndex, pos);
        return this;
    }

    @Override
    public IMutablePolygon setVertexColor(int layerIndex, int vertexIndex, int color) {
        ((IMutablePolygon) wrapped).setVertexColor(layerIndex, vertexIndex, color);
        return this;
    }

    @Override
    public IMutablePolygon setVertexUV(int layerIndex, int vertexIndex, float u, float v) {
        ((IMutablePolygon) wrapped).setVertexUV(layerIndex, vertexIndex, u, v);
        return this;
    }

    @Override
    public IMutablePolygon setVertexGlow(int vertexIndex, int glow) {
        ((IMutablePolygon) wrapped).setVertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public IMutablePolygon setVertexNormal(int vertexIndex, Vec3f normal) {
        ((IMutablePolygon) wrapped).setVertexNormal(vertexIndex, normal);
        return this;
    }

    @Override
    public IMutablePolygon setVertexNormal(int vertexIndex, float x, float y, float z) {
        ((IMutablePolygon) wrapped).setVertexNormal(vertexIndex, x, y, z);
        return this;
    }

    // TODO: switch to materials
//    @Override
//    public IMutablePolygon setPipelineIndex(int pipelineIndex)
//    {
//        ((IMutablePolygon)wrapped).setPipelineIndex(pipelineIndex);
//        return this;
//    }

    @Override
    public IMutablePolygon clearFaceNormal() {
        ((IMutablePolygon) wrapped).clearFaceNormal();
        return this;
    }

    @Override
    public IMutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3,
            Direction topFace) {
        ((IMutablePolygon) wrapped).setupFaceQuad(side, tv0, tv1, tv2, tv3, topFace);
        return this;
    }

    @Override
    public IMutablePolygon setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2,
            FaceVertex vertexIn3, Direction topFace) {
        ((IMutablePolygon) wrapped).setupFaceQuad(vertexIn0, vertexIn1, vertexIn2, vertexIn3, topFace);
        return this;
    }

    @Override
    public IMutablePolygon setupFaceQuad(float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        ((IMutablePolygon) wrapped).setupFaceQuad(x0, y0, x1, y1, depth, topFace);
        return this;
    }

    @Override
    public IMutablePolygon setupFaceQuad(Direction face, float x0, float y0, float x1, float y1, float depth,
            Direction topFace) {
        ((IMutablePolygon) wrapped).setupFaceQuad(face, x0, y0, x1, y1, depth, topFace);
        return this;
    }

    @Override
    public IMutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2,
            Direction topFace) {
        ((IMutablePolygon) wrapped).setupFaceQuad(side, tv0, tv1, tv2, topFace);
        return this;
    }

    @Override
    public IMutablePolygon setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, Direction topFace) {
        ((IMutablePolygon) wrapped).setupFaceQuad(tv0, tv1, tv2, topFace);
        return this;
    }

    @Override
    public IMutablePolygon setNominalFace(Direction face) {
        ((IMutablePolygon) wrapped).setNominalFace(face);
        return this;
    }

    @Override
    public IMutablePolygon setSurface(Surface surface) {
        ((IMutablePolygon) wrapped).setSurface(surface);
        return this;
    }

    @Override
    public IMutablePolygon copyVertexFrom(int targetIndex, IPolygon source, int sourceIndex) {
        ((IMutablePolygon) wrapped).copyVertexFrom(targetIndex, source, sourceIndex);
        return this;
    }

    @Override
    public void copyFrom(IPolygon polyIn, boolean includeVertices) {
        throw new UnsupportedOperationException();
    }
}
