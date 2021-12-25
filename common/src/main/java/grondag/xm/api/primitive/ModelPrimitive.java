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

package grondag.xm.api.primitive;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.vram.frex.api.buffer.QuadSink;
import io.vram.frex.api.mesh.Mesh;
import io.vram.frex.api.model.BlockModel.BlockInputContext;
import io.vram.frex.api.model.ItemModel.ItemInputContext;

import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.orientation.api.OrientationType;

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
	default void emitBlockMesh(Mesh mesh, BlockInputContext input, QuadSink output) {
		mesh.outputTo(output);
	}

	@Environment(EnvType.CLIENT)
	default void emitItemMesh(Mesh mesh, ItemInputContext input, QuadSink output) {
		mesh.outputTo(output);
	}
}
