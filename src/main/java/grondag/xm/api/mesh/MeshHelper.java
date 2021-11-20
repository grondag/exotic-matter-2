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

package grondag.xm.api.mesh;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.primitive.surface.XmSurface;

@Experimental
public class MeshHelper {
	public static void unitCylinder(MutablePolygon writer, int sliceCount, Consumer<MutablePolygon> transform, XmSurface sideSurface, @Nullable XmSurface topSurface, @Nullable XmSurface bottomSurface, float wrapDistance) {
		unitCylinder(writer, sliceCount, transform, sideSurface, topSurface, false, bottomSurface, false, wrapDistance);
	}

	public static void unitCylinder(
		MutablePolygon writer,
		int sliceCount,
		Consumer<MutablePolygon> transform,
		XmSurface sideSurface,
		@Nullable XmSurface topSurface,
		boolean subdivideTop,
		@Nullable XmSurface bottomSurface,
		boolean subdiviteBottom,
		float wrapDistance
	) {
		unitCylinder(writer, sliceCount, transform, sideSurface, topSurface, false, bottomSurface, false, wrapDistance, 0, 1);
	}

	/**
	 *
	 * @param mesh
	 * @param sliceCount Should be a multiple of 4, and at least 8;
	 * @param transform
	 * @param sideSurface
	 * @param topSurface If null, top cap not output.
	 * @param bottomSurface If null, bottom cap not output.
	 */
	public static void unitCylinder(
		MutablePolygon writer,
		int sliceCount,
		Consumer<MutablePolygon> transform,
		XmSurface sideSurface,
		@Nullable XmSurface topSurface,
		boolean subdivideTop,
		@Nullable XmSurface bottomSurface,
		boolean subdiviteBottom,
		float wrapDistance,
		float bottom,
		float top
	) {
		sliceCount = Math.max(8, ((sliceCount + 3) / 4) * 4);

		final double sliceRadians = Math.PI * 2 / sliceCount;

		for (int i = 0; i < sliceCount; i++) {
			cylSide(i, sliceRadians, writer, transform, sideSurface, wrapDistance, bottom, top);

			if ((i & 1) == 0) {
				if (topSurface != null) {
					cylEnd(i, sliceRadians, writer, transform, topSurface, Direction.UP, subdivideTop, top);
				}

				if (bottomSurface != null) {
					cylEnd(i, sliceRadians, writer, transform, bottomSurface, Direction.DOWN, subdiviteBottom, bottom);
				}
			}
		}
	}

	private static void cylSide(
		int slice,
		double sliceRadians,
		MutablePolygon writer,
		Consumer<MutablePolygon> transform,
		XmSurface sideSurface,
		float wrapDistance,
		float bottom,
		float top
	) {
		final double fromRad = sliceRadians * slice;
		final double toRad = fromRad + sliceRadians;
		final float nx0 = (float) Math.sin(fromRad);
		final float nz0 = (float) Math.cos(fromRad);
		final float nx1 = (float) Math.sin(toRad);
		final float nz1 = (float) Math.cos(toRad);
		final float x0 = (float) (0.5 + 0.5 * nx0);
		final float x1 = (float) (0.5 + 0.5 * nx1);
		final float z0 = (float) (0.5 + 0.5 * nz0);
		final float z1 = (float) (0.5 + 0.5 * nz1);
		final float uMin = (float) (fromRad / Math.PI / 2) * wrapDistance;
		final float uMax = (float) (toRad / Math.PI / 2) * wrapDistance;

		writer.maxU(0, wrapDistance);
		writer.maxV(0, 1);

		writer
			.surface(sideSurface)
			.vertex(0, x0, bottom, z0, uMin, bottom, 0xFFFFFFFF)
			.normal(0, nx0, 0, nz0)

			.vertex(1, x1, bottom, z1, uMax, bottom, 0xFFFFFFFF)
			.normal(1, nx1, 0, nz1)

			.vertex(2, x1, top, z1, uMax, top, 0xFFFFFFFF)
			.normal(2, nx1, 0, nz1)

			.vertex(3, x0, top, z0, uMin, top, 0xFFFFFFFF)
			.normal(3, nx0, 0, nz0)

			.nominalFace(writer.lightFace())
			.apply(transform)
			.append();
	}

