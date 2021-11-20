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

package grondag.xm.api.primitive;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@Experimental
public interface ModelPrimitive<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> {
	/**
	 * Used for registration and serialization of model state.
	 */
	ResourceLocation id();

	/**
	 * Used for fast, transient serialization. Recommended that implementations
	 * override this and cache value to avoid map lookups.
	 */
	default int index() {
		return ModelPrimitiveRegistry.INSTANCE.indexOf(this);
	}

	/**
	 * This convention is used by XM2 but 3rd-party primitives can use a different
	 * one.
	 */
	default String translationKey() {
		return "xm2_primitive_name." + id().getNamespace() + "." + id().getPath();
	}

	XmSurfaceList surfaces(R modelState);

	@Nullable
	default XmSurface lampSurface(R modelState) {
		final XmSurface lamp = surfaces(modelState).lamp();
		return lamp == null ? null : modelState.paint(lamp).emissive(0) ? lamp : null;
	}

	/**
	 * Override if shape has an orientation to be selected during placement.
	 */
	default OrientationType orientationType(R modelState) {
		return OrientationType.NONE;
	}

	int stateFlags(R modelState);

	/**
	 * Output polygons must be quads or tris. Consumer MUST NOT hold references to
	 * any of the polys received.
	 */
	void emitQuads(R modelState, Consumer<Polygon> target);

	R defaultState();

	W geometricState(R fromState);

	default W newState() {
		return defaultState().mutableCopy();
	}

	W fromBytes(FriendlyByteBuf buf, PaintIndex sync);

	W fromTag(CompoundTag tag, PaintIndex sync);

	@Deprecated
	boolean doesShapeMatch(R from, R to);

	default boolean isMultiBlock() {
		return false;
	}

	default void invalidateCache() { }

	@Environment(EnvType.CLIENT)
	default void emitBlockMesh(Mesh mesh, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(mesh);
	}

	@Environment(EnvType.CLIENT)
	default void emitItemMesh(Mesh mesh, ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(mesh);
	}
}
