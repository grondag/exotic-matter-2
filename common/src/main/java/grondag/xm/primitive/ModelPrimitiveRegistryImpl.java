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

package grondag.xm.primitive;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.paint.PaintIndex;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.ModelPrimitiveRegistry;
import grondag.xm.modelstate.ModelStateTagHelper;

@SuppressWarnings("rawtypes")
@Internal
public class ModelPrimitiveRegistryImpl implements ModelPrimitiveRegistry {
	public static ModelPrimitiveRegistryImpl INSTANCE = new ModelPrimitiveRegistryImpl();

	private final Object2ObjectOpenHashMap<String, ModelPrimitive> map = new Object2ObjectOpenHashMap<>();
	private final ObjectArrayList<ModelPrimitive> list = new ObjectArrayList<>();
	private final Object2IntOpenHashMap<String> reverseMap = new Object2IntOpenHashMap<>();

	private ModelPrimitiveRegistryImpl() {
	}

	@Override
	public synchronized <R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> boolean register(ModelPrimitive<R, W> primitive) {
		final boolean result = map.putIfAbsent(primitive.id().toString(), primitive) == null;

		if (result) {
			final int index = list.size();
			list.add(primitive);
			reverseMap.put(primitive.id().toString(), index);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> ModelPrimitive<R, W> get(int primitiveIndex) {
		return list.get(primitiveIndex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> ModelPrimitive<R, W> get(String idString) {
		return map.get(idString);
	}

	@Override
	public synchronized void forEach(Consumer<ModelPrimitive> consumer) {
		list.forEach(consumer);
	}

	@Override
	public int count() {
		return list.size();
	}

	@Override
	public int indexOf(String idString) {
		return reverseMap.getInt(idString);
	}

	@Override
	public <R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> W fromTag(CompoundTag tag, PaintIndex paintIndex) {
		final ModelPrimitive<R, W> shape = get(tag.getString(ModelStateTagHelper.NBT_SHAPE));
		return shape == null ? null : shape.fromTag(tag, paintIndex);
	}

	@Override
	public <R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> W fromBytes(FriendlyByteBuf buf, PaintIndex paintIndex) {
		final ModelPrimitive<R, W> shape = get(buf.readVarInt());
		return shape == null ? null : shape.fromBytes(buf, paintIndex);
	}

	public void invalidateCache() {
		list.forEach(p -> p.invalidateCache());
	}
}
