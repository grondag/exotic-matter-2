/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.api.mesh;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.mesh.polygon.Vec3f;

@Experimental
public interface CsgMesh extends MutableMesh {
	float normalX(int nodeAddress);

	float normalY(int nodeAddress);

	float normalZ(int nodeAddress);

	float dist(int nodeAddress);

	/**
	 * Signals that all original polys have been added and subsequent operations
	 * will only clip or invert the existing tree.
	 */
	void complete();

	/**
	 * Conceptually, converts solid space to empty space and vice versa for all
	 * nodes in this stream / BSP tree.
	 *
	 * <p>Does not actually reverse winding order or normals, because that would be
	 * expensive and not actually needed until we need to produce renderable quads.
	 *
	 * <p>However, this means logic elsewhere must interpret isFlipped to mean the tree
	 * is inverted and interpret the node normals accordingly.
	 */
	void invert();

	boolean isInverted();

	/**
	 * Remove all polygons in this BSP tree that are inside the input BSP tree.<br>
	 * This tree is modified, and polygons are split if necessary.
	 *
	 * <p>Does not create or remove any nodes in the BSP tree, but nodes can become
	 * empty.
	 */
	void clipTo(CsgMesh clippingStream);

	/**
	 * Appends all non-deleted polys to the given output, recombining polys that
	 * have been split as much as possible. Outputs will all be quads or tris. (No
	 * higher-order polys.)
	 *
	 * <p>MAY MAKE MODIFICATIONS TO THIS STREAM THAT BREAK OR DEOPTIMIZE BSP TREE.<br>
	 * Specifically, will join polys that may be split by one or more BSP nodes.<br>
	 * Should only be used as the terminal operation for this stream.
	 *
	 * <p>Will not join polys that were never part of the same original poly. No effect
	 * on bounds and rejoined polys will be in same node as polys that were joined.
	 * Obviously, the joined polys will be marked deleted.
	 */
	void outputRecombinedQuads(WritableMesh output);

	/**
	 * Creates an empty node.
	 */
	int createNode(Vec3f normal, float sampleX, float sampleY, float sampleZ);
}
