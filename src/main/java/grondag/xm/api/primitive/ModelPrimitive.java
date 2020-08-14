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
package grondag.xm.api.primitive;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

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

@API(status = EXPERIMENTAL)
public interface ModelPrimitive<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>> {
	/**
	 * Used for registration and serialization of model state.
	 */
	Identifier id();

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

	W fromBytes(PacketByteBuf buf, PaintIndex sync);

	W fromTag(CompoundTag tag, PaintIndex sync);

	@Deprecated
	boolean doesShapeMatch(R from, R to);

	default boolean isMultiBlock() {
		return false;
	}

	default void invalidateCache() { }

	@Environment(EnvType.CLIENT)
	default void emitBlockMesh(Mesh mesh, BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(mesh);
	}

	@Environment(EnvType.CLIENT)
	default void emitItemMesh(Mesh mesh, ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(mesh);
	}
}
