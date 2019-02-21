package grondag.brocade.model.render;

import javax.vecmath.Vector3f;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RenderUtil {
    private static final float VOXEL_TEST_RAY_X;
    private static final float VOXEL_TEST_RAY_Y;
    private static final float VOXEL_TEST_RAY_Z;

    static {
        Vector3f ray = new Vector3f(5525, 13123, 7435);
        ray.normalize();
        VOXEL_TEST_RAY_X = ray.x;
        VOXEL_TEST_RAY_Y = ray.y;
        VOXEL_TEST_RAY_Z = ray.z;
    }

    public static final BlockRenderLayer[] RENDER_LAYERS = BlockRenderLayer.values();
    public static final int RENDER_LAYER_COUNT = RENDER_LAYERS.length;

    /**
     * Draws block-aligned grid on sides of AABB if entity can see it from outside
     */
    @SideOnly(Side.CLIENT)
    public static void drawGrid(BufferBuilder buffer, AxisAlignedBB aabb, Vec3d viewFrom, double offsetX,
            double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
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
                    buffer.pos(dx, dy, minZ).color(red, green, blue, alpha).endVertex();
                    buffer.pos(dx, dy, maxZ).color(red, green, blue, alpha).endVertex();
                }
                for (int z = 1; z <= zSpan; z++) {
                    double dz = minZ + z;
                    buffer.pos(minX, dy, dz).color(red, green, blue, alpha).endVertex();
                    buffer.pos(maxX, dy, dz).color(red, green, blue, alpha).endVertex();
                }
            }
        }

        if (ySpan > 1) {
            if (zSpan > 1) {
                double dx = viewFrom.x > aabb.maxX ? maxX : viewFrom.x < aabb.minX ? minX : Double.MAX_VALUE;
                if (dx != Double.MAX_VALUE) {
                    for (int y = 1; y <= ySpan; y++) {
                        double dy = minY + y;
                        buffer.pos(dx, dy, minZ).color(red, green, blue, alpha).endVertex();
                        buffer.pos(dx, dy, maxZ).color(red, green, blue, alpha).endVertex();
                    }
                    for (int z = 1; z <= zSpan; z++) {
                        double dz = minZ + z;
                        buffer.pos(dx, minY, dz).color(red, green, blue, alpha).endVertex();
                        buffer.pos(dx, maxY, dz).color(red, green, blue, alpha).endVertex();
                    }
                }
            }

            if (xSpan > 1) {
                double dz = viewFrom.z > aabb.maxZ ? maxZ : viewFrom.z < aabb.minZ ? minZ : Double.MAX_VALUE;
                if (dz != Double.MAX_VALUE) {
                    for (int y = 1; y <= ySpan; y++) {
                        double dy = minY + y;
                        buffer.pos(minX, dy, dz).color(red, green, blue, alpha).endVertex();
                        buffer.pos(maxX, dy, dz).color(red, green, blue, alpha).endVertex();
                    }
                    for (int x = 1; x <= xSpan; x++) {
                        double dx = minX + x;
                        buffer.pos(dx, minY, dz).color(red, green, blue, alpha).endVertex();
                        buffer.pos(dx, maxY, dz).color(red, green, blue, alpha).endVertex();
                    }
                }
            }
        }
    }

}
