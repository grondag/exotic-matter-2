package grondag.xm2.block.virtual;

import javax.annotation.Nullable;

import grondag.xm2.Xm;
import grondag.xm2.XmConfig;
import grondag.xm2.dispatch.RenderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ExcavationRenderer
{
    public final int id;
    
    /**
     * If true, is replacement instead of straight excavation.
     */
    public final boolean isExchange;
    
    private Box aabb;
    
    private Box visibilityBounds;
    
    private boolean didDrawBoundsLastTime = false;
    
    private Vec3d lastEyePosition;
    
    /**
     * If non-null, then we should render individual positions instead of AABB.
     */
    private BlockPos[] positions;
    
    public ExcavationRenderer(int id, Box aabb, boolean isExchange, @Nullable BlockPos[] positions)
    {
        this.id = id;
        this.isExchange = isExchange;
        this.setBounds(aabb, positions);
    }
    
    public void setBounds(Box bounds, @Nullable BlockPos[] positions)
    {
        this.aabb = bounds;
        this.visibilityBounds = bounds.expand(192);
        this.positions = positions;
        
        if(XmConfig.logExcavationRenderTracking) Xm.LOG.info("id %d Renderer setBounds position count = %d", id, positions == null ? 0 : positions.length);
    }
    
    public Box bounds()
    {
        return this.aabb;
    }
    
    public Box visibilityBounds()
    {
        return this.visibilityBounds;
    }
    
    /** return true if something was drawn */
    @Environment(EnvType.CLIENT)
    public boolean drawBounds(BufferBuilder bufferbuilder, Entity viewEntity, double d0, double d1, double d2, float partialTicks)
    {
        this.lastEyePosition = viewEntity.getCameraPosVec(partialTicks);
        if(this.visibilityBounds.contains(this.lastEyePosition))
        {
            if(this.positions == null)
            {
                Box box = this.aabb;
                WorldRenderer.buildBox(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f, 1f);
            }
            else
            {
                for(BlockPos pos : this.positions)
                {
                    double x = pos.getX() - d0;
                    double y = pos.getY() - d1;
                    double z = pos.getZ() - d2;
                    WorldRenderer.buildBox(bufferbuilder, x, y, z, x + 1, y + 1, z + 1, 1f, 0.3f, 0.3f, 1f);
                }
            }
            this.didDrawBoundsLastTime = true;
            return true;
        }
        else
        {
            this.didDrawBoundsLastTime = false;
            return false;
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void drawGrid(BufferBuilder buffer, double d0, double d1, double d2)
    {
        if(this.didDrawBoundsLastTime && this.positions == null)
        {
            RenderUtil.drawGrid(buffer, this.aabb, this.lastEyePosition, d0, d1, d2, 1f, 0.3f, 0.3f, 0.5F);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void drawBox(BufferBuilder bufferbuilder, double d0, double d1, double d2)
    {
        if(this.didDrawBoundsLastTime)
        {
            if(this.positions == null)
            {
                Box box = this.aabb;
                WorldRenderer.buildBoxOutline(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f, 0.3f);
            }
            else
            {
                for(BlockPos pos : this.positions)
                {
                    double x = pos.getX() - d0;
                    double y = pos.getY() - d1;
                    double z = pos.getZ() - d2;
                    WorldRenderer.buildBoxOutline(bufferbuilder, x, y, z, x + 1, y + 1, z + 1, 1f, 0.3f, 0.3f, 0.3f);
                }
            }
        }
    }
}
