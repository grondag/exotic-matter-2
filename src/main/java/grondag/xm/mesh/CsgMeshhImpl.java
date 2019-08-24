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
package grondag.xm.mesh;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.concurrent.atomic.AtomicInteger;

import org.apiguardian.api.API;

import grondag.fermion.intstream.IntStream;
import grondag.fermion.intstream.IntStreams;
import grondag.xm.api.mesh.CsgMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.mesh.polygon.Vec3f;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Implements a BSP tree for CSG operations as part of mutable mesh/stream.
 * <p>
 * 
 * The BSP tree is build and maintained as polys are added. While polys are
 * split to conform to the tree, we do not associates polys with specific BSP
 * nodes because that association is never needed.
 * <p>
 * 
 * Also tracks AABB for the mesh overall and for individual polys as a way to
 * optimize bounds testing for CSG operations.
 * <p>
 */
@API(status = INTERNAL)
class CsgMeshhImpl extends MutableMeshImpl implements CsgMesh {
    /**
     * Ensures poly tags for recombination are globally unique. <br>
     * Could probably use stream address, but to be certain would have to
     * check/maintain all cases where stream polys are copied from one stream to
     * another and this makes that unnecessary.
     */
    private static final AtomicInteger NEXT_TAG = new AtomicInteger(1);

    private static final int COPLANAR = 0;
    private static final int FRONT = 1;
    private static final int BACK = 2;

    private static final int BACK_SHIFT = 8;
    private static final int FRONT_INCREMENT = 1;
    private static final int FRONT_MASK = (1 << BACK_SHIFT) - 1;
    private static final int BACK_INCREMENT = 1 << BACK_SHIFT;
    private static final int BACK_MASK = FRONT_MASK << BACK_SHIFT;

    private static final int vertexIncrement(float x, float y, float z, float normalX, float normalY, float normalZ, float dist) {
        final float t = x * normalX + y * normalY + z * normalZ - dist;

        if (t <= PolyHelper.EPSILON) {
            if (t >= -PolyHelper.EPSILON)
                return COPLANAR;
            else
                return BACK_INCREMENT;
        } else
            return FRONT_INCREMENT;
    }

    private static final int vertexType(Polygon poly, int vertexIndex, float normalX, float normalY, float normalZ, float dist) {
        return vertexType(poly.x(vertexIndex), poly.y(vertexIndex), poly.z(vertexIndex), normalX, normalY, normalZ, dist);
    }

    private static final int vertexType(float x, float y, float z, float normalX, float normalY, float normalZ, float dist) {
        final float t = x * normalX + y * normalY + z * normalZ - dist;

        if (t >= -PolyHelper.EPSILON) {
            if (t <= PolyHelper.EPSILON)
                return COPLANAR;
            else
                return FRONT;
        } else {
            // t < -QuadHelper.EPSILON
            return BACK;
        }
    }

    private static final int NODE_FRONT_NODE_ADDRESS = 0;
    private static final int NODE_BACK_NODE_ADDRESS = 1;
    private static final int NODE_PLANE_DIST = 2;
    private static final int NODE_PLANE_NORMAL_X = 3;
    private static final int NODE_PLANE_NORMAL_Y = 4;
    private static final int NODE_PLANE_NORMAL_Z = 5;
    private static final int NODE_STRIDE = 6;

    private static final int NO_NODE_ADDRESS = -1;

    private static final ThreadLocal<IntArrayList> STACK = new ThreadLocal<IntArrayList>() {
        @Override
        protected IntArrayList initialValue() {
            return new IntArrayList();
        }
    };

    private IntStream nodeStream;

    private int nextNodeAddress = 0;

    private boolean isComplete = false;

    private boolean isInverted = false;

    protected void prepare() {
        super.prepare(0);
        nodeStream = IntStreams.claim();
        clear();
    }

    @Override
    public void clear() {
        super.clear();
        nextNodeAddress = 0;
        isComplete = false;
        isInverted = false;
    }

    @Override
    protected void doRelease() {
        super.doRelease();
        nodeStream.release();
        nodeStream = null;
    }

    @Override
    protected void returnToPool() {
        XmMeshesImpl.release(this);
    }