	/**
	 *
	 * @param slice
	 * @param sliceRadians
	 * @param writer
	 * @param transform
	 * @param surface
	 * @param face
	 * @param subDivide Use true for CSG surfaces that may benefit from better recombination. Especially hollow cylinders.
	 */
	private static void cylEnd(
		int slice,
		double sliceRadians,
		MutablePolygon writer,
		Consumer<MutablePolygon> transform,
		XmSurface surface,
		Direction face,
		boolean subDivide,
		float y
	) {
		final double fromRad = slice * sliceRadians;
		final double midRad = fromRad + sliceRadians;
		final double toRad = midRad + sliceRadians;
		final float x0 = (float) (0.5 + 0.5 * Math.sin(fromRad));
		final float x1 = (float) (0.5 + 0.5 * Math.sin(midRad));
		final float x2 = (float) (0.5 + 0.5 * Math.sin(toRad));
		final float z0 = (float) (0.5 + 0.5 * Math.cos(fromRad));
		final float z1 = (float) (0.5 + 0.5 * Math.cos(midRad));
		final float z2 = (float) (0.5 + 0.5 * Math.cos(toRad));

		if (face == Direction.UP) {
			if (subDivide) {
				writer
					.vertexCount(3)
					.vertex(0, x0, y, z0, x0, z0, 0xFFFFFFFF)
					.vertex(1, x1, y, z1, x1, z1, 0xFFFFFFFF)
					.vertex(2, 0.5f, y, 0.5f, 0.5f, 0.5f, 0xFFFFFFFF)
					.surface(surface)
					.clearFaceNormal()
					.nominalFace(face)
					.apply(transform)
					.append();

				writer
					.vertexCount(3)
					.vertex(0, x1, y, z1, x1, z1, 0xFFFFFFFF)
					.vertex(1, x2, y, z2, x2, z2, 0xFFFFFFFF)
					.vertex(2, 0.5f, y, 0.5f, 0.5f, 0.5f, 0xFFFFFFFF);
			} else {
				writer
					.vertex(0, x0, y, z0, x0, z0, 0xFFFFFFFF)
					.vertex(1, x1, y, z1, x1, z1, 0xFFFFFFFF)
					.vertex(2, x2, y, z2, x2, z2, 0xFFFFFFFF)
					.vertex(3, 0.5f, y, 0.5f, 0.5f, 0.5f, 0xFFFFFFFF);
			}
		} else {
			if (subDivide) {
				writer
					.vertexCount(3)
					.vertex(0, x1, y, z1, x1, z1, 0xFFFFFFFF)
					.vertex(1, x0, y, z0, x0, z0, 0xFFFFFFFF)
					.vertex(2, 0.5f, y, 0.5f, 0.5f, 0.5f, 0xFFFFFFFF)
					.surface(surface)
					.clearFaceNormal()
					.nominalFace(face)
					.apply(transform)
					.append();

				writer
					.vertexCount(3)
					.vertex(0, x2, y, z2, x2, z2, 0xFFFFFFFF)
					.vertex(1, x1, y, z1, x1, z1, 0xFFFFFFFF)
					.vertex(2, 0.5f, y, 0.5f, 0.5f, 0.5f, 0xFFFFFFFF);
			} else {
				writer
					.vertex(0, x0, y, z0, x0, z0, 0xFFFFFFFF)
					.vertex(1, 0.5f, y, 0.5f, 0.5f, 0.5f, 0xFFFFFFFF)
					.vertex(2, x2, y, z2, x2, z2, 0xFFFFFFFF)
					.vertex(3, x1, y, z1, x1, z1, 0xFFFFFFFF);
			}
		}

		writer
			.surface(surface)
			.clearFaceNormal()
			.nominalFace(face)
			.apply(transform)
			.append();
	}

