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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.primitive.ModelPrimitiveRegistryImpl;

@Experimental
public interface ModelPrimitiveRegistry {
	ModelPrimitiveRegistry INSTANCE = ModelPrimitiveRegistryImpl.INSTANCE;

	<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> boolean register(ModelPrimitive<R, W> primitive);

	<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> ModelPrimitive<R, W> get(int primitiveIndex);

	default <R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> ModelPrimitive<R, W> get(ResourceLocation primitiveId) {
		return get(primitiveId.toString());
	}

	<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> ModelPrimitive<R, W> get(String idString);

	@SuppressWarnings("rawtypes")
	void forEach(Consumer<ModelPrimitive> consumer);

	int count();

	int indexOf(String idString);

	default int indexOf(ResourceLocation primitiveId) {
		return indexOf(primitiveId.toString());
	}

	default <R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> int indexOf(ModelPrimitive<R, W> primitive) {
		return indexOf(primitive.id());
	}

	<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> W fromTag(CompoundTag tag, PaintIndex sync);

	<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> W fromBytes(FriendlyByteBuf buf, PaintIndex sync);
}
