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

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.phys.Vec3;

import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@Experimental
public class Icosahedron {
	public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("back", SurfaceTopology.TILED, XmSurface.FLAG_NONE).build();

	public static final XmSurface SURFACE_ALL = SURFACES.get(0);

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final WritableMesh mesh = XmMeshes.claimWritable();
		mesh
			.writer()
			.lockUV(0, false)
			.surface(SURFACE_ALL)
			.saveDefaults();

		icosahedron(new Vec3(.5, .5, .5), 0.6, mesh, false);
		return mesh.releaseToReader();
	};

	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
		.surfaceList(SURFACES)
		.polyFactory(POLY_FACTORY)
		.orientationType(OrientationType.NONE)
		.build(Xm.id("icosahedron"));

	/**
	 * Makes a regular icosahedron, which is a very close approximation to a sphere
	 * for most purposes. Loosely based on
	 * http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
	 */
	//PERF: use primitives instead of Vec3d
	public static void icosahedron(Vec3 center, double radius, WritableMesh mesh, boolean smoothNormals) {
		/** vertex scale */
		final double s = radius / (2 * Math.sin(2 * Math.PI / 5));

		final Vec3[] vertexes = new Vec3[12];

		// create 12 vertices of a icosahedron
		final double t = s * (1.0 + Math.sqrt(5.0)) / 2.0;
		int vi = 0;

		vertexes[vi++] = new Vec3(-s, t, 0).add(center);
		vertexes[vi++] = new Vec3(s, t, 0).add(center);
		vertexes[vi++] = new Vec3(-s, -t, 0).add(center);
		vertexes[vi++] = new Vec3(s, -t, 0).add(center);

		vertexes[vi++] = new Vec3(0, -s, t).add(center);
		vertexes[vi++] = new Vec3(0, s, t).add(center);
		vertexes[vi++] = new Vec3(0, -s, -t).add(center);
		vertexes[vi++] = new Vec3(0, s, -t).add(center);

		vertexes[vi++] = new Vec3(t, 0, -s).add(center);
		vertexes[vi++] = new Vec3(t, 0, s).add(center);
		vertexes[vi++] = new Vec3(-t, 0, -s).add(center);
		vertexes[vi++] = new Vec3(-t, 0, s).add(center);

		Vec3[] normals = null;

		if (smoothNormals) {
			normals = new Vec3[12];

			for (int i = 0; i < 12; i++) {
				normals[i] = vertexes[i].subtract(center).normalize();
			}
		}

		// create 20 triangles of the icosahedron

		final MutablePolygon writer = mesh.writer();
		writer.vertexCount(3);
		final XmSurface surface = writer.surface();

		if (surface.topology() == SurfaceTopology.TILED) {
			final float uvMax = (float) (2 * s);
			writer.maxU(0, uvMax);
			writer.maxV(0, uvMax);
			writer.uvWrapDistance(uvMax);
		}

		// enable texture randomization
		int salt = 0;
		writer.textureSalt(salt++);
		writer.saveDefaults();

		icosahedronFace(true, 0, 11, 5, vertexes, normals, mesh);
		icosahedronFace(false, 4, 5, 11, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 0, 5, 1, vertexes, normals, mesh);
		icosahedronFace(false, 9, 1, 5, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 0, 1, 7, vertexes, normals, mesh);
		icosahedronFace(false, 8, 7, 1, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 0, 7, 10, vertexes, normals, mesh);
		icosahedronFace(false, 6, 10, 7, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 0, 10, 11, vertexes, normals, mesh);
		icosahedronFace(false, 2, 11, 10, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 5, 4, 9, vertexes, normals, mesh);
		icosahedronFace(false, 3, 9, 4, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 11, 2, 4, vertexes, normals, mesh);
		icosahedronFace(false, 3, 4, 2, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 10, 6, 2, vertexes, normals, mesh);
		icosahedronFace(false, 3, 2, 6, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 7, 8, 6, vertexes, normals, mesh);
		icosahedronFace(false, 3, 6, 8, vertexes, normals, mesh);

		writer.textureSalt(salt++);
		writer.saveDefaults();
		icosahedronFace(true, 1, 9, 8, vertexes, normals, mesh);
		icosahedronFace(false, 3, 8, 9, vertexes, normals, mesh);
	}

	private static void icosahedronFace(boolean topHalf, int p1, int p2, int p3, Vec3[] points, Vec3[] normals, WritableMesh mesh) {
		final MutablePolygon writer = mesh.writer();

		if (normals == null) {
			if (topHalf) {
				writer.vertex(0, points[p1], 1, 1, 0xFFFFFFFF, null);
				writer.vertex(1, points[p2], 0, 1, 0xFFFFFFFF, null);
				writer.vertex(2, points[p3], 1, 0, 0xFFFFFFFF, null);
			} else {
				writer.vertex(0, points[p1], 0, 0, 0xFFFFFFFF, null);
				writer.vertex(1, points[p2], 1, 0, 0xFFFFFFFF, null);
				writer.vertex(2, points[p3], 0, 1, 0xFFFFFFFF, null);
			}
		} else {
			if (topHalf) {
				writer.vertex(0, points[p1], 1, 1, 0xFFFFFFFF, normals[p1]);
				writer.vertex(1, points[p2], 0, 1, 0xFFFFFFFF, normals[p2]);
				writer.vertex(2, points[p3], 1, 0, 0xFFFFFFFF, normals[p3]);
			} else {
				writer.vertex(0, points[p1], 0, 0, 0xFFFFFFFF, normals[p1]);
				writer.vertex(1, points[p2], 1, 0, 0xFFFFFFFF, normals[p2]);
				writer.vertex(2, points[p3], 0, 1, 0xFFFFFFFF, normals[p3]);
			}
		}

		// clear face normal if has been set somehow
		writer.clearFaceNormal();
		writer.nominalFace(writer.lightFace());
		writer.append();
	}
}
