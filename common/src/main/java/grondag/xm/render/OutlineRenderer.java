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

package grondag.xm.render;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.modelstate.ModelState;

@Internal
public class OutlineRenderer {
	private static ModelState outlineModelState = null;
	private static final WritableMesh outlineMesh = XmMeshes.claimWritable();

	/**
	 * Draws block-aligned grid on sides of AABB if entity can see it from outside.
	 */

	public static void drawModelOutline(PoseStack matrixStack, VertexConsumer vertexConsumer, ModelState modelState, double x, double y, double z, float r, float g, float b, float a) {
		if (modelState == null) {
			return;
		}

		final WritableMesh mesh = outlineMesh;
		final Matrix4f matrix4f = matrixStack.last().pose();
		final Matrix3f normalMatrix = matrixStack.last().normal();

		if (outlineModelState == null || !modelState.equals(outlineModelState)) {
			outlineModelState = modelState.toImmutable();
			mesh.clear();
			outlineModelState.emitPolygons(p -> mesh.appendCopy(p));
		}

		mesh.forEach(p -> {
			final int limit = p.vertexCount() - 1;

			for (int i = 0; i < limit; i++) {
				// lines format wants a normal - god knows what for - we always pass in up
				vertexConsumer.vertex(matrix4f, (float) (p.x(i) + x), (float) (p.y(i) + y), (float) (p.z(i) + z)).color(r, g, b, a).normal(normalMatrix, 0, 1, 0).endVertex();
				vertexConsumer.vertex(matrix4f, (float) (p.x(i + 1) + x), (float) (p.y(i + 1) + y), (float) (p.z(i + 1) + z)).color(r, g, b, a).normal(normalMatrix, 0, 1, 0).endVertex();
			}
		});
	}

	@SuppressWarnings("unused")
	public static void drawGrid(BufferBuilder buffer, AABB aabb, Vec3 viewFrom, double offsetX, double offsetY, double offsetZ, float red, float green,
			float blue, float alpha) {
		final double minX = aabb.minX - offsetX;
		final double minY = aabb.minY - offsetY;
		final double minZ = aabb.minZ - offsetZ;
		final double maxX = aabb.maxX - offsetX;
		final double maxY = aabb.maxY - offsetY;
		final double maxZ = aabb.maxZ - offsetZ;
		final int xSpan = (int) (aabb.maxX + 0.0001 - aabb.minX);
		final int ySpan = (int) (aabb.maxY + 0.0001 - aabb.minY);
		final int zSpan = (int) (aabb.maxZ + 0.0001 - aabb.minZ);

		//TODO: reimplement for new rendering system
		//        if (xSpan > 1 && zSpan > 1) {
		//            double dy = viewFrom.y > aabb.maxY ? maxY : viewFrom.y < aabb.minY ? minY : Double.MAX_VALUE;
		//            if (dy != Double.MAX_VALUE) {
		//                for (int x = 1; x <= xSpan; x++) {
		//                    double dx = minX + x;
		//                    buffer.vertex(dx, dy, minZ).color(red, green, blue, alpha).end();
		//                    buffer.vertex(dx, dy, maxZ).color(red, green, blue, alpha).end();
		//                }
		//                for (int z = 1; z <= zSpan; z++) {
		//                    double dz = minZ + z;
		//                    buffer.vertex(minX, dy, dz).color(red, green, blue, alpha).end();
		//                    buffer.vertex(maxX, dy, dz).color(red, green, blue, alpha).end();
		//                }
		//            }
		//        }
		//
		//        if (ySpan > 1) {
		//            if (zSpan > 1) {
		//                double dx = viewFrom.x > aabb.maxX ? maxX : viewFrom.x < aabb.minX ? minX : Double.MAX_VALUE;
		//                if (dx != Double.MAX_VALUE) {
		//                    for (int y = 1; y <= ySpan; y++) {
		//                        double dy = minY + y;
		//                        buffer.vertex(dx, dy, minZ).color(red, green, blue, alpha).end();
		//                        buffer.vertex(dx, dy, maxZ).color(red, green, blue, alpha).end();
		//                    }
		//                    for (int z = 1; z <= zSpan; z++) {
		//                        double dz = minZ + z;
		//                        buffer.vertex(dx, minY, dz).color(red, green, blue, alpha).end();
		//                        buffer.vertex(dx, maxY, dz).color(red, green, blue, alpha).end();
		//                    }
		//                }
		//            }
		//
		//            if (xSpan > 1) {
		//                double dz = viewFrom.z > aabb.maxZ ? maxZ : viewFrom.z < aabb.minZ ? minZ : Double.MAX_VALUE;
		//                if (dz != Double.MAX_VALUE) {
		//                    for (int y = 1; y <= ySpan; y++) {
		//                        double dy = minY + y;
		//                        buffer.vertex(minX, dy, dz).color(red, green, blue, alpha).end();
		//                        buffer.vertex(maxX, dy, dz).color(red, green, blue, alpha).end();
		//                    }
		//                    for (int x = 1; x <= xSpan; x++) {
		//                        double dx = minX + x;
		//                        buffer.vertex(dx, minY, dz).color(red, green, blue, alpha).end();
		//                        buffer.vertex(dx, maxY, dz).color(red, green, blue, alpha).end();
		//                    }
		//                }
		//            }
		//        }
	}
}
