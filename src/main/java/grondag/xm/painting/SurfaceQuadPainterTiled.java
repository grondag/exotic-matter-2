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

package grondag.xm.painting;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import grondag.fermion.varia.Useful;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureRotation;
import grondag.xm.api.texture.TextureSet;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

/**
 * 
 * See {@link SurfaceTopology#TILED}
 */
public abstract class SurfaceQuadPainterTiled extends QuadPainter {
    /**
     * The smallest UV distance that can be tiled with single texture. Equivalently,
     * the size of each tile in the tiling grid. If no wrap, is simply the texture
     * size. If we wrap, then pick the distance that gives the texture scaling
     * closest to 1:1.
     */
    private static float tilingDistance(float uvWrapDistance, int textureScale) {
        // if wrap disabled use texture scale and paint at 1:1
        if (uvWrapDistance <= PolyHelper.EPSILON)
            return textureScale;

        // if texture is larger than wrap distance, must scale down to the wrap distance
        if (textureScale > uvWrapDistance)
            return uvWrapDistance;

        /*
         * Examples Wrap = 6, texScale = 2, divisor = 3, -> 2 Wrap = 7, texScale = 2,
         * divisor = 3, -> 7/3 Wrap = 7, texScale = 4, divisor = 2, -> 7/2
         */

        // subtract epsilon because want to favor rounding down at midpoint - fewer
        // polygons
        return uvWrapDistance / Math.round((uvWrapDistance / textureScale) - PolyHelper.EPSILON);
    }

    /**
     * 
     * If quad at the target address is within the given u bounds, does nothing to
     * it and passes address to consumer, and then returns
     * {@link Polygon#NO_ADDRESS};
     * 
     * Otherwise, slices off a quad from the poly at the target address, with u
     * value between low bound + span, with the assumption no vertices are below the
     * lower bound. Appends the new quad to the end of the stream, and passes its
     * address to the consumer. The original quad is marked as deleted.
     * <p>
     * 
     * Output quads will have uMin/uMax of 0,1 corresponding to the given split
     * bounds and all vertices will be scaled to that range. This will be true even
     * for quads that covered a multiple or fraction of a 0-1 range, or which were
     * offset.
     * <p>
     * 
     * Also appends a new quad containing the remnants of the original polygon less
     * the slice. This remainder quad will have the same uMin/uMax as the input
     * quad, and all vertices will remain scaled to that range. Address of remainder
     * quad is the return value.
     * <p>
     * 
     * EXCEPT, if all remainder vertices are within the given split bounds, will
     * instead apply offset and scaled as if it had been sliced, and pass it to the
     * consumer. Will then return {@link Polygon#NO_ADDRESS}.
     */
    private static int splitU(MutableMesh stream, int targetAddress, int layerIndex, final float uSplitLow, final float uSpan, IntConsumer vSplitter) {
        Polygon reader = stream.reader(targetAddress);
        final float uMin = reader.minU(layerIndex);
        final float uMax = reader.maxU(layerIndex);
        final int vCountIn = reader.vertexCount();

        // PERF: make this threadlocal
        final float[] vertexU = new float[vCountIn];
        final boolean flipped = uSpan < 0;

        /** points fully within the slice */
        int sliceCount = 0;
        /** points fully within the remainder */
        int remainderCount = 0;

        for (int i = 0; i < vCountIn; i++) {
            // vertex u where 0 corresponds to low split and 1 to high split
            final float u = (uMin + reader.spriteU(i, layerIndex) * (uMax - uMin) - uSplitLow) / uSpan;
            vertexU[i] = u;

            final int t = vertexType(u);
            if (t == REMAINDER)
                remainderCount++;
            else if (t == SLICE)
                sliceCount++;
        }

        // if nothing to slice do nothing and stop iteration - should not happen
        if (sliceCount == 0) {
            assert false;
            return Polygon.NO_LINK_OR_TAG;
        }

        // if this is the last slice, bring into 0-1 min/max and send to output
        if (remainderCount == 0) {
            assert sliceCount > 0 : "degenerate u split - no remainder and no quads in slice";

            MutablePolygon editor = stream.editor(targetAddress);
            editor.minU(layerIndex, flipped ? 1 : 0);
            editor.maxU(layerIndex, flipped ? 0 : 1);

            for (int i = 0; i < vCountIn; i++) {
                editor.spriteU(i, layerIndex, vertexU[i]);
            }
            vSplitter.accept(targetAddress);
            return Polygon.NO_LINK_OR_TAG;
        }

        // if we get to here, take slice and return remainder

        /** point on 0-1 on input vertex scale that separates slice and remainder */
        final float vertexSplitU = (uSplitLow + uSpan) / (uMax - uMin);

        final MutablePolygon writer = stream.writer();

        final int sliceAddress = stream.writerAddress();
        stream.setVertexCount(sliceCount + 2);
        writer.copyFrom(reader, false);
        writer.minU(layerIndex, flipped ? 1 : 0);
        writer.maxU(layerIndex, flipped ? 0 : 1);
        stream.append();
        int iSliceVertex = 0;

        final int remainderAddress = stream.writerAddress();
        stream.setVertexCount(remainderCount + 2);
        writer.copyFrom(reader, false);
        stream.append();
        int iRemainderVertex = 0;

        float uThis = vertexU[vCountIn - 1];

        int iThis = vCountIn - 1;
        int thisType = vertexType(uThis);

        for (int iNext = 0; iNext < vCountIn; iNext++) {
            final float uNext = vertexU[iNext];
            final int nextType = vertexType(uNext);

            if (thisType == EDGE) {
                stream.editor(sliceAddress).copyVertexFrom(iSliceVertex, reader, iThis).spriteU(iSliceVertex, layerIndex, uThis);
                iSliceVertex++;
                stream.editor(remainderAddress).copyVertexFrom(iRemainderVertex, reader, iThis);
                iRemainderVertex++;
            } else if (thisType == SLICE) {
                stream.editor(sliceAddress).copyVertexFrom(iSliceVertex, reader, iThis).spriteU(iSliceVertex, layerIndex, uThis);
                iSliceVertex++;
                if (nextType == REMAINDER) {
                    final float dist = (vertexSplitU - uThis) / (uNext - uThis);
                    stream.editor(remainderAddress).copyInterpolatedVertexFrom(iRemainderVertex, reader, iThis, reader, iNext, dist);

                    final Polygon remainder = stream.polyA(remainderAddress);
                    final float uNew = (uMin + remainder.spriteU(iRemainderVertex, layerIndex) * (uMax - uMin) - uSplitLow) / uSpan;
                    stream.editor(sliceAddress).sprite(iSliceVertex, layerIndex, uNew, remainder.spriteV(iRemainderVertex, layerIndex));

                    iRemainderVertex++;
                    iSliceVertex++;
                }
            } else {
                stream.editor(remainderAddress).copyVertexFrom(iRemainderVertex, reader, iThis);
                iRemainderVertex++;
                if (nextType == SLICE) {
                    final float dist = (vertexSplitU - uThis) / (uNext - uThis);
                    stream.editor(remainderAddress).copyInterpolatedVertexFrom(iRemainderVertex, reader, iThis, reader, iNext, dist);

                    final Polygon remainder = stream.polyA(remainderAddress);
                    final float uNew = (uMin + remainder.spriteU(iRemainderVertex, layerIndex) * (uMax - uMin) - uSplitLow) / uSpan;
                    stream.editor(sliceAddress).sprite(iSliceVertex, layerIndex, uNew, remainder.spriteV(iRemainderVertex, layerIndex));

                    iRemainderVertex++;
                    iSliceVertex++;
                }
            }

            uThis = uNext;
            iThis = iNext;
            thisType = nextType;
        }

        // input poly no longer valid so delete
        reader.setDeleted();

        vSplitter.accept(sliceAddress);
        return remainderAddress;
    }