	/**
	 * Adds box to stream using current stream defaults.
	 */
	public static void box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, WritableMesh stream) {
		final MutablePolygon quad = stream.writer();
		quad.vertexCount(4);
		quad.setupFaceQuad(Direction.UP, 1 - maxX, minZ, 1 - minX, maxZ, 1 - maxY, Direction.SOUTH);
		quad.append();

		quad.vertexCount(4);
		quad.setupFaceQuad(Direction.DOWN, minX, minZ, maxX, maxZ, minY, Direction.SOUTH);
		quad.append();

		// -X
		quad.vertexCount(4);
		quad.setupFaceQuad(Direction.WEST, minZ, minY, maxZ, maxY, minX, Direction.UP);
		quad.append();

		// +X
		quad.vertexCount(4);
		quad.setupFaceQuad(Direction.EAST, 1 - maxZ, minY, 1 - minZ, maxY, 1 - maxX, Direction.UP);
		quad.append();

		// -Z
		quad.vertexCount(4);
		quad.setupFaceQuad(Direction.NORTH, 1 - maxX, minY, 1 - minX, maxY, minZ, Direction.UP);
		quad.append();

		// +Z
		quad.vertexCount(4);
		quad.setupFaceQuad(Direction.SOUTH, minX, minY, maxX, maxY, 1 - maxZ, Direction.UP);
		quad.append();
	}

	//    /**
	//     * This method is intended for boxes that span multiple world blocks. Typically
	//     * used with unlocked UV coordinates and tiled surface painter. Will emit quads
	//     * with uv min/max outside the 0-1 range. Textures will render 1:1, no wrapping.
	//     *
	//     * TODO: incomplete / use polystream
	//     */
	//    public static List<IPolygon> makeBigBox(IVec3f origin, final float xSize, final float ySize, final float zSize,
	//            IMutablePolygon template) {
	//        ImmutableList.Builder<IPolygon> builder = ImmutableList.builder();
	//
	//        final float xEnd = origin.x() + xSize;
	//        final float yEnd = origin.y() + ySize;
	//        final float zEnd = origin.z() + zSize;
	//
	//        IMutablePolygon quad = template.claimCopy(4);
	//        quad.setLockUV(0, false);
	//        quad.setMinU(0, 0);
	//        quad.setMaxU(0, xSize);
	//        quad.setMinV(0, 0);
	//        quad.setMaxV(0, zSize);
	//        quad.setNominalFace(Direction.UP);
	//        quad.setVertex(0, xEnd, yEnd, origin.z(), 0, 0, 0xFFFFFFFF, 0, 1, 0);
	//        quad.setVertex(1, origin.x(), yEnd, origin.z(), 0, 0, 0xFFFFFFFF, 0, 1, 0);
	//        quad.setVertex(2, origin.x(), yEnd, zEnd, 0, 0, 0xFFFFFFFF, 0, 1, 0);
	//        quad.setVertex(3, xEnd, yEnd, zEnd, 0, 0, 0xFFFFFFFF, 0, 1, 0);
	////        quad.setupFaceQuad(Direction.UP, 1 - box.maxX, box.minZ, 1 - box.minX, box.maxZ, 1 - box.maxY, Direction.SOUTH);
	//        builder.add(quad);
	//
	////        quad = Poly.mutable(template);
	////        quad.setupFaceQuad(Direction.DOWN, box.minX, box.minZ, box.maxX, box.maxZ, box.minY, Direction.SOUTH);
	////        builder.add(quad);
	////
	////        //-X
	////        quad = Poly.mutable(template);
	////        quad.setupFaceQuad(Direction.WEST, box.minZ, box.minY, box.maxZ, box.maxY, box.minX, Direction.UP);
	////        builder.add(quad);
	////
	////        //+X
	////        quad = Poly.mutable(template);
	////        quad.setupFaceQuad(Direction.EAST, 1 - box.maxZ, box.minY, 1 - box.minZ, box.maxY, 1 - box.maxX, Direction.UP);
	////        builder.add(quad);
	////
	////        //-Z
	////        quad = Poly.mutable(template);
	////        quad.setupFaceQuad(Direction.NORTH, 1 - box.maxX, box.minY, 1 - box.minX, box.maxY, box.minZ, Direction.UP);
	////        builder.add(quad);
	////
	////        //+Z
	////        quad = Poly.mutable(template);
	////        quad.setupFaceQuad(Direction.SOUTH, box.minX, box.minY, box.maxX, box.maxY, 1 - box.maxZ, Direction.UP);
	////        builder.add(quad);
	//
	//        return builder.build();
	//    }

	// TODO: fix or remove
	//    /**
	//     * Generates a quad that isn't uv-locked - originally for putting symbols on
	//     * MatterPackaging Cubes. Bit of a mess, but thought might get some reuse out of
	//     * it, so putting here.
	//     *
	//     * @param createMutable  if true will add Paintable (mutable) quads. Painted
	//     *                       (immutable) otherwise.
	//     * @param rawTextureName should not have mod/blocks prefix
	//     * @param top            using semantic coordinates here; 0,0 is lower right of
	//     *                       face
	//     * @param left
	//     * @param size           assuming square box
	//     * @param scaleFactor    quads will be scaled out from center by this- use value
	//     *                       > 1 to bump out overlays
	//     * @param color          color of textures
	//     * @param uvFraction     how much of texture to include, starting from u,v 0,0.
	//     *                       Pass 1 to include whole texture. Mainly of use when
	//     *                       trying to apply big textures to item models and don't
	//     *                       want whole thing.
	//     * @param contractUVs    should be true for everything except fonts maybe
	//     * @param list           your mutable list of quads
	//     */
	//    @SuppressWarnings("unchecked")
	//    public static <T extends IPolygon> void addTextureToAllFaces(boolean createMutable, String rawTextureName,
	//            float left, float top, float size, float scaleFactor, int color, boolean contractUVs, float uvFraction,
	//            Rotation texturRotation, List<T> list) {
	//        IMutablePolygon template = PolyFactory.COMMON_POOL.newPaintable(4)
	//                .setTextureName(0, "hard_science:blocks/" + rawTextureName).setLockUV(0, false)
	//                .setShouldContractUVs(0, contractUVs);
	//
	//        float bottom = top - size;
	//        float right = left + size;
	//
	//        FaceVertex[] fv = new FaceVertex[4];
	//
	//        switch (texturRotation) {
	//        case ROTATE_180:
	//            fv[0] = new FaceVertex.UV(left, bottom, 0, uvFraction, 0);
	//            fv[1] = new FaceVertex.UV(right, bottom, 0, 0, 0);
	//            fv[2] = new FaceVertex.UV(right, top, 0, 0, uvFraction);
	//            fv[3] = new FaceVertex.UV(left, top, 0, uvFraction, uvFraction);
	//            break;
	//
	//        case ROTATE_270:
	//            fv[0] = new FaceVertex.UV(left, bottom, 0, 0, 0);
	//            fv[1] = new FaceVertex.UV(right, bottom, 0, 0, uvFraction);
	//            fv[2] = new FaceVertex.UV(right, top, 0, uvFraction, uvFraction);
	//            fv[3] = new FaceVertex.UV(left, top, 0, uvFraction, 0);
	//            break;
	//
	//        case ROTATE_90:
	//            fv[0] = new FaceVertex.UV(left, bottom, 0, uvFraction, uvFraction);
	//            fv[1] = new FaceVertex.UV(right, bottom, 0, uvFraction, 0);
	//            fv[2] = new FaceVertex.UV(right, top, 0, 0, 0);
	//            fv[3] = new FaceVertex.UV(left, top, 0, 0, uvFraction);
	//            break;
	//
	//        case ROTATE_NONE:
	//        default:
	//            fv[0] = new FaceVertex.UV(left, bottom, 0, 0, uvFraction);
	//            fv[1] = new FaceVertex.UV(right, bottom, 0, uvFraction, uvFraction);
	//            fv[2] = new FaceVertex.UV(right, top, 0, uvFraction, 0);
	//            fv[3] = new FaceVertex.UV(left, top, 0, 0, 0);
	//            break;
	//
	//        }
	//
	//        for (Direction face : Direction.VALUES) {
	//            template.setupFaceQuad(face, fv[0], fv[1], fv[2], fv[3], null);
	//            template.scaleFromBlockCenter(scaleFactor);
	//            list.add((T) (createMutable ? template.claimCopy() : template.toPainted()));
	//        }
	//
	//        template.release();
	//    }
}
