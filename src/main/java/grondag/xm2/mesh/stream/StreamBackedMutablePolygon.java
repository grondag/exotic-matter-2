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

package grondag.xm2.mesh.stream;

import grondag.fermion.spatial.Rotation;
import grondag.xm2.api.surface.XmSurface;
import grondag.xm2.mesh.polygon.IMutablePolygon;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.mesh.vertex.Vec3f;
import grondag.xm2.surface.XmSurfaceImpl;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class StreamBackedMutablePolygon extends StreamBackedPolygon implements IMutablePolygon {
    @Override
    public final IMutablePolygon setVertexLayer(int layerIndex, int vertexIndex, float u, float v, int color, int glow) {
        vertexIndex = vertexIndexer.applyAsInt(vertexIndex);
        spriteColor(vertexIndex, layerIndex, color);
        sprite(vertexIndex, layerIndex, u, v);
        setVertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public final IMutablePolygon setMaxU(int layerIndex, float maxU) {
        polyEncoder.setMaxU(stream, baseAddress, layerIndex, maxU);
        return this;
    }

    @Override
    public final IMutablePolygon setMaxV(int layerIndex, float maxV) {
        polyEncoder.setMaxV(stream, baseAddress, layerIndex, maxV);
        return this;
    }

    @Override
    public final IMutablePolygon setMinU(int layerIndex, float minU) {
        polyEncoder.setMinU(stream, baseAddress, layerIndex, minU);
        return this;
    }

    @Override
    public final IMutablePolygon setMinV(int layerIndex, float minV) {
        polyEncoder.setMinV(stream, baseAddress, layerIndex, minV);
        return this;
    }

    @Override
    public IMutablePolygon uvWrapDistance(float uvWrapDistance) {
        StaticEncoder.uvWrapDistance(stream, baseAddress, uvWrapDistance);
        return this;
    }

    @Override
    public final IMutablePolygon setTextureSalt(int salt) {
        StaticEncoder.setTextureSalt(stream, baseAddress, salt);
        return this;
    }

    @Override
    public final IMutablePolygon setLockUV(int layerIndex, boolean lockUV) {
        StaticEncoder.setLockUV(stream, baseAddress, layerIndex, lockUV);
        return this;
    }

    @Override
    public final IMutablePolygon setTextureName(int layerIndex, String textureName) {
        polyEncoder.setTextureName(stream, baseAddress, layerIndex, textureName);
        return this;
    }

    @Override
    public final IMutablePolygon setRotation(int layerIndex, Rotation rotation) {
        StaticEncoder.setRotation(stream, baseAddress, layerIndex, rotation);
        return this;
    }

    @Override
    public final IMutablePolygon setShouldContractUVs(int layerIndex, boolean contractUVs) {
        StaticEncoder.setContractUVs(stream, baseAddress, layerIndex, contractUVs);
        return this;
    }

    @Override
    public final IMutablePolygon setRenderLayer(int layerIndex, BlockRenderLayer layer) {
        StaticEncoder.setRenderLayer(stream, baseAddress, layerIndex, layer);
        return this;
    }

    /**
     * Throws exception if not a mutable format.
     */
    @Override
    public final IMutablePolygon setLayerCount(int layerCount) {
        final int format = format();
        if (!PolyStreamFormat.isMutable(format))
            throw new UnsupportedOperationException("Cannot change layer count on immutable polygon");
        setFormat(PolyStreamFormat.setLayerCount(format, layerCount));
        return this;
    }

    @Override
    public final IMutablePolygon setEmissive(int layerIndex, boolean emissive) {
        StaticEncoder.setEmissive(stream, baseAddress, layerIndex, emissive);
        return this;
    }

    @Override
    public final IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow) {
        vertexIndex = vertexIndexer.applyAsInt(vertexIndex);
        pos(vertexIndex, x, y, z);
        sprite(vertexIndex, 0, u, v);
        spriteColor(vertexIndex, 0, color);
        setVertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public final IMutablePolygon pos(int vertexIndex, float x, float y, float z) {
        vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), x, y, z);
        return this;
    }

    @Override
    public final IMutablePolygon pos(int vertexIndex, Vec3f pos) {
        vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), pos.x(), pos.y(), pos.z());
        return this;
    }

    @Override
    public final IMutablePolygon spriteColor(int vertexIndex, int layerIndex, int color) {
        if (vertexEncoder.hasColor())
            vertexEncoder.setVertexColor(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), color);
        else
            polyEncoder.setVertexColor(stream, baseAddress, layerIndex, color);
        return this;
    }

    @Override
    public final IMutablePolygon spriteU(int vertexIndex, int layerIndex, float u) {
        vertexEncoder.setVertexU(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), u);
        return this;
    }

    @Override
    public final IMutablePolygon spriteV(int vertexIndex, int layerIndex, float v) {
        vertexEncoder.setVertexV(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), v);
        return this;
    }

    @Override
    public final IMutablePolygon sprite(int vertexIndex, int layerIndex, float u, float v) {
        vertexEncoder.setVertexUV(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), u, v);
        return this;
    }

    @Override
    public final IMutablePolygon setVertexGlow(int vertexIndex, int glow) {
        glowEncoder.setGlow(stream, glowAddress, vertexIndexer.applyAsInt(vertexIndex), glow);
        return this;
    }

    @Override
    public final IMutablePolygon normal(int vertexIndex, Vec3f normal) {
        if (vertexEncoder.hasNormals()) {
            if (normal == null)
                vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), Float.NaN, Float.NaN, Float.NaN);
            else
                vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), normal.x(), normal.y(), normal.z());
        }
        return this;
    }

    @Override
    public final IMutablePolygon normal(int vertexIndex, float x, float y, float z) {
        if (vertexEncoder.hasNormals())
            vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), x, y, z);
        return this;
    }

    // TODO replace with render material