    private static final int EDGE = 0;
    private static final int REMAINDER = 1;
    private static final int SLICE = 2;

    private static final int vertexType(float uvCoord) {
        if (uvCoord >= 1 - PolyHelper.EPSILON) {
            if (uvCoord <= 1 + PolyHelper.EPSILON)
                return EDGE;
            else
                return REMAINDER;
        } else {
            // < 1-QuadHelper.EPSILON
            return SLICE;
        }
    }

    /**
     * Just like {@link #splitU(MutablePolygon, float, float, Consumer)} but for
     * the v dimension.
     */
    private static int splitV(MutableMesh stream, int targetAddress, int layerIndex, float vSplitLow, float vSpan, IntConsumer output) {
        Polygon reader = stream.reader(targetAddress);
        final float vMin = reader.minV(layerIndex);
        final float vMax = reader.maxV(layerIndex);
        final int vCountIn = reader.vertexCount();
        // PERF: make this threadlocal
        final float[] vertexV = new float[vCountIn];
        final boolean flipped = vSpan < 0;

        /** points fully within the slice */
        int sliceCount = 0;
        /** points fully within the remainder */
        int remainderCount = 0;

        for (int i = 0; i < vCountIn; i++) {
            // vertex u where 0 corresponds to low split and 1 to high split
            final float v = (vMin + reader.spriteV(i, layerIndex) * (vMax - vMin) - vSplitLow) / vSpan;
            vertexV[i] = v;

            final int t = vertexType(v);
            if (t == REMAINDER)
                remainderCount++;
            else if (t == SLICE)
                sliceCount++;
        }

        // if nothing to slice return unmodified; no output to consumer
        if (sliceCount == 0) {
            assert false;
            return Polygon.NO_LINK_OR_TAG;
        }

        // if this is the last slice, bring into 0-1 min/max and send to output
        if (remainderCount == 0) {
            assert sliceCount > 0 : "degenerate u split - no remainder and no quads in slice";

            MutablePolygon editor = stream.editor(targetAddress);
            editor.minV(layerIndex, flipped ? 1 : 0);
            editor.maxV(layerIndex, flipped ? 0 : 1);

            for (int i = 0; i < vCountIn; i++) {
                editor.spriteV(i, layerIndex, vertexV[i]);
            }
            output.accept(targetAddress);
            return Polygon.NO_LINK_OR_TAG;
        }

        // if we get to here, take slice and return remainder

        /** point on 0-1 on input vertex scale that separates slice and remainder */
        final float vertexSplitV = (vSplitLow + vSpan) / (vMax - vMin);

        final MutablePolygon writer = stream.writer();

        final int sliceAddress = stream.writerAddress();
        stream.setVertexCount(sliceCount + 2);
        writer.copyFrom(reader, false);
        writer.minV(layerIndex, flipped ? 1 : 0);
        writer.maxV(layerIndex, flipped ? 0 : 1);
        stream.append();
        int iSliceVertex = 0;

        final int remainderAddress = stream.writerAddress();
        stream.setVertexCount(remainderCount + 2);
        writer.copyFrom(reader, false);
        stream.append();
        int iRemainderVertex = 0;

        float vThis = vertexV[vCountIn - 1];

        int iThis = vCountIn - 1;
        int thisType = vertexType(vThis);

        for (int iNext = 0; iNext < vCountIn; iNext++) {
            final float vNext = vertexV[iNext];
            final int nextType = vertexType(vNext);

            if (thisType == EDGE) {
                stream.editor(sliceAddress).copyVertexFrom(iSliceVertex, reader, iThis).spriteV(iSliceVertex, layerIndex, vThis);
                iSliceVertex++;
                stream.editor(remainderAddress).copyVertexFrom(iRemainderVertex, reader, iThis);
                iRemainderVertex++;
            } else if (thisType == SLICE) {
                stream.editor(sliceAddress).copyVertexFrom(iSliceVertex, reader, iThis).spriteV(iSliceVertex, layerIndex, vThis);
                iSliceVertex++;
                if (nextType == REMAINDER) {
                    final float dist = (vertexSplitV - vThis) / (vNext - vThis);
                    stream.editor(remainderAddress).copyInterpolatedVertexFrom(iRemainderVertex, reader, iThis, reader, iNext, dist);

                    final Polygon remainder = stream.polyA(remainderAddress);
                    final float vNew = (vMin + remainder.spriteV(iRemainderVertex, layerIndex) * (vMax - vMin) - vSplitLow) / vSpan;
                    stream.editor(sliceAddress).sprite(iSliceVertex, layerIndex, remainder.spriteU(iRemainderVertex, layerIndex), vNew);

                    iRemainderVertex++;
                    iSliceVertex++;
                }
            } else {
                stream.editor(remainderAddress).copyVertexFrom(iRemainderVertex, reader, iThis);
                iRemainderVertex++;
                if (nextType == SLICE) {
                    final float dist = (vertexSplitV - vThis) / (vNext - vThis);
                    stream.editor(remainderAddress).copyInterpolatedVertexFrom(iRemainderVertex, reader, iThis, reader, iNext, dist);

                    final Polygon remainder = stream.polyA(remainderAddress);
                    final float vNew = (vMin + remainder.spriteV(iRemainderVertex, layerIndex) * (vMax - vMin) - vSplitLow) / vSpan;
                    stream.editor(sliceAddress).sprite(iSliceVertex, layerIndex, remainder.spriteU(iRemainderVertex, layerIndex), vNew);

                    iRemainderVertex++;
                    iSliceVertex++;
                }
            }

            vThis = vNext;
            iThis = iNext;
            thisType = nextType;
        }

        // input no longer valid so delete
        reader.setDeleted();

        output.accept(sliceAddress);
        return remainderAddress;
    }

