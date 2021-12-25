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

package grondag.xm.api.primitive.simple;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.phys.Vec3;

import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.orientation.api.OrientationType;

@Experimental
public class IcosahedralSphere {
	public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("back", SurfaceTopology.TILED, XmSurface.FLAG_NONE).build();

	public static final XmSurface SURFACE_ALL = SURFACES.get(0);

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final WritableMesh mesh = XmMeshes.claimWritable();
		mesh
			.writer()
			.lockUV(0, false)
			.surface(SURFACE_ALL)
			.saveDefaults();

		sphere(mesh);
		return mesh.releaseToReader();

		//        return XmMeshes.claimRecoloredCopy(mesh);
	};

	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
			.surfaceList(SURFACES)
			.polyFactory(POLY_FACTORY)
			.orientationType(OrientationType.NONE)
			.build(Xm.id("ico_sphere"));

	public static void sphere(WritableMesh mesh) {
		final WritableMesh icoMesh = XmMeshes.claimWritable();
		icoMesh
			.writer()
			.surface(mesh.writer().surface())
			.lockUV(0, false)
			.saveDefaults();
		Icosahedron.icosahedron(Vec3.ZERO, 0.5, icoMesh, true);

		final Polygon reader = icoMesh.reader();
		reader.origin();

		do {
			subdivideAndEmit(reader, mesh);
		} while (reader.next());

		icoMesh.release();
	}

	/**
	 * Creates four quads in place of each triangle in the original icosahedron mesh.
	 * Each triangle edge is subdivided at midpoint and last vertex is always the
	 * triangle centroid.  Each point is then scaled to be 0.5 from origin.
	 */
	static void subdivideAndEmit(Polygon poly, WritableMesh output) {
		final MutablePolygon writer = output.writer();
		writer.spriteDepth(1);
		writer.vertexCount(3);
		writer.saveDefaults();

		final float xCenter = (poly.x(0) + poly.x(1) + poly.x(2)) / 3;
		final float yCenter = (poly.y(0) + poly.y(1) + poly.y(2)) / 3;
		final float zCenter = (poly.z(0) + poly.z(1) + poly.z(2)) / 3;
		final float uCenter = (poly.u(0, 0) + poly.u(1, 0) + poly.u(2, 0)) / 3;
		final float vCenter = (poly.v(0, 0) + poly.v(1, 0) + poly.v(2, 0)) / 3;

		subdiveFace(poly, output, 0, xCenter, yCenter, zCenter, uCenter, vCenter);
		subdiveFace(poly, output, 1, xCenter, yCenter, zCenter, uCenter, vCenter);
		subdiveFace(poly, output, 2, xCenter, yCenter, zCenter, uCenter, vCenter);
	}

	static void subdiveFace(Polygon poly, WritableMesh output, int startVertex, float xCenter, float yCenter, float zCenter, float uCenter, float vCenter) {
		final int endVertex = startVertex == 2 ? 0 : startVertex + 1;
		final MutablePolygon writer = output.writer();
		final float xMid = (poly.x(startVertex) + poly.x(endVertex)) / 2;
		final float yMid = (poly.y(startVertex) + poly.y(endVertex)) / 2;
		final float zMid = (poly.z(startVertex) + poly.z(endVertex)) / 2;
		final float uMid = (poly.u(startVertex, 0) + poly.u(endVertex, 0)) / 2;
		final float vMid = (poly.v(startVertex, 0) + poly.v(endVertex, 0)) / 2;

		writer.copyFrom(poly, false);
		writer.copyVertexFrom(0, poly, startVertex);
		writer.pos(1, xMid, yMid, zMid);
		writer.uv(1, 0, uMid, vMid);
		writer.color(1, 0, 0xFFFFFFFF);
		writer.pos(2, xCenter, yCenter, zCenter);
		writer.uv(2, 0, uCenter, vCenter);
		writer.color(2, 0, 0xFFFFFFFF);
		scalePoly(writer);
		writer.translate(0.5f);
		writer.append();

		writer.copyFrom(poly, false);
		writer.pos(0, xMid, yMid, zMid);
		writer.uv(0, 0, uMid, vMid);
		writer.color(0, 0, 0xFFFFFFFF);
		writer.copyVertexFrom(1, poly, endVertex);
		writer.pos(2, xCenter, yCenter, zCenter);
		writer.uv(2, 0, uCenter, vCenter);
		writer.color(2, 0, 0xFFFFFFFF);
		scalePoly(writer);
		writer.translate(0.5f);
		writer.append();
	}

	static void scalePoly(MutablePolygon poly) {
		// is at orgin, so position is essentially a vector
		final int limit = poly.vertexCount();

		for (int i = 0; i < limit; i++) {
			scaleVertex(poly, i);
		}
	}

	static void scaleVertex(MutablePolygon poly, int i) {
		final float x = poly.x(i);
		final float y = poly.y(i);
		final float z = poly.z(i);
		final float scale = (float) (0.5 / Math.sqrt(x * x + y * y + z * z));
		poly.pos(i, x * scale, y * scale, z * scale);

		// because this is centered on origin and radius is 0.5, normal is simply position x 2
		poly.normal(i, poly.x(i) * 2, poly.y(i) * 2, poly.z(i) * 2);
	}
}
