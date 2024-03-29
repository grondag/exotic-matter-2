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

package grondag.xm.api.mesh.generator;

import net.minecraft.core.Direction;

import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;

public class StairMesh {
	public static void build(
		WritableMesh mesh,
		PolyTransform transform,
		boolean isCorner,
		boolean isInside,
		XmSurface SURFACE_BOTTOM,
		XmSurface SURFACE_TOP,
		XmSurface SURFACE_FRONT,
		XmSurface SURFACE_BACK,
		XmSurface SURFACE_LEFT,
		XmSurface SURFACE_RIGHT
	) {
		// Default geometry bottom/back against down/south faces. Corner is on right.

		// Sides are split into three quadrants vs one long strip plus one long quadrant
		// is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
		// a T-junction tends to mess about with the results.

		final MutablePolygon quad = mesh.writer();

		quad.rotation(0, TextureOrientation.IDENTITY);
		quad.lockUV(0, true);
		quad.saveDefaults();

		// bottom is always the same
		quad.surface(SURFACE_BOTTOM);
		quad.nominalFace(Direction.DOWN);
		quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
		transform.accept(quad);
		quad.append();

		// back is full except for outside corners
		if (isCorner && !isInside) {
			quad.surface(SURFACE_BACK);
			quad.setupFaceQuad(Direction.SOUTH, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();

			quad.surface(SURFACE_BACK);
			quad.setupFaceQuad(Direction.SOUTH, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();

			quad.surface(SURFACE_BACK);
			quad.setupFaceQuad(Direction.SOUTH, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();
		} else {
			quad.surface(SURFACE_BACK);
			quad.nominalFace(Direction.SOUTH);
			quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
			transform.accept(quad);
			quad.append();
		}

		if (isCorner) {
			if (isInside) {
				quad.surface(SURFACE_LEFT);
				quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
				transform.accept(quad);
				quad.append();

				// Extra, inset top quadrant on inside corner

				// make cuts appear different from top/front face
				quad.textureSalt(1);
				quad.surface(SURFACE_LEFT);
				quad.setupFaceQuad(Direction.EAST, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
				transform.accept(quad);
				quad.append();
			} else {
				// Left side top quadrant is inset on an outside corner
				quad.textureSalt(1);
				quad.surface(SURFACE_LEFT);
				quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.UP);
				transform.accept(quad);
				quad.append();
			}
		} else {
			quad.surface(SURFACE_LEFT);
			quad.setupFaceQuad(Direction.EAST, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();
		}

		quad.surface(SURFACE_LEFT);
		quad.setupFaceQuad(Direction.EAST, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
		transform.accept(quad);
		quad.append();

		quad.surface(SURFACE_LEFT);
		quad.setupFaceQuad(Direction.EAST, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
		transform.accept(quad);
		quad.append();

		// right side is a full face on an inside corner
		if (isCorner && isInside) {
			quad.surface(SURFACE_RIGHT);
			quad.setupFaceQuad(Direction.WEST, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();
		} else {
			quad.surface(SURFACE_RIGHT);
			quad.setupFaceQuad(Direction.WEST, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();

			quad.surface(SURFACE_RIGHT);
			quad.setupFaceQuad(Direction.WEST, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();

			quad.surface(SURFACE_RIGHT);
			quad.setupFaceQuad(Direction.WEST, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();
		}

		// front
		if (isCorner) {
			if (isInside) {
				quad.surface(SURFACE_FRONT);
				quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 0.5f, 0.5f, 0.0f, Direction.UP);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_FRONT);
				quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_FRONT);
				quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.UP);
				transform.accept(quad);
				quad.append();

				quad.textureSalt(1);
				quad.surface(SURFACE_FRONT);
				quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.UP);
				transform.accept(quad);
				quad.append();
			} else {
				quad.surface(SURFACE_FRONT);
				quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
				transform.accept(quad);
				quad.append();

				quad.textureSalt(1);
				quad.surface(SURFACE_FRONT);
				quad.setupFaceQuad(Direction.NORTH, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
				transform.accept(quad);
				quad.append();
			}
		} else {
			quad.surface(SURFACE_FRONT);
			quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.UP);
			transform.accept(quad);
			quad.append();

			quad.textureSalt(1);
			quad.surface(SURFACE_FRONT);
			quad.setupFaceQuad(Direction.NORTH, 0.0f, 0.5f, 1.0f, 1.0f, 0.5f, Direction.UP);
			transform.accept(quad);
			quad.append();
		}

		// top
		if (isCorner) {
			if (isInside) {
				quad.surface(SURFACE_TOP);
				quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 0.5f, 1.0f, 0.0f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.setupFaceQuad(Direction.UP, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.setupFaceQuad(Direction.UP, 0.5f, 0.0f, 1.0f, 0.5f, 0.0f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.textureSalt(1);
				quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();
			} else {
				quad.surface(SURFACE_TOP);
				quad.setupFaceQuad(Direction.UP, 0.5f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.textureSalt(1);
				quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.textureSalt(1);
				quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 0.5f, 1.0f, 0.5f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.textureSalt(1);
				quad.setupFaceQuad(Direction.UP, 0.5f, 0.0f, 1.0f, 0.5f, 0.5f, Direction.SOUTH);
				transform.accept(quad);
				quad.append();
			}
		} else {
			quad.surface(SURFACE_TOP);
			quad.setupFaceQuad(Direction.UP, 0.0f, 0.5f, 1.0f, 1.0f, 0.0f, Direction.SOUTH);
			transform.accept(quad);
			quad.append();

			quad.surface(SURFACE_TOP);
			quad.textureSalt(1);
			quad.setupFaceQuad(Direction.UP, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f, Direction.SOUTH);
			transform.accept(quad);
			quad.append();
		}
	}
}
