/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.dispatch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.vram.frex.api.buffer.QuadSink;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.MutableModelState;

@Internal
@Environment(EnvType.CLIENT)
public class XmModelProxy extends AbstractXmModel implements UnbakedModel {
	private XmModelProxy() { }

	public static final XmModelProxy INSTANCE = new XmModelProxy();

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource rand) {
		final XmBlockState xmState = XmBlockState.get(state);
		return xmState == null ? Collections.emptyList() : XmDispatcher.INSTANCE.get(xmState.defaultModelState()).bakedQuads(state, face, rand);
	}

	@Override
	public void renderAsBlock(BlockInputContext input, QuadSink output) {
		final MutableModelState modelState = XmBlockState.modelState(input.blockState(), input.blockView(), input.pos(), true);

		if (modelState != null) {
			XmDispatcher.INSTANCE.get(modelState).renderAsBlock(input, output);
			modelState.release();
		}
	}

	@Override
	public void renderAsItem(ItemInputContext input, QuadSink output) {
		final MutableModelState modelState = XmItem.modelState(input.itemStack());

		if (modelState != null) {
			XmDispatcher.INSTANCE.get(modelState).renderAsItem(input, output);
			modelState.release();
		}
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
	}

	@Override
	public BakedModel bake(ModelBaker var1, Function<Material, TextureAtlasSprite> var2, ModelState var3, ResourceLocation modelId) {
		return this;
	}

	@Override
	public void onNewTerrainParticle(@Nullable ClientLevel clientLevel, BlockState blockState, @Nullable BlockPos blockPos, TerrainParticleDelegate delegate) {
		final MutableModelState lookupState = XmBlockState.modelState(blockState, clientLevel, blockPos, false);

		if (lookupState != null) {
			final var renderState = XmDispatcher.INSTANCE.get(lookupState);
			lookupState.release();
			delegate.setModelParticleSprite(renderState.particleSprite());
			delegate.setModelParticleColor(renderState.particleColorARBG());
		}
	}
}
