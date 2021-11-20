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

package grondag.xm.api.modelstate.base;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.surface.XmSurface;

// WIP: remove Fabric deps
@Experimental
public interface BaseModelState<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> extends ModelState {
	BaseModelStateFactory<R, W> factory();

	@Override
	R toImmutable();

	@Override
	W mutableCopy();

	/**
	 * Does NOT consider isStatic in comparison.
	 *
	 * {@inheritDoc}
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Returns true if visual elements and geometry match. Does not consider species
	 * in matching.
	 */
	boolean doShapeAndAppearanceMatch(ModelState other);

	/**
	 * Returns true if visual elements match. Does not consider species or geometry
	 * in matching.
	 */
	boolean doesAppearanceMatch(ModelState other);

	@Override
	void toTag(CompoundTag tag);

	void fromTag(CompoundTag tag, PaintIndex paintIndex);

	@Override
	void toBytes(FriendlyByteBuf pBuff);

	void fromBytes(FriendlyByteBuf pBuff, PaintIndex paintIndex);

	int stateFlags();

	ModelPrimitive<R, W> primitive();

	@Override
	void emitPolygons(Consumer<Polygon> target);

	@Override
	W geometricState();

	int orientationIndex();

	OrientationType orientationType();

	@Override
	boolean isStatic();

	boolean doPaintsMatch(ModelState other);

	XmPaint paint(int surfaceIndex);

	XmPaint paint(XmSurface surface);

	int posX();

	int posY();

	int posZ();

	/**
	 * Means that one or more elements (like a texture) uses species. Does not mean
	 * that the shape or block actually capture or generate species other than 0.
	 */
	boolean hasSpecies();

	/**
	 * Will return 0 if model state does not include species. This is more
	 * convenient than checking each place species is used.
	 *
	 * @return
	 */
	int species();

	CornerJoinState cornerJoin();

	SimpleJoinState simpleJoin();

	SimpleJoinState alternateJoin();

	int alternateJoinBits();

	int primitiveBits();

	////////////////////////////////////////// RENDERING //////////////////////////////////////////

	@Override
	@Environment(EnvType.CLIENT)
	List<BakedQuad> bakedQuads(BlockState state, Direction face, Random rand);

	@Override
	@Environment(EnvType.CLIENT)
	void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context);

	@Override
	@Environment(EnvType.CLIENT)
	void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context);
}