    @Override
    public final float normalX(int nodeAddress) {
        return isInverted ? -nodeStream.getFloat(nodeAddress + NODE_PLANE_NORMAL_X) : nodeStream.getFloat(nodeAddress + NODE_PLANE_NORMAL_X);
    }

    @Override
    public final float normalY(int nodeAddress) {
        return isInverted ? -nodeStream.getFloat(nodeAddress + NODE_PLANE_NORMAL_Y) : nodeStream.getFloat(nodeAddress + NODE_PLANE_NORMAL_Y);
    }

    @Override
    public final float normalZ(int nodeAddress) {
        return isInverted ? -nodeStream.getFloat(nodeAddress + NODE_PLANE_NORMAL_Z) : nodeStream.getFloat(nodeAddress + NODE_PLANE_NORMAL_Z);
    }

    @Override
    public final float dist(int nodeAddress) {
        return isInverted ? -nodeStream.getFloat(nodeAddress + NODE_PLANE_DIST) : nodeStream.getFloat(nodeAddress + NODE_PLANE_DIST);
    }

    /**
     * {@inheritDoc}
     * 
     * CSG streams that aren't locked will assigned a unique ID in the tag to
     * facilitate recombination of split polys after CSG operations complete.
     */
    @Override
    protected void appendCopy(Polygon polyIn, int withFormat) {
        // anything after complete should be calling raw version
        assert !isComplete;

        appendRawCopy(polyIn, withFormat);

        // create BSP root node if it doesn't exist
        if (this.nextNodeAddress == 0)
            this.createNode(internal.baseAddress);
        else
            buildBSP(internal.baseAddress, 0);
    }

    /**
     * For internal use by CSG streams to append writer that doesn't yet have
     * vertices and thus shouldn't have bounds updated and should not be added to
     * the BSP tree yet.
     * <p>
     * 
     * Does copy tags.
     * <p>
     * 
     * Guarantees internal poly is set at new poly on exit.
     */
    protected void appendRawCopy(Polygon polyIn, int withFormat) {
        int newAddress = writerAddress();
        super.appendCopy(polyIn, withFormat);
        internal.moveTo(newAddress);

        int tag = polyIn.tag();
        if (tag <= 0)
            tag = NEXT_TAG.getAndIncrement();
        internal.tag(tag);
    }

    /**
     * Like {@link #append()} but doesn't update bounds, etc. Uses
     * {@link #appendRawCopy(Polygon, int)} internally. See that method for more
     * info.
     */
    void appendRaw() {
        appendRawCopy(writer, formatFlags);
        //loadDefaults(); <- was causing problems with surface being lost?
    }

    /**
     * Appends a copy with given vertex count and does not copy vertices.<br>
     * Does not update bounds. Does not add to BSP tree.<br>
     * Returns address of new poly.
     */
    protected int appendEmptySplit(Polygon template, int vertexCount) {
        int newAddress = writerAddress();
        setVertexCount(vertexCount);
        writer.copyFromCSG(template);
        writer.tag(template.tag());

        appendRawCopy(writer, formatFlags);
        //loadDefaults(); <- was causing problems with surface being lost?

        return newAddress;
    }

    /**
     * Signals that all original polys have been added and subsequent operations
     * will only clip or invert the existing tree.
     * <p>
     */
    @Override
    public void complete() {
        isComplete = true;
    }

    private void buildBSP(int polyAddress, int nodeAddress) {
        IntArrayList stack = STACK.get();
        assert stack.isEmpty();
        buildBSPInner(stack, polyAddress, nodeAddress);

        while (!stack.isEmpty()) {
            nodeAddress = stack.popInt();
            polyAddress = stack.popInt();
            buildBSPInner(stack, polyAddress, nodeAddress);
        }
    }

