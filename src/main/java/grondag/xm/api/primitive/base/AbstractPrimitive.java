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
package grondag.xm.api.primitive.base;

import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.xm.Xm;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.BaseModelStateFactory;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.ModelPrimitiveRegistry;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@Experimental
public abstract class AbstractPrimitive<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>> implements ModelPrimitive<R, W> {
	private final R defaultState;

	private final BaseModelStateFactory<R, W> factory;

	private final ResourceLocation id;

	private final Function<R, XmSurfaceList> surfaceFunc;

	/**
	 * bits flags used by ModelState to know which optional state elements are
	 * needed by this shape
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