    @SuppressWarnings("rawtypes")
    public static void paintQuads(MutableMesh stream, PrimitiveModelState modelState, XmSurface surface, XmPaint paint, int textureIndex) {
        /**
         * We add new polys, none of which need to be repainted by this routine. So,
         * when we get to this address we know we are done.
         */
        final int limitAddress = stream.writerAddress();

        MutablePolygon editor = stream.editor();
        do {
            // may move editor so save address and restore at end
            final int editorAddress = stream.editorAddress();

            // if the poly was added after we started we are done
            if (editorAddress >= limitAddress)
                break;

            final Direction face = editor.nominalFace();
            final TextureSet tex = paint.texture(textureIndex);

            /**
             * The smallest UV distance that can be tiled with single texture. Equivalently,
             * the size of each tile in the tiling grid. If no wrap, is simply the texture
             * size. If we wrap, then pick the distance that gives the texture scaling
             * closest to 1:1.
             * 
             */
            final float tilingDistance = tilingDistance(editor.uvWrapDistance(), tex.scale().sliceCount);

            /**
             * See if we will need to split the quad on a UV boundary.
             * <p>
             * 
             * Test on span > tile distance isn't sufficient because uv span might fit
             * within a single texture tile distance but actually start or end mid-texture.
             * <p>
             */

            final boolean uFlipped = editor.maxU(textureIndex) < editor.minU(textureIndex);
            final int uMinIndex = uFlipped ? MathHelper.ceil(editor.minU(textureIndex) / tilingDistance)
                    : MathHelper.floor(editor.minU(textureIndex) / tilingDistance);

            final int uMaxIndex = uFlipped ? MathHelper.floor(editor.maxU(textureIndex) / tilingDistance)
                    : MathHelper.ceil(editor.maxU(textureIndex) / tilingDistance);

            final boolean vFlipped = editor.maxV(textureIndex) < editor.minV(textureIndex);
            final int vMinIndex = vFlipped ? MathHelper.ceil(editor.minV(textureIndex) / tilingDistance)
                    : MathHelper.floor(editor.minV(textureIndex) / tilingDistance);

            final int vMaxIndex = vFlipped ? MathHelper.floor(editor.maxV(textureIndex) / tilingDistance)
                    : MathHelper.ceil(editor.maxV(textureIndex) / tilingDistance);

            final int uStep = uFlipped ? -1 : 1;
            final int vStep = vFlipped ? -1 : 1;

            final float uSpan = uStep * tilingDistance;
            final float vSpan = vStep * tilingDistance;

            final int baseSalt = (editor.textureSalt() << 3) | (face == null ? 6 : face.ordinal());

            int uRemainder = editorAddress;

            // do the splits
            for (int uIndex = uMinIndex; uIndex != uMaxIndex; uIndex += uStep) {
                final int uIndexFinal = uIndex;
                final float uSplitLow = uIndexFinal * tilingDistance;

                uRemainder = splitU(stream, uRemainder, textureIndex, uSplitLow, uSpan, vTargetAddress -> {
                    int vRemainder = vTargetAddress;
                    for (int vIndex = vMinIndex; vIndex != vMaxIndex; vIndex += vStep) {
                        final int vIndexFinal = vIndex;
                        final float vSplitLow = vIndexFinal * tilingDistance;

                        vRemainder = splitV(stream, vRemainder, textureIndex, vSplitLow, vSpan, outputAddress -> {
                            // final painting and output

                            // If we get to this point, quad is within tile boundaries (if needed) and all
                            // needed splits have happened.

                            // The uv min/max on the quad should be 0-1, where 1 represents a single
                            // texture tile distance. The provided salt will be used for texture
                            // randomization.

                            // salt at this point will be original salt plus u,v indexes in higher bits (if
                            // applicable)
                            // add face if have it to make faces different and then hash it.
                            // Note that we could do some randomization via texture offset, but that would
                            // be more complicated and might force us to do additional splits on some models
                            // (would it?)
                            // For now, always use uv 0,0 as tiling origin.

                            final int salt = HashCommon.mix(baseSalt | (uIndexFinal << 16) | (vIndexFinal << 22));
                            int textureVersion = tex.versionMask() & (salt >> 4);
                            editor.sprite(textureIndex, tex.textureName(textureVersion));

                            editor.rotation(textureIndex,
                                    tex.rotation() == TextureRotation.ROTATE_RANDOM ? Useful.offsetEnumValue(tex.rotation().rotation, (salt >> 16) & 3)
                                            : tex.rotation().rotation);

                            editor.lockUV(textureIndex, false);
                            commonPostPaint(editor, modelState, surface, paint, textureIndex);

                            // earlier UV splits may have left us with something other than a convex quad or
                            // tri
                            // doing this last to avoid have to loop through the splits
                            if (stream.splitIfNeeded(editorAddress) != Polygon.NO_LINK_OR_TAG) {
                                assert editor.isDeleted();
                            }
                        });

                        if (vRemainder == Polygon.NO_LINK_OR_TAG)
                            break;
                    }
                });

                if (uRemainder == Polygon.NO_LINK_OR_TAG)
                    break;
            }

            // restore editor position for iteration
            stream.moveEditor(editorAddress);

        } while (stream.editorNext());
    }
}