    private void buildBSPInner(IntArrayList stack, final int polyAddress, int nodeAddress) {
        polyB.moveTo(polyAddress);

        do {
            final float normalX = normalX(nodeAddress);
            final float normalY = normalY(nodeAddress);
            final float normalZ = normalZ(nodeAddress);
            final float dist = dist(nodeAddress);
            final int vCount = polyB.vertexCount();
            int combinedCount = 0;

            for (int i = 0; i < vCount; i++) {
                combinedCount += vertexIncrement(polyB.x(i), polyB.y(i), polyB.z(i), normalX, normalY, normalZ, dist);
            }

            // Put the polygon in the correct list, splitting it when necessary.
            if ((combinedCount & FRONT_MASK) == 0) {
                if (combinedCount == 0) {
                    // coplanar
                    float faceNormX = polyB.faceNormalX();
                    float faceNormY = polyB.faceNormalY();
                    float faceNormZ = polyB.faceNormalZ();
                    if (this.isInverted()) {
                        faceNormX = -faceNormX;
                        faceNormY = -faceNormY;
                        faceNormZ = -faceNormZ;
                    }
                    final float t = faceNormX * normalX + faceNormY * normalY + faceNormZ * normalZ;

                    if (t > 0) {
                        // coplanar front counts as front 
                        final int frontNodeAddress = getFrontNode(nodeAddress);
                        if (frontNodeAddress == NO_NODE_ADDRESS) {
                            // no front node, so this poly starts it
                            setFrontNode(nodeAddress, createNode(polyAddress));
                            return;
                        } else {
                            // loop at front node
                            nodeAddress = frontNodeAddress;
                        }
                    } else {
                        final int backNodeAddress = getBackNode(nodeAddress);
                        if (backNodeAddress == NO_NODE_ADDRESS) {
                            // no back node, so this poly starts it
                            setBackNode(nodeAddress, createNode(polyAddress));
                            return;
                        } else {
                            // found a back node, so move to that node and loop
                            nodeAddress = backNodeAddress;
                        }
                    }
                } else {
                    // must be a back node
                    final int backNodeAddress = getBackNode(nodeAddress);
                    if (backNodeAddress == NO_NODE_ADDRESS) {
                        // no back node, so this poly starts it
                        setBackNode(nodeAddress, createNode(polyAddress));
                        return;
                    } else {
                        // found a back node, so move to that node and loop
                        nodeAddress = backNodeAddress;
                    }
                }
            } else {
                // frontcount > 0
                if ((combinedCount & BACK_MASK) == 0) {
                    final int frontNodeAddress = getFrontNode(nodeAddress);
                    if (frontNodeAddress == NO_NODE_ADDRESS) {
                        // no front node, so this poly starts it
                        setFrontNode(nodeAddress, createNode(polyAddress));
                        return;
                    } else {
                        // part of front node - move there and loop
                        nodeAddress = frontNodeAddress;
                    }
                } else {
                    // split and exit
                    final int frontAddress = appendEmptySplit(polyB, (combinedCount & FRONT_MASK) + 2);
                    int iFront = 0;

                    final int backAddress = appendEmptySplit(polyB, ((combinedCount & BACK_MASK) >> BACK_SHIFT) + 2);
                    int iBack = 0;

                    int i = vCount - 1;
                    int iType = vertexType(polyB, i, normalX, normalY, normalZ, dist);

                    for (int j = 0; j < vCount; j++) {
                        final int jType = vertexType(polyB, j, normalX, normalY, normalZ, dist);

                        switch (iType * 3 + jType) {
                            case 0: // I COPLANAR - J COPLANAR
                            case 1: // I COPLANAR - J FRONT
                            case 2: // I COPLANAR - J BACK
                                editor(frontAddress).copyVertexFrom(iFront++, polyB, i);
                                editor(backAddress).copyVertexFrom(iBack++, polyB, i);
                                break;
    
                            case 3: // I FRONT - J COPLANAR
                            case 4: // I FRONT - J FRONT
                                editor(frontAddress).copyVertexFrom(iFront++, polyB, i);
                                break;
    
                            case 6: // I BACK- J COPLANAR
                            case 8: // I BACK - J BACK
                                editor(backAddress).copyVertexFrom(iBack++, polyB, i);
                                break;
    
                            case 5: {
                                // I FRONT - J BACK
                                editor(frontAddress).copyVertexFrom(iFront++, polyB, i);
    
                                // Line for interpolated vertex depends on what the next vertex is for this side
                                // (front/back).
                                // If the next vertex will be included in this side, we are starting the line
                                // connecting
                                // next vertex with previous vertex and should use line from prev. vertex
                                // If the next vertex will NOT be included in this side, we are starting the
                                // split line.
    
                                final float ix = polyB.x(i);
                                final float iy = polyB.y(i);
                                final float iz = polyB.z(i);
    
                                final float tx = polyB.x(j) - ix;
                                final float ty = polyB.y(j) - iy;
                                final float tz = polyB.z(j) - iz;
    
                                final float iDot = ix * normalX + iy * normalY + iz * normalZ;
                                final float tDot = tx * normalX + ty * normalY + tz * normalZ;
                                float t = (dist - iDot) / tDot;
    
                                editor(frontAddress).copyInterpolatedVertexFrom(iFront, polyB, i, polyB, j, t);
                                editor(backAddress).copyVertexFrom(iBack++, polyA(frontAddress), iFront++);
    
                                break;
                            }
    
                            case 7: {
                                // I BACK - J FRONT
                                editor(backAddress).copyVertexFrom(iBack++, polyB, i);
    
                                // see notes for 5
                                final float ix = polyB.x(i);
                                final float iy = polyB.y(i);
                                final float iz = polyB.z(i);
    
                                final float tx = polyB.x(j) - ix;
                                final float ty = polyB.y(j) - iy;
                                final float tz = polyB.z(j) - iz;
    
                                final float iDot = ix * normalX + iy * normalY + iz * normalZ;
                                final float tDot = tx * normalX + ty * normalY + tz * normalZ;
                                float t = (dist - iDot) / tDot;
    
                                editor(frontAddress).copyInterpolatedVertexFrom(iFront, polyB, i, polyB, j, t);
                                editor(backAddress).copyVertexFrom(iBack++, polyA(frontAddress), iFront++);
                                break;
                            }
                        }

                        i = j;
                        iType = jType;
                    }

                    // put front node in BSP tree
                    final int frontNodeAddress = getFrontNode(nodeAddress);
                    if (frontNodeAddress == NO_NODE_ADDRESS) {
                        // no back node, so this poly starts it
                        setFrontNode(nodeAddress, createNode(frontAddress));
                    } else {
                        // put new poly on stack with back node and come back to it
                        stack.push(frontAddress);
                        stack.push(frontNodeAddress);
                    }

                    // put back node in BSP tree
                    final int backNodeAddress = getBackNode(nodeAddress);
                    if (backNodeAddress == NO_NODE_ADDRESS) {
                        // no back node, so this poly starts it
                        setBackNode(nodeAddress, createNode(backAddress));
                    } else {
                        // put new poly on stack with back node and come back to it
                        stack.push(backAddress);
                        stack.push(backNodeAddress);
                    }

                    // mark the split poly deleted
                    polyB.delete();

                    return;
                }
            }
        } while (true);
    }

