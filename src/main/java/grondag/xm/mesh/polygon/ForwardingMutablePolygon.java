/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm.mesh.polygon;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.helper.FaceVertex;
import grondag.xm.mesh.vertex.Vec3f;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class ForwardingMutablePolygon extends ForwardingPolygon implements MutablePolygon {
    @Override
    public MutablePolygon spriteVertex(int layerIndex, int vertexIndex, float u, float v, int color, int glow) {
        ((MutablePolygon) wrapped).spriteVertex(layerIndex, vertexIndex, u, v, color, glow);
        return this;
    }

    @Override
    public MutablePolygon maxU(int layerIndex, float maxU) {
        ((MutablePolygon) wrapped).maxU(layerIndex, maxU);
        return this;
    }

    @Override
    public MutablePolygon maxV(int layerIndex, float maxV) {
        ((MutablePolygon) wrapped).maxV(layerIndex, maxV);
        return this;
    }

    @Override
    public MutablePolygon minU(int layerIndex, float minU) {
        ((MutablePolygon) wrapped).minU(layerIndex, minU);
        return this;
    }

    @Override
    public MutablePolygon minV(int layerIndex, float minV) {
        ((MutablePolygon) wrapped).minV(layerIndex, minV);
        return this;
    }

    @Override
    public MutablePolygon uvWrapDistance(float uvWrapDistance) {
        ((MutablePolygon) wrapped).uvWrapDistance(uvWrapDistance);
        return this;
    }

    @Override
    public MutablePolygon textureSalt(int salt) {
        ((MutablePolygon) wrapped).textureSalt(salt);
        return this;
    }

    @Override
    public MutablePolygon lockUV(int layerIndex, boolean lockUV) {
        ((MutablePolygon) wrapped).lockUV(layerIndex, lockUV);
        return this;
    }

    @Override
    public MutablePolygon sprite(int layerIndex, String textureName) {
        ((MutablePolygon) wrapped).sprite(layerIndex, textureName);
        return this;
    }

    @Override
    public MutablePolygon rotation(int layerIndex, Rotation rotation) {
        ((MutablePolygon) wrapped).rotation(layerIndex, rotation);
        return this;
    }

    @Override
    public MutablePolygon contractUV(int layerIndex, boolean contractUVs) {
        ((MutablePolygon) wrapped).contractUV(layerIndex, contractUVs);
        return this;
    }

    @Override
    public MutablePolygon blendMode(int layerIndex, BlockRenderLayer layer) {
        ((MutablePolygon) wrapped).blendMode(layerIndex, layer);
        return this;
    }

    @Override
    public MutablePolygon spriteDepth(int layerCount) {
        ((MutablePolygon) wrapped).spriteDepth(layerCount);
        return this;
    }

    @Override
    public MutablePolygon emissive(int textureLayerIndex, boolean emissive) {
        ((MutablePolygon) wrapped).emissive(textureLayerIndex, emissive);
        return this;
    }

    @Override
    public MutablePolygon vertex(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow) {
        ((MutablePolygon) wrapped).vertex(vertexIndex, x, y, z, u, v, color, glow);
        return this;
    }

    @Override
    public MutablePolygon pos(int vertexIndex, float x, float y, float z) {
        ((MutablePolygon) wrapped).pos(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public MutablePolygon pos(int vertexIndex, Vec3f pos) {
        ((MutablePolygon) wrapped).pos(vertexIndex, pos);
        return this;
    }

    @Override
    public MutablePolygon spriteColor(int vertexIndex, int layerIndex, int color) {
        ((MutablePolygon) wrapped).spriteColor(vertexIndex, layerIndex, color);
        return this;
    }

    @Override
    public MutablePolygon sprite(int vertexIndex, int layerIndex, float u, float v) {
        ((MutablePolygon) wrapped).sprite(vertexIndex, layerIndex, u, v);
        return this;
    }

    @Override
    public MutablePolygon vertexGlow(int vertexIndex, int glow) {
        ((MutablePolygon) wrapped).vertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public MutablePolygon normal(int vertexIndex, Vec3f normal) {
        ((MutablePolygon) wrapped).normal(vertexIndex, normal);
        return this;
    }

    @Override
    public MutablePolygon normal(int vertexIndex, float x, float y, float z) {
        ((MutablePolygon) wrapped).normal(vertexIndex, x, y, z);
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
    public MutablePolygon clearFaceNormal() {
        ((MutablePolygon) wrapped).clearFaceNormal();
        return this;
    }

    @Override
    public MutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, Direction topFace) {
        ((MutablePolygon) wrapped).setupFaceQuad(side, tv0, tv1, tv2, tv3, topFace);
        return this;
    }

    @Override
    public MutablePolygon setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2, FaceVertex vertexIn3, Direction topFace) {
        ((MutablePolygon) wrapped).setupFaceQuad(vertexIn0, vertexIn1, vertexIn2, vertexIn3, topFace);
        return this;
    }

    @Override
    public MutablePolygon setupFaceQuad(float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        ((MutablePolygon) wrapped).setupFaceQuad(x0, y0, x1, y1, depth, topFace);
        return this;
    }

    @Override
    public MutablePolygon setupFaceQuad(Direction face, float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        ((MutablePolygon) wrapped).setupFaceQuad(face, x0, y0, x1, y1, depth, topFace);
        return this;
    }

    @Override
    public MutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, Direction topFace) {
        ((MutablePolygon) wrapped).setupFaceQuad(side, tv0, tv1, tv2, topFace);
        return this;
    }

    @Override
    public MutablePolygon setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, Direction topFace) {
        ((MutablePolygon) wrapped).setupFaceQuad(tv0, tv1, tv2, topFace);
        return this;
    }

    @Override
    public MutablePolygon nominalFace(Direction face) {
        ((MutablePolygon) wrapped).nominalFace(face);
        return this;
    }

    @Override
    public MutablePolygon surface(XmSurface surface) {
        ((MutablePolygon) wrapped).surface(surface);
        return this;
    }

    @Override
    public MutablePolygon copyVertexFrom(int targetIndex, Polygon source, int sourceIndex) {
        ((MutablePolygon) wrapped).copyVertexFrom(targetIndex, source, sourceIndex);
        return this;
    }

    @Override
    public void copyFrom(Polygon polyIn, boolean includeVertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutablePolygon tag(int tag) {
        tagSetter.setValue(tag);
        return this;
    }
}
