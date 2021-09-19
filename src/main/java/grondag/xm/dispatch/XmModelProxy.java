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
package grondag.xm.dispatch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.MutableModelState;

@Internal
@Environment(EnvType.CLIENT)
public class XmModelProxy extends AbstractXmModel implements UnbakedModel {
	private XmModelProxy() {
	}

	public static final XmModelProxy INSTANCE = new XmModelProxy();

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction face, Random rand) {
		final XmBlockState xmState = XmBlockState.get(state);
		return xmState == null ? Collections.emptyList() : XmDispatcher.INSTANCE.get(xmState.defaultModelState()).bakedQuads(state, face, rand);
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		final MutableModelState modelState = XmBlockState.modelState(state, blockView, pos, true);

		if (modelState != null) {
			XmDispatcher.INSTANCE.get(modelState).emitBlockQuads(blockView, state, pos, randomSupplier, context);
			modelState.release();
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		final MutableModelState modelState = XmItem.modelState(stack);

		if (modelState != null) {
			XmDispatcher.INSTANCE.get(modelState).emitItemQuads(stack, randomSupplier, context);
			modelState.release();
		}
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> var1, Set<Pair<String, String>> var2) {
		return Collections.emptyList();
	}

	@Override
	public BakedModel bake(ModelBakery var1, Function<Material, TextureAtlasSprite> var2, ModelState var3, ResourceLocation modelId) {
		return this;
	}
}
