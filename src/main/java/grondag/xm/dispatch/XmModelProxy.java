/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.dispatch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.datafixers.util.Pair;

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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.MutableModelState;

// WIP: fabric deps
@Internal
@Environment(EnvType.CLIENT)
public class XmModelProxy extends AbstractXmModel implements UnbakedModel {
	private XmModelProxy() { }

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
