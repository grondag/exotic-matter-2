package grondag.brocade.block;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import grondag.exotic_matter.model.render.PerQuadModelRenderer;
import grondag.exotic_matter.model.render.RenderLayout;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.varia.SuperDispatcher;
import grondag.exotic_matter.model.varia.SuperDispatcher.DispatchDelegate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class SuperBlockTESR extends TileEntitySpecialRenderer<SuperTileEntity>
{
    protected static void addVertexWithUV(BufferBuilder buffer, double x, double y, double z, double u, double v, int skyLight, int blockLight)
    {
        buffer.pos(x, y, z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(u, v).lightmap(skyLight, blockLight).endVertex();
    }

    private final DispatchDelegate tesrDelegate = SuperDispatcher.INSTANCE.delegates[RenderLayout.NONE.ordinal];
    
    @Override
    public void render(@Nonnull SuperTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if(te != null)
        {
            
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            BlockPos pos = te.getPos();
            buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
            renderBlock(te, buffer);
            buffer.setTranslation(0, 0, 0);
        }
    }

    
    protected void renderBlock(SuperTileEntity te, BufferBuilder buffer)
    {
        SuperBlock block = (SuperBlock) te.getBlockType();
        
        if(MinecraftForgeClient.getRenderPass() == 0)
        {
            ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);
            
            // FIXME: only do this when texture demands it and use FastTESR other times
            GlStateManager.disableAlpha();
            renderBlockInner(te, block, false, buffer);
            GlStateManager.enableAlpha();
            ForgeHooksClient.setRenderLayer(null);
        }
        else if(MinecraftForgeClient.getRenderPass() == 1)
        {
            ForgeHooksClient.setRenderLayer(BlockRenderLayer.TRANSLUCENT);
            renderBlockInner(te, block, true, buffer);
            ForgeHooksClient.setRenderLayer(null);
        }
    }
    
    protected void renderBlockInner(SuperTileEntity te, ISuperBlock block, boolean translucent, BufferBuilder buffer)
    {
      
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        if(translucent)
        {
            GlStateManager.disableCull();
        }

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        
      
        World world = te.getWorld();
        ISuperModelState modelState = te.getCachedModelState();
        IBlockState state = ((IExtendedBlockState)world.getBlockState(te.getPos())).withProperty(ISuperBlock.MODEL_STATE,  modelState);
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
       
        
        if (translucent ) 
        {
            
            if(modelState.getRenderLayout().containsBlockRenderLayer(BlockRenderLayer.TRANSLUCENT))
            {
                PerQuadModelRenderer.INSTANCE.renderModel(world, this.tesrDelegate, state, te.getPos(), buffer, true, 0L);

                // FIXME: do this if TESR?
                buffer.sortVertexData((float) TileEntityRendererDispatcher.staticPlayerX,
                        (float) TileEntityRendererDispatcher.staticPlayerY, (float) TileEntityRendererDispatcher.staticPlayerZ);
            }
        }
        else
        {
            if(modelState.getRenderLayout().containsBlockRenderLayer(BlockRenderLayer.SOLID))
            {
                PerQuadModelRenderer.INSTANCE.renderModel(world, this.tesrDelegate, state, te.getPos(), buffer, true, 0L);
            }
        }
        
        Tessellator.getInstance().draw();

        RenderHelper.enableStandardItemLighting();
    }
}