    /**
     * Conceptually, converts solid space to empty space and vice versa for all
     * nodes in this stream / BSP tree.
     * <p>
     * 
     * Does not actually reverse winding order or normals, because that would be
     * expensive and not actually needed until we need to produce renderable quads.
     * <p>
     * 
     * However, this means logic elsewhere must interpret isFlipped to mean the tree
     * is inverted and interpret the node normals accordingly.
     */
    @Override
    public void invert() {
        assert isComplete;

        isInverted = !isInverted;
    }

    @Override
    public boolean isInverted() {
        return isInverted;
    }

    /**
     * Remove all polygons in this BSP tree that are inside the input BSP tree.<br>
     * This tree is modified, and polygons are split if necessary.
     * <p>
     * 
     * Does not create or remove any nodes in the BSP tree, but nodes can become
     * empty.
     */
    @Override
    public void clipTo(CsgMesh clippingStream) {
        assert isComplete;
        clip(this, (CsgMeshhImpl)clippingStream);
    }

    private static void clip(CsgMeshhImpl targetStream, CsgMeshhImpl clippingStream) {
        final StreamBackedPolygon reader = targetStream.reader;
        final int saveReadAddress = reader.baseAddress;

        /**
         * Clip can create new polys, and we don't need to clip those.
         */
        final int limitAddress = targetStream.writerAddress();

        if (reader.origin()) {
            do {
                clipPoly(targetStream, clippingStream, reader.baseAddress);
            } while (reader.next() && reader.baseAddress < limitAddress);
        }

        reader.moveTo(saveReadAddress);
    }

