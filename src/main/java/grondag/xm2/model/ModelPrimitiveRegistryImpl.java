package grondag.xm2.model;

import java.util.function.Consumer;

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.ModelPrimitiveRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Identifier;

public class ModelPrimitiveRegistryImpl implements ModelPrimitiveRegistry {
    public static ModelPrimitiveRegistryImpl INSTANCE = new ModelPrimitiveRegistryImpl();

    private final Object2ObjectOpenHashMap<String, ModelPrimitive> map = new Object2ObjectOpenHashMap<>();
    private final ObjectArrayList<ModelPrimitive> list = new ObjectArrayList<>();
    private final Object2IntOpenHashMap<ModelPrimitive> reverseMap = new Object2IntOpenHashMap<>();

    private ModelPrimitiveRegistryImpl() {
    }

    @Override
    public synchronized boolean register(Identifier id, ModelPrimitive primitive) {
	boolean result = map.putIfAbsent(id.toString(), primitive) == null;
	if (result) {
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
    public ModelPrimitive get(String idString) {
	return map.get(idString);
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
    public int indexOf(String idString) {
	return reverseMap.getInt(idString);
    }
}
