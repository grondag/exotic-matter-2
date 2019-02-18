package grondag.brocade.model.render;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Selects lighter based on isShaded value of each individual quad, 
 * vs. determining it at model level.
 *
 */
@SideOnly(Side.CLIENT)
public class PerQuadModelRenderer extends BlockModelRenderer
{
 
    public static final PerQuadModelRenderer INSTANCE = new PerQuadModelRenderer(Minecraft.getMinecraft().getBlockColors());
    
    private final ThreadLocal<VertexLighterFlat> lighterFlat = new ThreadLocal<VertexLighterFlat>()
    {
        @Override
        protected VertexLighterFlat initialValue()
        {
            return new VertexLighterFlat(colors);
        }
    };

    private final ThreadLocal<VertexLighterSmoothAo> lighterSmooth = new ThreadLocal<VertexLighterSmoothAo>()
    {
        @Override
        protected VertexLighterSmoothAo initialValue()
        {
            return new VertexLighterSmoothAo(colors);
        }
    };

    private final ThreadLocal<VertexBufferConsumer> wrFlat = new ThreadLocal<>();
    private final ThreadLocal<VertexBufferConsumer> wrSmooth = new ThreadLocal<>();
    private final ThreadLocal<BufferBuilder> lastRendererFlat = new ThreadLocal<>();
    private final ThreadLocal<BufferBuilder> lastRendererSmooth = new ThreadLocal<>();

    private final BlockColors colors;
    
    public PerQuadModelRenderer(BlockColors colors)
    {
        super(colors);
        this.colors = colors;
    }
    
    /** always returns true for convenience */
    private VertexLighterFlat setupFlat(IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder buffer)
    {
        VertexLighterFlat lighter = this.lighterFlat.get();
        if(buffer != this.lastRendererFlat.get())
        {
            this.lastRendererFlat.set(buffer);
            VertexBufferConsumer newCons = new VertexBufferConsumer(buffer);
            this.wrFlat.set(newCons);
            lighter.setParent(newCons);
        }
        this.wrFlat.get().setOffset(pos);
        lighter.setWorld(world);
        lighter.setState(state);
        lighter.setBlockPos(pos);
        lighter.updateBlockInfo();
        return lighter;
    }
    
    /** always returns true for convenience */
    private VertexLighterFlat setupSmooth(IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder buffer)
    {
        VertexLighterFlat lighter = this.lighterSmooth.get();
        if(buffer != this.lastRendererSmooth.get())
        {
            this.lastRendererSmooth.set(buffer);
            VertexBufferConsumer newCons = new VertexBufferConsumer(buffer);
            this.wrSmooth.set(newCons);
            lighter.setParent(newCons);
        }
        this.wrSmooth.get().setOffset(pos);
        lighter.setWorld(world);
        lighter.setState(state);
        lighter.setBlockPos(pos);
        lighter.updateBlockInfo();
        return lighter;
    }
    
    @Override
    public boolean renderModel(@Nonnull IBlockAccess world, @Nonnull IBakedModel model, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull BufferBuilder buffer, boolean checkSides, long rand)
    {

        boolean isEmpty = true;
        VertexLighterFlat flatLighter = null;
        VertexLighterFlat smoothLighter = null;
        
        List<BakedQuad> quads = model.getQuads(state, null, rand);
        if(!quads.isEmpty())
        {
            isEmpty = false;
            for(BakedQuad quad : quads)
            {
                if(quad.shouldApplyDiffuseLighting())
                {
                    if(smoothLighter == null) smoothLighter = this.setupSmooth(world, model, state, pos, buffer);
                    quad.pipe(smoothLighter);
                }
                else
                {
                    if(flatLighter == null) flatLighter = this.setupFlat(world, model, state, pos, buffer);
                    quad.pipe(flatLighter);
                }
            }
        }
        
        for(int i = 0; i < 6; i++)
        {
            final EnumFacing side = EnumFacing.VALUES[i];
            quads = model.getQuads(state, side, rand);
            if(!quads.isEmpty())
            {
                if(!checkSides || state.shouldSideBeRendered(world, pos, side))
                {
                    isEmpty = false;
                    for(BakedQuad quad : quads)
                    {
                        if(quad.shouldApplyDiffuseLighting())
                        {
                            if(smoothLighter == null) smoothLighter = this.setupSmooth(world, model, state, pos, buffer);
                            quad.pipe(smoothLighter);
                        }
                        else
                        {
                            if(flatLighter == null) flatLighter = this.setupFlat(world, model, state, pos, buffer);
                            quad.pipe(flatLighter);
                        }
                    }
                }
            }
        }
        return !isEmpty;

    }
}