    /**
     * Clips the poly at the target address to the input stream/mesh.<br>
     * If the poly is "behind" (inside) the mesh it will be removed.<br>
     * If spanning will be split and the "behind" portion removed.
     * <p>
     * 
     * Static to avoid ambiguity/confusion on which stream is being used for what.
     */
    private static void clipPoly(CsgMeshhImpl targetStream, CsgMeshhImpl clippingStream, int polyAddress) {
        IntArrayList stack = STACK.get();
        assert stack.isEmpty();
        int nodeAddress = 0;

        clipPolyInner(stack, targetStream, clippingStream, polyAddress, 0);

        while (!stack.isEmpty()) {
            nodeAddress = stack.popInt();
            polyAddress = stack.popInt();
            clipPolyInner(stack, targetStream, clippingStream, polyAddress, nodeAddress);
        }

    }

    private static void clipPolyInner(IntArrayList stack, CsgMeshhImpl targetStream, final CsgMeshhImpl clippingStream, final int polyAddress,
            int nodeAddress) {
        final StreamBackedPolygon polyB = targetStream.polyB;
        polyB.moveTo(polyAddress);

        do {
            final float normalX = clippingStream.normalX(nodeAddress);
            final float normalY = clippingStream.normalY(nodeAddress);
            final float normalZ = clippingStream.normalZ(nodeAddress);
            final float dist = clippingStream.dist(nodeAddress);
            final int vCount = polyB.vertexCount();
            int combinedCount = 0;

            for (int i = 0; i < vCount; i++) {
                combinedCount += vertexIncrement(polyB.x(i), polyB.y(i), polyB.z(i), normalX, normalY, normalZ, dist);
            }

            // Remove back-facing polys or split when necessary.
            if ((combinedCount & FRONT_MASK) == 0) {
                if (combinedCount == 0) // coplanar
                {
                    float faceNormX = polyB.faceNormalX();
                    float faceNormY = polyB.faceNormalY();
                    float faceNormZ = polyB.faceNormalZ();
                    if (targetStream.isInverted()) {
                        faceNormX = -faceNormX;
                        faceNormY = -faceNormY;
                        faceNormZ = -faceNormZ;
                    }
                    final float t = faceNormX * normalX + faceNormY * normalY + faceNormZ * normalZ;

                    if (t > 0) {
                        // coplanar front counts as front 
                        final int frontNodeAddress = clippingStream.getFrontNode(nodeAddress);
                        if (frontNodeAddress == NO_NODE_ADDRESS) {
                            // this is a leaf node, so we are in front of all tree nodes so we are done - no clip
                            return;
                        } else {
                            // loop at front node
                            nodeAddress = frontNodeAddress;
                        }
                    } else {
                        // coplanar back counts as back
                        final int backNodeAddress = clippingStream.getBackNode(nodeAddress);
                        if (backNodeAddress == NO_NODE_ADDRESS) {
                            // this is a leaf node, so we are in back of all nodes - poly is clipped
                            polyB.delete();
                            return;
                        } else {
                            // loop at back node
                            nodeAddress = backNodeAddress;
                        }
                    }
                } else {
                    // not front, not coplanar, therefore must be back
                    final int backNodeAddress = clippingStream.getBackNode(nodeAddress);
                    if (backNodeAddress == NO_NODE_ADDRESS) {
                        // this is a leaf node, so we are in back of all nodes - poly is clipped
                        polyB.delete();
                        return;
                    } else {
                        // loop at back node
                        nodeAddress = backNodeAddress;
                    }
                }
            } else {
                // frontcount > 0
                if ((combinedCount & BACK_MASK) == 0) {
                    // front
                    final int frontNodeAddress = clippingStream.getFrontNode(nodeAddress);
                    if (frontNodeAddress == NO_NODE_ADDRESS) {
                        // this is a leaf node, so we are in front of all tree nodes so we are done - no
                        // clip
                        return;
                    } else {
                        // loop at front node
                        nodeAddress = frontNodeAddress;
                    }
                } else { 
                    // spanning
                    // split, push to stack if needed, and exit
                    final int frontAddress = targetStream.appendEmptySplit(polyB, (combinedCount & FRONT_MASK) + 2);
                    int iFront = 0;

                    // Don't create back side poly if we are going to discard it.
                    // Will discard if at a back-side leaf node.
                    final int backNodeAddress = clippingStream.getBackNode(nodeAddress);
                    final int backAddress = backNodeAddress == NO_NODE_ADDRESS ? Polygon.NO_LINK_OR_TAG
                            : targetStream.appendEmptySplit(polyB, ((combinedCount & BACK_MASK) >> BACK_SHIFT) + 2);
                    int iBack = 0;

                    int i = vCount - 1;
                    int iType = vertexType(polyB, i, normalX, normalY, normalZ, dist);

                    for (int j = 0; j < vCount; j++) {
                        final int jType = vertexType(polyB, j, normalX, normalY, normalZ, dist);

                        switch (iType * 3 + jType) {
                        case 0: // I COPLANAR - J COPLANAR
                        case 1: // I COPLANAR - J FRONT
                        case 2: // I COPLANAR - J BACK
                            targetStream.editor(frontAddress).copyVertexFrom(iFront++, polyB, i);
                            if (backAddress != Polygon.NO_LINK_OR_TAG) {
                                targetStream.editor(backAddress).copyVertexFrom(iBack++, polyB, i);
                            }
                            break;

                        case 3: // I FRONT - J COPLANAR
                        case 4: // I FRONT - J FRONT
                            targetStream.editor(frontAddress).copyVertexFrom(iFront++, polyB, i);
                            break;

                        case 6: // I BACK- J COPLANAR
                        case 8: // I BACK - J BACK
                            if (backAddress != Polygon.NO_LINK_OR_TAG) {
                                targetStream.editor(backAddress).copyVertexFrom(iBack++, polyB, i);
                            }
                            break;

                        case 5: {
                            // I FRONT - J BACK
                            targetStream.editor(frontAddress).copyVertexFrom(iFront++, polyB, i);

                            // Line for interpolated vertex depends on what the next vertex is for this side
                            // (front/back).
                            // If the next vertex will be included in this side, we are starting the line
                            // connecting
                            // next vertex with previous vertex and should use line from prev. vertex
                            // If the next vertex will NOT be included in this side, we are starting the
                            // split line.

                            final float ix = polyB.x(i);
                            final float iy = polyB.y(i);
                            final float iz = polyB.z(i);

                            final float tx = polyB.x(j) - ix;
                            final float ty = polyB.y(j) - iy;
                            final float tz = polyB.z(j) - iz;

                            final float iDot = ix * normalX + iy * normalY + iz * normalZ;
                            final float tDot = tx * normalX + ty * normalY + tz * normalZ;
                            float t = (dist - iDot) / tDot;

                            targetStream.editor(frontAddress).copyInterpolatedVertexFrom(iFront, polyB, i, polyB, j, t);
                            if (backAddress != Polygon.NO_LINK_OR_TAG) {
                                targetStream.editor(backAddress).copyVertexFrom(iBack++, targetStream.polyA(frontAddress), iFront);
                            }
                            iFront++;

                            break;
                        }

                        case 7: {
                            // I BACK - J FRONT
                            if (backAddress != Polygon.NO_LINK_OR_TAG) {
                                targetStream.editor(backAddress).copyVertexFrom(iBack++, polyB, i);
                            }
                            // see notes for 5
                            final float ix = polyB.x(i);
                            final float iy = polyB.y(i);
                            final float iz = polyB.z(i);

                            final float tx = polyB.x(j) - ix;
                            final float ty = polyB.y(j) - iy;
                            final float tz = polyB.z(j) - iz;

                            final float iDot = ix * normalX + iy * normalY + iz * normalZ;
                            final float tDot = tx * normalX + ty * normalY + tz * normalZ;
                            float t = (dist - iDot) / tDot;

                            targetStream.editor(frontAddress).copyInterpolatedVertexFrom(iFront, polyB, i, polyB, j, t);
                            if (backAddress != Polygon.NO_LINK_OR_TAG) {
                                targetStream.editor(backAddress).copyVertexFrom(iBack++, targetStream.polyA(frontAddress), iFront);
                            }
                            iFront++;
                            break;
                        }
                        }

                        i = j;
                        iType = jType;
                    }

                    // if not a front-side leaf, put new poly on stack with front node and come back
                    // to it
                    final int frontNodeAddress = clippingStream.getFrontNode(nodeAddress);
                    if (frontNodeAddress != NO_NODE_ADDRESS) {
                        stack.push(frontAddress);
                        stack.push(frontNodeAddress);
                    }

                    if (backAddress != Polygon.NO_LINK_OR_TAG) {
                        // no need to check / find back node address
                        // if we get here, means there is a back node
                        // put new poly on stack with back node and come back to it
                        stack.push(backAddress);
                        stack.push(backNodeAddress);
                    }

                    // delete original poly that was split
                    polyB.delete();
                    return;
                }
            }
        } while (true);
    }

