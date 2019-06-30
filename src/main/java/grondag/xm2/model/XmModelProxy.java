package grondag.xm2.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import grondag.xm2.block.XmBlock;
import grondag.xm2.block.XmStackHelper;
import grondag.xm2.dispatch.XmDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ExtendedBlockView;

@Environment(EnvType.CLIENT)
public class XmModelProxy extends AbstractXmModel implements UnbakedModel {
    private XmModelProxy() {}
    
    public static final XmModelProxy INSTANCE = new XmModelProxy();
    
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random rand) {
        return XmDispatcher.INSTANCE.get(((XmBlock)state.getBlock()).getDefaultModelState()).getBakedQuads(state, face, rand);
    }
    
    @Override
    public void emitBlockQuads(ExtendedBlockView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        XmDispatcher.INSTANCE.get(((XmBlock)state.getBlock()).getModelStateAssumeStateIsCurrent(state, blockView, pos, true)).emitQuads(context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        XmDispatcher.INSTANCE.get(XmStackHelper.getStackModelState(stack)).emitQuads(context);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Identifier> getTextureDependencies(Function<Identifier, UnbakedModel> var1, Set<String> var2) {
        return Collections.emptyList();
    }

    @Override
    public BakedModel bake(ModelLoader var1, Function<Identifier, Sprite> var2, ModelBakeSettings var3) {
        return this;
    }
}
