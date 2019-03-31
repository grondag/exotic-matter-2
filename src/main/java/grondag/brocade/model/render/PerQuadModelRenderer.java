package grondag.brocade.model.render;

import java.util.List;



import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;



/**
 * Selects lighter based on isShaded value of each individual quad, vs.
 * determining it at model level.
 *
 */

public class PerQuadModelRenderer extends BlockModelRenderer {

    public static final PerQuadModelRenderer INSTANCE = new PerQuadModelRenderer(
            MinecraftClient.getMinecraft().getBlockColors());

    private final ThreadLocal<VertexLighterFlat> lighterFlat = new ThreadLocal<VertexLighterFlat>() {
        @Override
        protected VertexLighterFlat initialValue() {
            return new VertexLighterFlat(colors);
        }
    };

    private final ThreadLocal<VertexLighterSmoothAo> lighterSmooth = new ThreadLocal<VertexLighterSmoothAo>() {
        @Override
        protected VertexLighterSmoothAo initialValue() {
            return new VertexLighterSmoothAo(colors);
        }
    };

    private final ThreadLocal<VertexBufferConsumer> wrFlat = new ThreadLocal<>();
    private final ThreadLocal<VertexBufferConsumer> wrSmooth = new ThreadLocal<>();
    private final ThreadLocal<BufferBuilder> lastRendererFlat = new ThreadLocal<>();
    private final ThreadLocal<BufferBuilder> lastRendererSmooth = new ThreadLocal<>();

    private final BlockColors colors;

    public PerQuadModelRenderer(BlockColors colors) {
        super(colors);
        this.colors = colors;
    }

    /** always returns true for convenience */
    private VertexLighterFlat setupFlat(IBlockAccess world, BakedModel model, BlockState state, BlockPos pos,
            BufferBuilder buffer) {
        VertexLighterFlat lighter = this.lighterFlat.get();
        if (buffer != this.lastRendererFlat.get()) {
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
    private VertexLighterFlat setupSmooth(IBlockAccess world, BakedModel model, BlockState state, BlockPos pos,
            BufferBuilder buffer) {
        VertexLighterFlat lighter = this.lighterSmooth.get();
        if (buffer != this.lastRendererSmooth.get()) {
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
    public boolean renderModel(IBlockAccess world, BakedModel model, BlockState state,
            BlockPos pos, BufferBuilder buffer, boolean checkSides, long rand) {

        boolean isEmpty = true;
        VertexLighterFlat flatLighter = null;
        VertexLighterFlat smoothLighter = null;

        List<BakedQuad> quads = model.getQuads(state, null, rand);
        if (!quads.isEmpty()) {
            isEmpty = false;
            for (BakedQuad quad : quads) {
                if (quad.shouldApplyDiffuseLighting()) {
                    if (smoothLighter == null)
                        smoothLighter = this.setupSmooth(world, model, state, pos, buffer);
                    quad.pipe(smoothLighter);
                } else {
                    if (flatLighter == null)
                        flatLighter = this.setupFlat(world, model, state, pos, buffer);
                    quad.pipe(flatLighter);
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            final Direction side = Direction.VALUES[i];
            quads = model.getQuads(state, side, rand);
            if (!quads.isEmpty()) {
                if (!checkSides || state.shouldSideBeRendered(world, pos, side)) {
                    isEmpty = false;
                    for (BakedQuad quad : quads) {
                        if (quad.shouldApplyDiffuseLighting()) {
                            if (smoothLighter == null)
                                smoothLighter = this.setupSmooth(world, model, state, pos, buffer);
                            quad.pipe(smoothLighter);
                        } else {
                            if (flatLighter == null)
                                flatLighter = this.setupFlat(world, model, state, pos, buffer);
                            quad.pipe(flatLighter);
                        }
                    }
                }
            }
        }
        return !isEmpty;

    }
}