//    @Override
//    public final IMutablePolygon setPipelineIndex(int pipelineIndex) {
//        StaticEncoder.setPipelineIndex(stream, baseAddress, pipelineIndex);
//        return this;
//    }

    @Override
    public final IMutablePolygon clearFaceNormal() {
        int normalFormat = PolyStreamFormat.getFaceNormalFormat(format());

        assert normalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED : "Face normal clear should only happen for full-precision normals";

        polyEncoder.clearFaceNormal(stream, baseAddress);
        return this;
    }

    @Override
    public final IMutablePolygon setNominalFace(Direction face) {
        setFormat(PolyStreamFormat.setNominalFace(format(), face));
        return this;
    }

    public final IMutablePolygon surface(XmSurfaceImpl surface) {
        StaticEncoder.surface(stream, baseAddress, surface);
        return this;
    }

    @Override
    public IMutablePolygon surface(XmSurface surface) {
        return surface((XmSurfaceImpl) surface);
    }

    @Override
    public final IMutablePolygon copyVertexFrom(int targetIndex, IPolygon source, int sourceIndex) {
        if (source.hasNormal(sourceIndex)) {
            assert vertexEncoder.hasNormals();
            normal(targetIndex, source.normalX(sourceIndex), source.normalY(sourceIndex), source.normalZ(sourceIndex));
        } else if (vertexEncoder.hasNormals())
            this.normal(targetIndex, (Vec3f) null);

        pos(targetIndex, source.x(sourceIndex), source.y(sourceIndex), source.z(sourceIndex));

        if (glowEncoder.glowFormat() == PolyStreamFormat.VERTEX_GLOW_PER_VERTEX)
            setVertexGlow(targetIndex, source.getVertexGlow(sourceIndex));
        else if (targetIndex == 0 && glowEncoder.glowFormat() == PolyStreamFormat.VERTEX_GLOW_SAME)
            setVertexGlow(0, source.getVertexGlow(sourceIndex));

        final int layerCount = source.layerCount();
        assert layerCount <= layerCount();

        // do for all vertices even if all the same - slightly wasteful but fewer logic
        // paths
        spriteColor(targetIndex, 0, source.spriteColor(sourceIndex, 0));
        if (layerCount > 1) {
            spriteColor(targetIndex, 1, source.spriteColor(sourceIndex, 1));

            if (layerCount == 3)
                spriteColor(targetIndex, 2, source.spriteColor(sourceIndex, 2));
        }

        sprite(targetIndex, 0, source.spriteU(sourceIndex, 0), source.spriteV(sourceIndex, 0));
        if (vertexEncoder.multiUV() && layerCount > 1) {
            sprite(targetIndex, 1, source.spriteU(sourceIndex, 1), source.spriteV(sourceIndex, 1));

            if (layerCount == 3)
                sprite(targetIndex, 2, source.spriteU(sourceIndex, 2), source.spriteV(sourceIndex, 2));
        }

        return this;
    }

    @Override
    public final void copyFrom(IPolygon polyIn, boolean includeVertices) {
        // PERF: make this faster for other stream-based polys
        setNominalFace(polyIn.nominalFace());

        final int faceNormalFormat = PolyStreamFormat.getFaceNormalFormat(format());
        if (faceNormalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED)
            clearFaceNormal();
        else if (faceNormalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_QUANTIZED)
            polyEncoder.setFaceNormal(stream, faceNormalFormat, polyIn.getFaceNormal());

        final int layerCount = polyIn.layerCount();
        assert layerCount == layerCount();

        setTextureSalt(polyIn.getTextureSalt());
        surface(polyIn.surface());
        uvWrapDistance(polyIn.uvWrapDistance());

        for (int l = 0; l < layerCount; l++) {
            setMaxU(l, polyIn.getMaxU(l));
            setMaxV(l, polyIn.getMaxV(l));
            setMinU(l, polyIn.getMinU(l));
            setMinV(l, polyIn.getMinV(l));
            setEmissive(l, polyIn.isEmissive(l));
            setRenderLayer(l, polyIn.getRenderLayer(l));
            setLockUV(l, polyIn.isLockUV(l));
            setShouldContractUVs(l, polyIn.shouldContractUVs(l));
            setRotation(l, polyIn.getRotation(l));
            setTextureName(l, polyIn.getTextureName(l));
        }

        if (includeVertices) {
            final int vertexCount = polyIn.vertexCount();
            if (vertexCount() == vertexCount) {
                for (int i = 0; i < vertexCount; i++)
                    this.copyVertexFrom(i, polyIn, i);
            } else
                throw new UnsupportedOperationException("Polygon vertex counts must match when copying vertex data.");

        }
    }

    /**
     * Specialized version for CSG operations. Never includes vertex info and does
     * include marks and tags.
     */
    public void copyFromCSG(IPolygon polyIn) {
        copyFrom(polyIn, false);
        tag(polyIn.tag());
        setMark(polyIn.isMarked());
    }

    public void loadStandardDefaults() {
        setMaxU(0, 1f);
        setMaxU(1, 1f);
        setMaxU(2, 1f);

        setMaxV(0, 1f);
        setMaxV(1, 1f);
        setMaxV(2, 1f);

        clearFaceNormal();
    }

    @Override
    public IMutablePolygon tag(int tag) {
        polyEncoder.setTag(stream, baseAddress, tag);
        return this;
    }
}
