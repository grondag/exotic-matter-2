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

package grondag.xm.api.primitive.base;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.BaseModelStateFactory;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.ModelPrimitiveRegistry;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@Experimental
public abstract class AbstractPrimitive<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> implements ModelPrimitive<R, W> {
	private final R defaultState;

	private final BaseModelStateFactory<R, W> factory;

	private final ResourceLocation id;

	private final Function<R, XmSurfaceList> surfaceFunc;

	/**
	 * Bits flags used by ModelState to know which optional state elements are
	 * needed by this shape.
	 */
	private final int stateFlags;

	protected AbstractPrimitive(ResourceLocation id, int stateFlags, BaseModelStateFactory<R, W> factory, Function<R, XmSurfaceList> surfaceFunc) {
		this.stateFlags = stateFlags;
		this.id = id;
		this.factory = factory;
		this.surfaceFunc = surfaceFunc;

		// we handle registration here because model state currently relies on it for
		// serialization
		if (!ModelPrimitiveRegistry.INSTANCE.register(this)) {
			Xm.LOG.warn("[XM2] Unable to register ModelPrimitive " + id.toString());
		}

		final W state = factory.claim(this);
		updateDefaultState(state);
		this.defaultState = state.releaseToImmutable();
	}

	@Override
	public final XmSurfaceList surfaces(R modelState) {
		return surfaceFunc.apply(modelState);
	}

	@Override
	public R defaultState() {
		return defaultState;
	}

	@Override
	public int stateFlags(R modelState) {
		return stateFlags;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	/**
	 * Override if default state should be something other than the, erm... default.
	 */
	protected void updateDefaultState(W modelState) {
	}

	@Override
	public final W fromBytes(FriendlyByteBuf buf, PaintIndex sync) {
		return factory.fromBytes(this, buf, sync);
	}

	@Override
	public final W fromTag(CompoundTag tag, PaintIndex sync) {
		return factory.fromTag(this, tag, sync);
	}
}
