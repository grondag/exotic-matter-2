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
package grondag.xm.painter;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.orientation.FaceCorner;

/**
 * Helper class to splits UV-locked quads into four quadrants at the u,v = 0.5,
 * 0.5 point (if needed) and to test if quads are already within a single
 * quadrant.
 */
@API(status = INTERNAL)
public class QuadrantSplitter {

    public final static FaceCorner uvQuadrant(MutablePolygon quad, int layerIndex) {
        final int vCount = quad.vertexCount();

        float uMin = quad.u(0, layerIndex);
        float uMax = uMin;
        float vMin = quad.v(0, layerIndex);
        float vMax = vMin;

        for (int i = 1; i < vCount; i++) {
            final float u = quad.u(i, layerIndex);
            final float v = quad.v(i, layerIndex);
            if (u < uMin) {
                uMin = u;
            } else if (u > uMax) {
                uMax = u;
            }

            if (v < vMin) {
                vMin = v;
            } else if (v > vMax) {
                vMax = v;
            }
        }

        // note that v is inverted from FaceCorner semantics.
        // (high v = bottom, low v = top)
        if (vertexType(uMin) == LOW) {
            // u is left
            if (vertexType(uMax) == HIGH)
                // spanning
                return null;

            else if (vertexType(vMin) == LOW) {
                // v is low

                if (vertexType(vMax) == HIGH)
                    // spanning
                    return null;
                else
                    return FaceCorner.TOP_LEFT;
            } else
                // v is high
                return FaceCorner.BOTTOM_LEFT;
        } else {
            // u is right
            if (vertexType(vMin) == LOW) {
                // v is low

                if (vertexType(vMax) == HIGH)
                    // spanning
                    return null;
                else
                    return FaceCorner.TOP_RIGHT;
            } else
                // v is high
                return FaceCorner.BOTTOM_RIGHT;
        }
    }

    /**
     * Stream editor must be at split position. <br>
     * May move editor but returns editor to position at call time if it does
     * so.<br>
     * If split occurs, poly at editor position will be marked deleted.
     * <p>
     *
     * May also move reader, and does not restore reader position if it does so.
     */
    public static final void split(MutableMesh stream, int layerIndex) {
        final MutablePolygon editor = stream.editor();
        final int editorAddress = editor.address();
        final Polygon reader = stream.reader(editorAddress);

        int lowCount = 0;
        int highCount = 0;
        final int vCount = reader.vertexCount();

        for (int i = 0; i < vCount; i++) {
            final int t = vertexType(reader.u(i, layerIndex));
            if (t == HIGH) {
                highCount++;
            } else if (t == LOW) {
                lowCount++;
            }
        }

        if (lowCount == 0) {
            // all on on high side
            splitV(stream, editorAddress, true, layerIndex);
        } else if (highCount == 0) {
            // all on low side
            splitV(stream, editorAddress, false, layerIndex);
        } else {
            // spanning
            final MutablePolygon writer = stream.writer();

            final int highAddress = stream.writerAddress();
            int iHighVertex = 0;
            writer.vertexCount(highCount + 2)
            .copyFrom(reader, false)
            .append();

            final int lowAddress = stream.writerAddress();
            int iLowVertex = 0;
            writer.vertexCount(lowCount + 2)
            .copyFrom(reader, false)
            .append();

            int iThis = vCount - 1;
            float uThis = reader.u(iThis, layerIndex);
            int thisType = vertexType(uThis);

            for (int iNext = 0; iNext < vCount; iNext++) {
                final float uNext = reader.u(iNext, layerIndex);
                final int nextType = vertexType(uNext);

                if (thisType == EDGE) {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                } else if (thisType == HIGH) {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    if (nextType == LOW) {
                        final float dist = (0.5f - uThis) / (uNext - uThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                } else {
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                    if (nextType == HIGH) {
                        final float dist = (0.5f - uThis) / (uNext - uThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                }
                iThis = iNext;
                uThis = uNext;
                thisType = nextType;
            }

            reader.delete();

            splitV(stream, highAddress, true, layerIndex);
            splitV(stream, lowAddress, false, layerIndex);

            // restore editor position if we moved it
            if (editor.address() != editorAddress) {
                editor.moveTo(editorAddress);
            }
        }
    }

    private static final void splitV(MutableMesh stream, int polyAddress, boolean isHighU, int layerIndex) {
        final Polygon reader = stream.reader(polyAddress);

        int lowCount = 0;
        int highCount = 0;
        final int vCount = reader.vertexCount();

        for (int i = 0; i < vCount; i++) {
            final int t = vertexType(reader.v(i, layerIndex));
            if (t == HIGH) {
                highCount++;
            } else if (t == LOW) {
                lowCount++;
            }
        }

        if (lowCount == 0)
            // all on on high side
            return;
        else if (highCount == 0)
            // all on low side
            return;
        else {
            // spanning
            final MutablePolygon writer = stream.writer();

            final int highAddress = stream.writerAddress();
            int iHighVertex = 0;
            writer.vertexCount(highCount + 2)
            .copyFrom(reader, false)
            .append();

            final int lowAddress = stream.writerAddress();
            int iLowVertex = 0;
            writer.vertexCount(lowCount + 2)
            .copyFrom(reader, false)
            .append();

            int iThis = vCount - 1;
            float vThis = reader.v(iThis, layerIndex);
            int thisType = vertexType(vThis);

            for (int iNext = 0; iNext < vCount; iNext++) {
                final float vNext = reader.v(iNext, layerIndex);
                final int nextType = vertexType(vNext);

                if (thisType == EDGE) {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                } else if (thisType == HIGH) {
                    stream.editor(highAddress).copyVertexFrom(iHighVertex++, reader, iThis);
                    if (nextType == LOW) {
                        final float dist = (0.5f - vThis) / (vNext - vThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                } else {
                    stream.editor(lowAddress).copyVertexFrom(iLowVertex++, reader, iThis);
                    if (nextType == HIGH) {
                        final float dist = (0.5f - vThis) / (vNext - vThis);
                        stream.editor(lowAddress).copyInterpolatedVertexFrom(iLowVertex, reader, iThis, reader, iNext, dist);
                        stream.editor(highAddress).copyVertexFrom(iHighVertex, stream.polyA(lowAddress), iLowVertex);
                        iLowVertex++;
                        iHighVertex++;
                    }
                }
                iThis = iNext;
                vThis = vNext;
                thisType = nextType;
            }

            reader.delete();
        }
    }

    private static final int EDGE = 0;
    private static final int HIGH = 1;
    private static final int LOW = 2;

    private final static int vertexType(float uvCoord) {
        if (uvCoord >= 0.5f - PolyHelper.EPSILON) {
            if (uvCoord <= 0.5f + PolyHelper.EPSILON)
                return EDGE;
            else
                return HIGH;
        } else
            // < 0.5f - QuadHelper.EPSILON
            return LOW;
    }
}
