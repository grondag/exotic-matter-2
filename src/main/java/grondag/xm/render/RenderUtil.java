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

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@API(status = INTERNAL)
public class RenderUtil {
    public static final BlockRenderLayer[] RENDER_LAYERS = BlockRenderLayer.values();
    public static final int RENDER_LAYER_COUNT = RENDER_LAYERS.length;

    /**
     * Draws block-aligned grid on sides of AABB if entity can see it from outside
     */

    public static void drawGrid(BufferBuilder buffer, Box aabb, Vec3d viewFrom, double offsetX, double offsetY, double offsetZ, float red, float green,
            float blue, float alpha) {
        double minX = aabb.minX - offsetX;
        double minY = aabb.minY - offsetY;
        double minZ = aabb.minZ - offsetZ;
        double maxX = aabb.maxX - offsetX;
        double maxY = aabb.maxY - offsetY;
        double maxZ = aabb.maxZ - offsetZ;
        int xSpan = (int) (aabb.maxX + 0.0001 - aabb.minX);
        int ySpan = (int) (aabb.maxY + 0.0001 - aabb.minY);
        int zSpan = (int) (aabb.maxZ + 0.0001 - aabb.minZ);

        if (xSpan > 1 && zSpan > 1) {
            double dy = viewFrom.y > aabb.maxY ? maxY : viewFrom.y < aabb.minY ? minY : Double.MAX_VALUE;
            if (dy != Double.MAX_VALUE) {
                for (int x = 1; x <= xSpan; x++) {
                    double dx = minX + x;
                    buffer.vertex(dx, dy, minZ).color(red, green, blue, alpha).end();
                    buffer.vertex(dx, dy, maxZ).color(red, green, blue, alpha).end();
                }
                for (int z = 1; z <= zSpan; z++) {
                    double dz = minZ + z;
                    buffer.vertex(minX, dy, dz).color(red, green, blue, alpha).end();
                    buffer.vertex(maxX, dy, dz).color(red, green, blue, alpha).end();
                }
            }
        }

        if (ySpan > 1) {
            if (zSpan > 1) {
                double dx = viewFrom.x > aabb.maxX ? maxX : viewFrom.x < aabb.minX ? minX : Double.MAX_VALUE;
                if (dx != Double.MAX_VALUE) {
                    for (int y = 1; y <= ySpan; y++) {
                        double dy = minY + y;
                        buffer.vertex(dx, dy, minZ).color(red, green, blue, alpha).end();
                        buffer.vertex(dx, dy, maxZ).color(red, green, blue, alpha).end();
                    }
                    for (int z = 1; z <= zSpan; z++) {
                        double dz = minZ + z;
                        buffer.vertex(dx, minY, dz).color(red, green, blue, alpha).end();
                        buffer.vertex(dx, maxY, dz).color(red, green, blue, alpha).end();
                    }
                }
            }

            if (xSpan > 1) {
                double dz = viewFrom.z > aabb.maxZ ? maxZ : viewFrom.z < aabb.minZ ? minZ : Double.MAX_VALUE;
                if (dz != Double.MAX_VALUE) {
                    for (int y = 1; y <= ySpan; y++) {
                        double dy = minY + y;
                        buffer.vertex(minX, dy, dz).color(red, green, blue, alpha).end();
                        buffer.vertex(maxX, dy, dz).color(red, green, blue, alpha).end();
                    }
                    for (int x = 1; x <= xSpan; x++) {
                        double dx = minX + x;
                        buffer.vertex(dx, minY, dz).color(red, green, blue, alpha).end();
                        buffer.vertex(dx, maxY, dz).color(red, green, blue, alpha).end();
                    }
                }
            }
        }
    }

}
