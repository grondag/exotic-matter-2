package grondag.xm2.state;

import java.util.List;
import java.util.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;

public interface ImmutableModelState extends ModelState {

    @Environment(EnvType.CLIENT)
    List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand);

    @Environment(EnvType.CLIENT)
    void emitQuads(RenderContext context);

}
