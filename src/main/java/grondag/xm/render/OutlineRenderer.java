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
package grondag.xm.render;

import org.jetbrains.annotations.ApiStatus.Internal;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.modelstate.ModelState;

@Internal
public class OutlineRenderer {
	public static final BlendMode[] RENDER_LAYERS = BlendMode.values();
	public static final int RENDER_LAYER_COUNT = RENDER_LAYERS.length;

	private static ModelState outlineModelState = null;
	private static final WritableMesh outlineMesh = XmMeshes.claimWritable();

	/**
	 * Draws block-aligned grid on sides of AABB if entity can see it from outside
	 */

	public static void drawModelOutline(PoseStack matrixStack, VertexConsumer vertexConsumer, ModelState modelState, double x, double y, double z, float r, float g, float b, float a) {
		if (modelState == null)
			return;

		final WritableMesh mesh = outlineMesh;
		final Matrix4f matrix4f = matrixStack.last().pose();
		final Matrix3f normalMatrix = matrixStack.last().normal();

		if(outlineModelState == null || !modelState.equals(outlineModelState)) {
			outlineModelState = modelState.toImmutable();
			mesh.clear();
			outlineModelState.emitPolygons(p -> mesh.appendCopy(p));
		}

		mesh.forEach(p -> {
			final int limit = p.vertexCount() - 1;

			for(int i = 0; i < limit; i++) {
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
