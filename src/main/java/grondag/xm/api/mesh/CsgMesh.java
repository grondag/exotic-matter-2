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
package grondag.xm.api.mesh;

import grondag.xm.api.mesh.polygon.Vec3f;

public interface CsgMesh extends MutableMesh {

    float normalX(int nodeAddress);

    float normalY(int nodeAddress);

    float normalZ(int nodeAddress);

    float dist(int nodeAddress);

    /**
     * Signals that all original polys have been added and subsequent operations
     * will only clip or invert the existing tree.
     * <p>
     */
    void complete();

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
    void invert();

    boolean isInverted();

    /**
     * Remove all polygons in this BSP tree that are inside the input BSP tree.<br>
     * This tree is modified, and polygons are split if necessary.
     * <p>
     * 
     * Does not create or remove any nodes in the BSP tree, but nodes can become
     * empty.
     */
    void clipTo(CsgMesh clippingStream);

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
    void outputRecombinedQuads(WritableMesh output);

    /**
     * Creates an empty node.
     */
    int createNode(Vec3f normal, float sampleX, float sampleY, float sampleZ);

}
