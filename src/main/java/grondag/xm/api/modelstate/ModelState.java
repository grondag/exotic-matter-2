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

package grondag.xm.api.modelstate;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.vram.frex.api.buffer.QuadSink;
import io.vram.frex.api.model.BlockModel.BlockInputContext;
import io.vram.frex.api.model.ItemModel.ItemInputContext;

import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.primitive.ModelPrimitiveRegistryImpl;

@Experimental
public interface ModelState {
	MutableModelState mutableCopy();

	/**
	 * Persisted but not part of hash nor included in equals comparison. If true,
	 * refreshFromWorldState does nothing.
	 */
	boolean isStatic();

	boolean isImmutable();

	ModelState toImmutable();

	/**
	 * Returns a copy of this model state with only the bits that matter for
	 * geometry. Used as lookup key for block damage models.
	 */
	MutableModelState geometricState();

	void toTag(CompoundTag tag);

	void toBytes(FriendlyByteBuf pBuff);

	default CompoundTag toTag() {
		final CompoundTag result = new CompoundTag();
		toTag(result);
		return result;
	}

	/**
	 * Output polygons must be quads or tris. Consumer MUST NOT hold references to
	 * any of the polys received.
	 */
	void emitPolygons(Consumer<Polygon> target);

	@Environment(EnvType.CLIENT)
	List<BakedQuad> bakedQuads(BlockState state, Direction face, Random rand);

	@Environment(EnvType.CLIENT)
	TextureAtlasSprite particleSprite();

	@Environment(EnvType.CLIENT)
	int particleColorARBG();

	@Environment(EnvType.CLIENT)
	void renderAsBlock(BlockInputContext input, QuadSink output);

	@Environment(EnvType.CLIENT)
	void renderAsItem(ItemInputContext input, QuadSink output);

	@Environment(EnvType.CLIENT)
	BakedModel itemProxy();

	static MutableModelState fromTag(CompoundTag tag, PaintIndex paintIndex) {
		return ModelPrimitiveRegistryImpl.INSTANCE.fromTag(tag, paintIndex);
	}

	static MutableModelState fromBytes(FriendlyByteBuf pBuff, PaintIndex paintIndex) {
		return ModelPrimitiveRegistryImpl.INSTANCE.fromBytes(pBuff, paintIndex);
	}
}
