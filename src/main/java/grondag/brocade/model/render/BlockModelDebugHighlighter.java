package grondag.brocade.model.render;

import org.lwjgl.opengl.GL11;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.vertex.IVec3f;
import grondag.exotic_matter.model.varia.SuperDispatcher;
import grondag.exotic_matter.model.varia.SuperDispatcher.DispatchDelegate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.property.IExtendedBlockState;


public class BlockModelDebugHighlighter 
{
	public static void handleDrawBlockHighlightEvent(DrawBlockHighlightEvent event) 
	{
        BlockPos pos = event.getTarget().getBlockPos();
        EntityPlayer player = event.getPlayer();
        if(player != null && pos != null)
        {
            World world = player.world;
    		IBlockState bs = world.getBlockState(pos);
    		if (bs.getBlock() instanceof ISuperBlock) 
    		{
    		    ISuperBlock block = (ISuperBlock) bs.getBlock();
    		    
    		    DispatchDelegate delegate = SuperDispatcher.INSTANCE.getDelegate(block);
    		    
    		    bs = bs.getBlock().getExtendedState(bs, world, pos);
    		    
    	        Tessellator tessellator = Tessellator.getInstance();
    	        BufferBuilder bufferBuilder = tessellator.getBuffer();
    	        float partialTicks = event.getPartialTicks();
    	        
    	        
    	        double d0 = pos.getX() - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
    	        double d1 = pos.getY() - (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
    	        double d2 = pos.getZ() - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
    	        
    	        
    		    renderModel(d0, d1, d2, world, delegate, bs, pos, tessellator, bufferBuilder);
    		    
  
    		}
		}
	}
	
    private static void renderModel(
            double d0,
            double d1,
            double d2,
            IBlockAccess world,
            DispatchDelegate model, 
            IBlockState state, 
            BlockPos pos, 
            Tessellator tessellator, 
            BufferBuilder buffer)
    {

//        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.doPolygonOffset(-1f, -1f);
        GlStateManager.enablePolygonOffset();
        
        model.forAllPaintedQuads((IExtendedBlockState) state, q -> drawQuad(d0, d1, d2, q, tessellator, buffer));
        
//        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disablePolygonOffset();
    }
    
    
    private static void drawQuad(
            double d0,
            double d1,
            double d2,
            IPolygon quad, 
            Tessellator tessellator, 
            BufferBuilder buffer)
    {
        
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        final int vertexCount = quad.vertexCount();
        for(int i = 0; i < vertexCount; i++)
        {
            IVec3f v = quad.getPos(i);
            buffer.pos(v.x() + d0, v.y() + d1, v.z() + d2).color(1f, 1f, 1f, 1f).endVertex();
        }
        tessellator.draw();
        
        for(int i = 0; i < vertexCount; i++)
        {
            IVec3f v = quad.getPos(i);
            IVec3f n = quad.getVertexNormal(i);
            
            // only draw vertex normals that differ from the standard side normals
            int zeroCount = 0;
            if(Math.abs(n.x()) < 0.0000001) zeroCount++; 
            if(Math.abs(n.y()) < 0.0000001) zeroCount++;
            if(Math.abs(n.z()) < 0.0000001) zeroCount++; 
            if(zeroCount == 2) return;
            
            double px = v.x() + d0;
            double py = v.y() + d1;
            double pz = v.z() + d2;
            
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(px, py, pz).color(0.7f, 1f, 1f, 1f).endVertex();
            buffer.pos(px + n.x(), py + n.y(), pz + n.z()).color(0.7f, 1f, 1f, 1f).endVertex();
            tessellator.draw();
        }
    }
}
