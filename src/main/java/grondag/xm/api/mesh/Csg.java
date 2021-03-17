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
/**
 * Portions reproduced or adapted from JCSG.
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.mesh.polygon.Polygon;

/**
 * Access point for CSG operations.<br>
 * <em>Heavily</em> modified from original source.<br>
 * Uses polystreams and threadlocals to minimize garbage collection.
 */
@Experimental
public abstract class Csg {
	/**
	 * Output a new mesh solid representing the difference of the two input meshes.
	 *
	 * <blockquote>
	 *
	 * <pre>
	 * A.difference(B)
	 *
	 * +-------+            +-------+
	 * |       |            |       |
	 * |   A   |            |       |
	 * |    +--+----+   =   |    +--+
	 * +----+--+    |       +----+
	 *      |   B   |
	 *      |       |
	 *      +-------+
	 * </pre>
	 *
	 * </blockquote>
	 */
	public static void difference(XmMesh a, XmMesh b, WritableMesh output) {
		final CsgMesh aCsg = XmMeshes.claimCsg(a);
		final CsgMesh bCsg = XmMeshes.claimCsg(b);

		difference(aCsg, bCsg, output);

		aCsg.release();
		bCsg.release();
	}

	/**
	 * Version of {@link #difference(XmMesh, XmMesh, WritableMesh)}
	 * to use when you've already built CSG streams. Marks the streams complete but
	 * does not release them. Both input streams are modified.
	 */
	public static void difference(CsgMesh a, CsgMesh b, WritableMesh output) {
		a.complete();
		b.complete();

		//PERF: look at restoring - problem with former implementation
		// is that it happens without face recombination
		//        // A outside of B bounds can be passed directly to output
		//        if (outputDisjointA(a, b, output))
		//            // if A is empty there is nothing to subtract from
		//            return;

		// add portions of A within B bounds but not inside B mesh
		a.invert();
		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		a.invert();

		a.outputRecombinedQuads(output);
		b.outputRecombinedQuads(output);
	}

	/**
	 * Output a new mesh representing the intersection of two input meshes.
	 *
	 * <blockquote>
	 *
	 * <pre>
	 *     A.intersect(B)
	 *
	 *     +-------+
	 *     |       |
	 *     |   A   |
	 *     |    +--+----+   =   +--+
	 *     +----+--+    |       +--+
	 *          |   B   |
	 *          |       |
	 *          +-------+
	 * </pre>
	 *
	 * </blockquote>
	 */
	public static void intersect(XmMesh a, XmMesh b, WritableMesh output) {
		final CsgMesh aCsg = XmMeshes.claimCsg(a);
		final CsgMesh bCsg = XmMeshes.claimCsg(b);

		intersect(aCsg, bCsg, output);

		aCsg.release();
		bCsg.release();
	}

	/**
	 * Version of {@link #intersect(XmMesh, XmMesh, WritableMesh)}
	 * to use when you've already built CSG streams. Marks the streams complete but
	 * does not release them. Both input streams are modified.
	 */
	public static void intersect(CsgMesh a, CsgMesh b, WritableMesh output) {
		a.complete();
		b.complete();

		a.complete();
		b.complete();

		a.invert();
		b.clipTo(a);
		b.invert();
		a.clipTo(b);
		b.clipTo(a);

		a.invert();
		b.invert();

		a.outputRecombinedQuads(output);
		b.outputRecombinedQuads(output);
	}

	/**
	 * Output a new mesh representing the union of the input meshes.
	 *
	 * <blockquote>
	 *
	 * <pre>
	 *    A.union(B)
	 *
	 *    +-------+            +-------+
	 *    |       |            |       |
	 *    |   A   |            |       |
	 *    |    +--+----+   =   |       +----+
	 *    +----+--+    |       +----+       |
	 *         |   B   |            |       |
	 *         |       |            |       |
	 *         +-------+            +-------+
	 * </pre>
	 *
	 * </blockquote>
	 *
	 */
	public static void union(XmMesh a, XmMesh b, WritableMesh output) {
		final CsgMesh aCsg = XmMeshes.claimCsg(a);
		final CsgMesh bCsg = XmMeshes.claimCsg(b);

		union(aCsg, bCsg, output);

		aCsg.release();
		bCsg.release();
	}

