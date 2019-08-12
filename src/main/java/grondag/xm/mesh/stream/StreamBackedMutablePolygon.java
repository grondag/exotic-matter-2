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

package grondag.xm.mesh.stream;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.vertex.Vec3f;
import grondag.xm.surface.XmSurfaceImpl;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class StreamBackedMutablePolygon extends StreamBackedPolygon implements MutablePolygon {
    @Override
    public final MutablePolygon spriteVertex(int layerIndex, int vertexIndex, float u, float v, int color, int glow) {
        vertexIndex = vertexIndexer.applyAsInt(vertexIndex);
        spriteColor(vertexIndex, layerIndex, color);
        sprite(vertexIndex, layerIndex, u, v);
        vertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public final MutablePolygon maxU(int layerIndex, float maxU) {
        polyEncoder.setMaxU(stream, baseAddress, layerIndex, maxU);
        return this;
    }

    @Override
    public final MutablePolygon maxV(int layerIndex, float maxV) {
        polyEncoder.setMaxV(stream, baseAddress, layerIndex, maxV);
        return this;
    }

    @Override
    public final MutablePolygon minU(int layerIndex, float minU) {
        polyEncoder.setMinU(stream, baseAddress, layerIndex, minU);
        return this;
    }

    @Override
    public final MutablePolygon minV(int layerIndex, float minV) {
        polyEncoder.setMinV(stream, baseAddress, layerIndex, minV);
        return this;
    }

    @Override
    public MutablePolygon uvWrapDistance(float uvWrapDistance) {
        StaticEncoder.uvWrapDistance(stream, baseAddress, uvWrapDistance);
        return this;
    }

    @Override
    public final MutablePolygon textureSalt(int salt) {
        StaticEncoder.setTextureSalt(stream, baseAddress, salt);
        return this;
    }

    @Override
    public final MutablePolygon lockUV(int layerIndex, boolean lockUV) {
        StaticEncoder.setLockUV(stream, baseAddress, layerIndex, lockUV);
        return this;
    }

    @Override
    public final MutablePolygon sprite(int layerIndex, String textureName) {
        polyEncoder.setTextureName(stream, baseAddress, layerIndex, textureName);
        return this;
    }

    @Override
    public final MutablePolygon rotation(int layerIndex, Rotation rotation) {
        StaticEncoder.setRotation(stream, baseAddress, layerIndex, rotation);
        return this;
    }

    @Override
    public final MutablePolygon contractUV(int layerIndex, boolean contractUVs) {
        StaticEncoder.setContractUVs(stream, baseAddress, layerIndex, contractUVs);
        return this;
    }

    @Override
    public final MutablePolygon blendMode(int layerIndex, BlockRenderLayer layer) {
        StaticEncoder.setRenderLayer(stream, baseAddress, layerIndex, layer);
        return this;
    }

    /**
     * Throws exception if not a mutable format.
     */
    @Override
    public final MutablePolygon spriteDepth(int layerCount) {
        final int format = format();
        if (!PolyStreamFormat.isMutable(format))
            throw new UnsupportedOperationException("Cannot change layer count on immutable polygon");
        setFormat(PolyStreamFormat.setLayerCount(format, layerCount));
        return this;
    }

    @Override
    public final MutablePolygon emissive(int layerIndex, boolean emissive) {
        StaticEncoder.setEmissive(stream, baseAddress, layerIndex, emissive);
        return this;
    }

    @Override
    public final MutablePolygon vertex(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow) {
        vertexIndex = vertexIndexer.applyAsInt(vertexIndex);
        pos(vertexIndex, x, y, z);
        sprite(vertexIndex, 0, u, v);
        spriteColor(vertexIndex, 0, color);
        vertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public final MutablePolygon pos(int vertexIndex, float x, float y, float z) {
        vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), x, y, z);
        return this;
    }

    @Override
    public final MutablePolygon pos(int vertexIndex, Vec3f pos) {
        vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), pos.x(), pos.y(), pos.z());
        return this;
    }

    @Override
    public final MutablePolygon spriteColor(int vertexIndex, int layerIndex, int color) {
        if (vertexEncoder.hasColor())
            vertexEncoder.setVertexColor(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), color);
        else
            polyEncoder.setVertexColor(stream, baseAddress, layerIndex, color);
        return this;
    }

    @Override
    public final MutablePolygon spriteU(int vertexIndex, int layerIndex, float u) {
        vertexEncoder.setVertexU(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), u);
        return this;
    }

    @Override
    public final MutablePolygon spriteV(int vertexIndex, int layerIndex, float v) {
        vertexEncoder.setVertexV(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), v);
        return this;
    }

    @Override
    public final MutablePolygon sprite(int vertexIndex, int layerIndex, float u, float v) {
        vertexEncoder.setVertexUV(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), u, v);
        return this;
    }

    @Override
    public final MutablePolygon vertexGlow(int vertexIndex, int glow) {
        glowEncoder.setGlow(stream, glowAddress, vertexIndexer.applyAsInt(vertexIndex), glow);
        return this;
    }

    @Override
    public final MutablePolygon normal(int vertexIndex, Vec3f normal) {
        if (vertexEncoder.hasNormals()) {
            if (normal == null)
                vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), Float.NaN, Float.NaN, Float.NaN);
            else
                vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), normal.x(), normal.y(), normal.z());
        }
        return this;
    }

    @Override
    public final MutablePolygon normal(int vertexIndex, float x, float y, float z) {
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
    public final MutablePolygon clearFaceNormal() {
        int normalFormat = PolyStreamFormat.getFaceNormalFormat(format());

        assert normalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED : "Face normal clear should only happen for full-precision normals";

        polyEncoder.clearFaceNormal(stream, baseAddress);
        return this;
    }

    @Override
    public final MutablePolygon nominalFace(Direction face) {
        setFormat(PolyStreamFormat.setNominalFace(format(), face));
        return this;
    }

    public final MutablePolygon surface(XmSurfaceImpl surface) {
        StaticEncoder.surface(stream, baseAddress, surface);
        return this;
    }

    @Override
    public MutablePolygon surface(XmSurface surface) {
        return surface((XmSurfaceImpl) surface);
    }

    @Override
    public final MutablePolygon copyVertexFrom(int targetIndex, Polygon source, int sourceIndex) {
        if (source.hasNormal(sourceIndex)) {
            assert vertexEncoder.hasNormals();
            normal(targetIndex, source.normalX(sourceIndex), source.normalY(sourceIndex), source.normalZ(sourceIndex));
        } else if (vertexEncoder.hasNormals())
            this.normal(targetIndex, (Vec3f) null);

        pos(targetIndex, source.x(sourceIndex), source.y(sourceIndex), source.z(sourceIndex));

        if (glowEncoder.glowFormat() == PolyStreamFormat.VERTEX_GLOW_PER_VERTEX)
            vertexGlow(targetIndex, source.getVertexGlow(sourceIndex));
        else if (targetIndex == 0 && glowEncoder.glowFormat() == PolyStreamFormat.VERTEX_GLOW_SAME)
            vertexGlow(0, source.getVertexGlow(sourceIndex));

        final int layerCount = source.spriteDepth();
        assert layerCount <= spriteDepth();

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
    public final void copyFrom(Polygon polyIn, boolean includeVertices) {
        // PERF: make this faster for other stream-based polys
        nominalFace(polyIn.nominalFace());

        final int faceNormalFormat = PolyStreamFormat.getFaceNormalFormat(format());
        if (faceNormalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED)
            clearFaceNormal();
        else if (faceNormalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_QUANTIZED)
            polyEncoder.setFaceNormal(stream, faceNormalFormat, polyIn.faceNormal());

        final int layerCount = polyIn.spriteDepth();
        assert layerCount == spriteDepth();

        textureSalt(polyIn.textureSalt());
        surface(polyIn.surface());
        uvWrapDistance(polyIn.uvWrapDistance());

        for (int l = 0; l < layerCount; l++) {
            maxU(l, polyIn.maxU(l));
            maxV(l, polyIn.maxV(l));
            minU(l, polyIn.minU(l));
            minV(l, polyIn.minV(l));
            emissive(l, polyIn.emissive(l));
            blendMode(l, polyIn.blendMode(l));
            lockUV(l, polyIn.lockUV(l));
            contractUV(l, polyIn.shouldContractUVs(l));
            rotation(l, polyIn.getRotation(l));
            sprite(l, polyIn.spriteName(l));
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
    public void copyFromCSG(Polygon polyIn) {
        copyFrom(polyIn, false);
        tag(polyIn.tag());
        setMark(polyIn.isMarked());
    }

    public void loadStandardDefaults() {
        maxU(0, 1f);
        maxU(1, 1f);
        maxU(2, 1f);

        maxV(0, 1f);
        maxV(1, 1f);
        maxV(2, 1f);

        clearFaceNormal();
    }

    @Override
    public MutablePolygon tag(int tag) {
        polyEncoder.setTag(stream, baseAddress, tag);
        return this;
    }
}
