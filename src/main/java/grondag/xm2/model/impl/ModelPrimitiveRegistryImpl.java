package grondag.xm2.model.impl;

import java.util.function.Consumer;

import grondag.xm2.model.api.ModelPrimitive;
import grondag.xm2.model.api.ModelPrimitiveRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Identifier;

public class ModelPrimitiveRegistryImpl implements ModelPrimitiveRegistry {
	public static ModelPrimitiveRegistryImpl INSTANCE = new ModelPrimitiveRegistryImpl();
	
	private final Object2ObjectOpenHashMap<Identifier, ModelPrimitive> map = new Object2ObjectOpenHashMap<>();
	private final ObjectArrayList<ModelPrimitive> list = new ObjectArrayList<>();
	private final Object2IntOpenHashMap<ModelPrimitive> reverseMap = new Object2IntOpenHashMap<>();
	
	private ModelPrimitiveRegistryImpl() {}
	
	@Override
	public synchronized boolean register(Identifier id, ModelPrimitive primitive) {
		boolean result = map.putIfAbsent(id, primitive) == null;
		if(result) {
			final int index = list.size();
			list.add(primitive);
			reverseMap.put(primitive, index);
		}
		return result;
	}

	@Override
	public ModelPrimitive get(int primitiveIndex) {
		return list.get(primitiveIndex);
	}

	@Override
	public ModelPrimitive get(Identifier primitiveId) {
		return map.get(primitiveId);
	}

	@Override
	public void forEach(Consumer<ModelPrimitive> consumer) {
		list.forEach(consumer);
	}

	@Override
	public int count() {
		return list.size();
	}
	
	@Override
	public int indexOf(Identifier id) {
		return reverseMap.getInt(id);
	}
}