    /**
     * Appends all non-deleted polys to the given output, recombining polys that
     * have been split as much as possible. Outputs will all be quads or tris. (No
     * higher-order polys.)
     * <p>
     * 
     * MAY MAKE MODIFICATIONS TO THIS STREAM THAT BREAK OR DEOPTIMIZE BSP TREE.<br>
     * Specifically, will join polys that may be split by one or more BSP nodes.<br>
     * Should only be used as the terminal operation for this stream.
     * <p>
     * 
     * Will not join polys that were never part of the same original poly. No effect
     * on bounds and rejoined polys will be in same node as polys that were joined.
     * Obviously, the joined polys will be marked deleted.
     */
    @Override
    public void outputRecombinedQuads(WritableMesh output) {
        CsgPolyRecombinator.outputRecombinedQuads(this, output);
    }

    private int getBackNode(int nodeAddress) {
        return isInverted ? nodeStream.get(nodeAddress + NODE_FRONT_NODE_ADDRESS) : nodeStream.get(nodeAddress + NODE_BACK_NODE_ADDRESS);
    }

    private void setBackNode(int targetNodeAddress, int backNodeAddress) {
        final int offset = isInverted ? NODE_FRONT_NODE_ADDRESS : NODE_BACK_NODE_ADDRESS;
        nodeStream.set(targetNodeAddress + offset, backNodeAddress);
    }

