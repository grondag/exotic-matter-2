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

package grondag.xm2.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import grondag.xm2.block.XmBlockStateAccess;
import grondag.xm2.block.XmStackHelper;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.dispatch.XmDispatcher;
import grondag.xm2.model.impl.state.ModelState;
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
    	final XmBlockStateImpl xmState = XmBlockStateAccess.get(state);
    	return xmState == null ? Collections.emptyList() : XmDispatcher.INSTANCE.get(xmState.defaultModelState).getBakedQuads(state, face, rand);
    }
    
    @Override
    public void emitBlockQuads(ExtendedBlockView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        final ModelState modelState = XmBlockStateAccess.modelState(state, blockView, pos, true);
    	if(modelState != null) {
    		XmDispatcher.INSTANCE.get(modelState).emitQuads(context);
    	}
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
    	final ModelState modelState = XmStackHelper.getStackModelState(stack);
    	if(modelState != null) {
    		XmDispatcher.INSTANCE.get(modelState).emitQuads(context);
    	}
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