	/**
	 * Version of {@link #union(XmMesh, XmMesh, WritableMesh)} to
	 * use when you've already built CSG streams. Marks the streams complete but
	 * does not release them. Both input streams are modified.
	 */
	public static void union(CsgMesh a, CsgMesh b, WritableMesh output) {
		a.complete();
		b.complete();

		//PERF: look at restoring - problem with former implementation
		// is that it happens without face recombination

		// A outside of B bounds can be passed directly to output
		//        if (outputDisjointA(a, b, output)) {
		//            // A and B bounds don't overlap, so output all of original b
		//            b.outputRecombinedQuads(output);
		//        } else {
		// some potential overlap
		// add union of the overlapping bits,
		// which will include any parts of B that need to be included

		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		b.invert();

		a.outputRecombinedQuads(output);
		b.outputRecombinedQuads(output);
		//        }
	}

	/**
	 * Polygons in A that do not intersect with B are sent to output and then
	 * deleted. Returns true if A is empty, either because it was empty at the
	 * start, or because all A polygons have been deleted.
	 */
	@SuppressWarnings("unused")
	private static boolean outputDisjointA(CsgMesh a, CsgMesh b, WritableMesh output) {
		final Polygon aReader = a.reader();
		if (aReader.origin()) {
			if (b.reader().origin())
				// nominal case
				return outputDisjointAInner(a, b, output);
			else {
				// B is empty, A is not, therefore output all of A and return false

				do {
					output.appendCopy(aReader);
					aReader.delete();
				} while (aReader.next());
				return false;
			}
		} else
			// A already empty
			return false;
	}

	/**
	 * Handles nominal case when both A and B are non-empty. Assumes A and B are at
	 * origin.
	 */
	private static boolean outputDisjointAInner(CsgMesh a, CsgMesh b, WritableMesh output) {
		boolean aIsEmpty = true;

		// compute B mesh bounds
		float bMinX = Float.MAX_VALUE;
		float bMinY = Float.MAX_VALUE;
		float bMinZ = Float.MAX_VALUE;
		float bMaxX = Float.MIN_VALUE;
		float bMaxY = Float.MIN_VALUE;
		float bMaxZ = Float.MIN_VALUE;

		// scoping
		{
			final Polygon bReader = b.reader();
			do {
				final int vCount = bReader.vertexCount();
				for (int i = 1; i < vCount; i++) {
					final float x = bReader.x(i);
					if (x < bMinX) {
						bMinX = x;
					} else if (x > bMaxX) {
						bMaxX = x;
					}

					final float y = bReader.y(i);
					if (y < bMinY) {
						bMinY = y;
					} else if (y > bMaxY) {
						bMaxY = y;
					}

					final float z = bReader.z(i);
					if (z < bMinZ) {
						bMinZ = z;
					} else if (z > bMaxZ) {
						bMaxZ = z;
					}
				}
			} while (bReader.next());
		}

		final Polygon aReader = a.reader();
		do {
			// Note we don't do a point-by-point test here
			// and instead compute a bounding box for the polygon.
			// This correctly handles case when all poly points
			// are outside the mesh box but plane of poly intersects.
			// Considered using SAT tests developed for collisions boxes
			// but those require tris and re-packing of vertex data. Not worth.

			float pMinX = aReader.x(0);
			float pMinY = aReader.y(0);
			float pMinZ = aReader.z(0);
			float pMaxX = pMinX;
			float pMaxY = pMinY;
			float pMaxZ = pMinZ;

			final int vCount = aReader.vertexCount();
			for (int i = 1; i < vCount; i++) {
				final float x = aReader.x(i);
				if (x < pMinX) {
					pMinX = x;
				} else if (x > pMaxX) {
					pMaxX = x;
				}

				final float y = aReader.y(i);
				if (y < pMinY) {
					pMinY = y;
				} else if (y > pMaxY) {
					pMaxY = y;
				}

				final float z = aReader.z(i);
				if (z < pMinZ) {
					pMinZ = z;
				} else if (z > pMaxZ) {
					pMaxZ = z;
				}
			}

			// For CSG operations we consider a point on the edge to be intersecting.
			if (bMinX <= pMaxX && bMaxX >= pMinX && bMinY <= pMaxY && bMaxY >= pMinY && bMinZ <= pMaxZ && bMaxZ >= pMinZ) {
				// potentially intersecting
				aIsEmpty = false;
			} else {
				// disjoint
				output.appendCopy(aReader);
				aReader.delete();
			}
		} while (aReader.next());

		return aIsEmpty;
	}
}
