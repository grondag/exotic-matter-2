/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.primitive.simple;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.FaceVertex;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.base.AbstractWedge;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;

@Experimental
public class Wedge extends AbstractWedge {
	private static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("back", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

	public static final XmSurface SURFACE_BACK = SURFACES.get(0);
	public static final XmSurface SURFACE_BOTTOM = SURFACES.get(1);
	public static final XmSurface SURFACE_TOP = SURFACES.get(2);
	public static final XmSurface SURFACE_SIDES = SURFACES.get(3);

	public static final Wedge INSTANCE = new Wedge(Xm.id("wedge"));

	protected Wedge(ResourceLocation id) {
		super(id, s -> SURFACES);
	}

	@Override
	protected ReadOnlyMesh buildMesh(PolyTransform transform, boolean isCorner, boolean isInside) {
		// Default geometry bottom/back against down/south faces. Corner is on right.

		final WritableMesh stream = XmMeshes.claimWritable();
		final MutablePolygon quad = stream.writer();

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
		quad.surface(SURFACE_BACK);
		quad.nominalFace(Direction.SOUTH);

		if (isCorner && !isInside) {
			quad.setupFaceQuad(Direction.SOUTH,
					new FaceVertex(0, 0, 0),
					new FaceVertex(1, 0, 0),
					new FaceVertex(0.5f, 0.5f, 0),
					new FaceVertex(0, 1, 0),
					Direction.UP);
			quad.assignLockedUVCoordinates(0);
		} else {
			quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
		}

		transform.accept(quad);
		quad.append();

		if (!isCorner || isInside) {
			quad.surface(SURFACE_SIDES);
			quad.nominalFace(Direction.EAST);
			quad.setupFaceQuad(Direction.EAST,
					new FaceVertex(0, 0, 0),
					new FaceVertex(1, 0, 0),
					new FaceVertex(0.5f, 0.5f, 0),
					new FaceVertex(0, 1, 0),
					Direction.UP);
			quad.assignLockedUVCoordinates(0);
			transform.accept(quad);
			quad.append();
		}

		//        } else {
		//            quad.setupFaceQuad(Direction.EAST,
		//                    new FaceVertex(0, 0, 0),
		//                    new FaceVertex(1, 0, 0),
		//                    new FaceVertex(0.5f, 0.5f, 0.5f),
		//                    new FaceVertex(0, 1, 1.0f),
		//                    Direction.UP);
		//        }

		quad.surface(SURFACE_SIDES);
		quad.nominalFace(Direction.WEST);

		if (!isCorner || !isInside) {
			quad.setupFaceQuad(
				Direction.WEST,
				new FaceVertex(0, 0, 0),
				new FaceVertex(1, 0, 0),
				new FaceVertex(1, 1, 0),
				new FaceVertex(0.5f, 0.5f, 0),
				Direction.UP
			);
		} else {
			quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
		}

		quad.assignLockedUVCoordinates(0);
		transform.accept(quad);
		quad.append();

		// front/top
		if (isCorner) {
			if (isInside) {
				quad.surface(SURFACE_TOP);
				quad.nominalFace(Direction.UP);
				quad.setupFaceQuad(Direction.UP,
						new FaceVertex(0, 0, 1),
						new FaceVertex(0.5f, 0.5f, 0.5f),
						new FaceVertex(1, 1, 0),
						new FaceVertex(0, 1, 0),
						Direction.SOUTH);
				quad.assignLockedUVCoordinates(0);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.nominalFace(Direction.UP);
				quad.setupFaceQuad(Direction.UP,
						new FaceVertex(0, 0, 1),
						new FaceVertex(1, 0, 0),
						new FaceVertex(1, 1, 0),
						new FaceVertex(0.5f, 0.5f, 0.5f),
						Direction.SOUTH);
				quad.assignLockedUVCoordinates(0);
				transform.accept(quad);
				quad.append();

				// inside has an extra side face on the front
				quad.surface(SURFACE_SIDES);
				quad.nominalFace(Direction.NORTH);
				quad.setupFaceQuad(Direction.NORTH,
						new FaceVertex(0, 0, 0),
						new FaceVertex(1, 0, 0),
						new FaceVertex(1, 1, 0),
						new FaceVertex(0.5f, 0.5f, 0),
						Direction.UP);
				quad.assignLockedUVCoordinates(0);
				transform.accept(quad);
				quad.append();
			} else {
				// outside
				quad.surface(SURFACE_TOP);
				quad.nominalFace(Direction.UP);
				quad.setupFaceQuad(Direction.UP,
						new FaceVertex(0, 0, 1),
						new FaceVertex(0.5f, 0.5f, 0.5f),
						new FaceVertex(1, 1, 0),
						new FaceVertex(0, 1, 1),
						Direction.SOUTH);
				quad.assignLockedUVCoordinates(0);
				transform.accept(quad);
				quad.append();

				quad.surface(SURFACE_TOP);
				quad.nominalFace(Direction.UP);
				quad.setupFaceQuad(Direction.UP,
						new FaceVertex(0, 0, 1),
						new FaceVertex(1, 0, 1),
						new FaceVertex(1, 1, 0),
						new FaceVertex(0.5f, 0.5f, 0.5f),
						Direction.SOUTH);
				quad.assignLockedUVCoordinates(0);
				transform.accept(quad);
				quad.append();
			}
		} else {
			quad.surface(SURFACE_TOP);
			quad.nominalFace(Direction.UP);
			quad.setupFaceQuad(Direction.UP,
					new FaceVertex(0, 0, 1),
					new FaceVertex(1, 0, 1),
					new FaceVertex(1, 1, 0),
					new FaceVertex(0, 1, 0),
					Direction.SOUTH);
			quad.assignLockedUVCoordinates(0);
			transform.accept(quad);
			quad.append();
		}

		return stream.releaseToReader();
	}
}
