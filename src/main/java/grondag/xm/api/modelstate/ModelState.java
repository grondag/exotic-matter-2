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
package grondag.xm.api.modelstate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiguardian.api.API;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.network.PaintSynchronizer;

@API(status = EXPERIMENTAL)
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

	void toBytes(PacketByteBuf pBuff, PaintSynchronizer sync);

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
	Sprite particleSprite();

	@Environment(EnvType.CLIENT)
	int particleColorARBG();

	@Environment(EnvType.CLIENT)
	void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context);

	@Environment(EnvType.CLIENT)
	void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context);
}