    private int getFrontNode(int nodeAddress) {
        return isInverted ? nodeStream.get(nodeAddress + NODE_BACK_NODE_ADDRESS) : nodeStream.get(nodeAddress + NODE_FRONT_NODE_ADDRESS);
    }

    private void setFrontNode(int targetNodeAddress, int frontNodeAddress) {
        final int offset = isInverted ? NODE_BACK_NODE_ADDRESS : NODE_FRONT_NODE_ADDRESS;
        nodeStream.set(targetNodeAddress + offset, frontNodeAddress);
    }

    private int createNode(int polyAddress) {
        Polygon p = reader(polyAddress);
        final int newNodeAddress = createNode(p.faceNormal(), p.x(0), p.y(0), p.z(0));
        return newNodeAddress;
    }

    /**
     * Creates an empty node.
     */
    @Override
    public int createNode(Vec3f normal, float sampleX, float sampleY, float sampleZ) {
        final int newNodeAddress = nextNodeAddress;
        nextNodeAddress += NODE_STRIDE;

        nodeStream.set(newNodeAddress + NODE_FRONT_NODE_ADDRESS, NO_NODE_ADDRESS);
        nodeStream.set(newNodeAddress + NODE_BACK_NODE_ADDRESS, NO_NODE_ADDRESS);
        nodeStream.setFloat(newNodeAddress + NODE_PLANE_DIST, normal.dotProduct(sampleX, sampleY, sampleZ));
        nodeStream.setFloat(newNodeAddress + NODE_PLANE_NORMAL_X, normal.x());
        nodeStream.setFloat(newNodeAddress + NODE_PLANE_NORMAL_Y, normal.y());
        nodeStream.setFloat(newNodeAddress + NODE_PLANE_NORMAL_Z, normal.z());
        return newNodeAddress;
    }
}
