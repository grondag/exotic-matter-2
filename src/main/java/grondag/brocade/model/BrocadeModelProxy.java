package grondag.brocade.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import grondag.brocade.block.BrocadeBlock;
import grondag.brocade.block.BrocadeBlockStackHelper;
import grondag.brocade.dispatch.BrocadeDispatcher;
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
public class BrocadeModelProxy extends AbstractBrocadeModel implements UnbakedModel {
    private BrocadeModelProxy() {}
    
    public static final BrocadeModelProxy INSTANCE = new BrocadeModelProxy();
    
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random rand) {
        return BrocadeDispatcher.INSTANCE.get(((BrocadeBlock)state.getBlock()).getDefaultModelState()).getBakedQuads(state, face, rand);
    }
    
    @Override
    public void emitBlockQuads(ExtendedBlockView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        BrocadeDispatcher.INSTANCE.get(((BrocadeBlock)state.getBlock()).getModelStateAssumeStateIsCurrent(state, blockView, pos, true)).emitQuads(context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        BrocadeDispatcher.INSTANCE.get(BrocadeBlockStackHelper.getStackModelState(stack)).emitQuads(context);
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
