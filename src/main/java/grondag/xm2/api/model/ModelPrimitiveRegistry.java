package grondag.xm2.api.model;

import java.util.function.Consumer;

import grondag.xm2.model.ModelPrimitiveRegistryImpl;
import net.minecraft.util.Identifier;

public interface ModelPrimitiveRegistry {
	static ModelPrimitiveRegistry INSTANCE = ModelPrimitiveRegistryImpl.INSTANCE;
	
	boolean register(Identifier id, ModelPrimitive primitive);
	
	ModelPrimitive get(int primitiveIndex);
	
	default ModelPrimitive get(Identifier primitiveId) {
		return get(primitiveId.toString());
	}
	
	ModelPrimitive get(String idString);
	
	void forEach(Consumer<ModelPrimitive> consumer);
	
	int count();

	int indexOf(String idString);
	
	default int indexOf(Identifier primitiveId) {
		return indexOf(primitiveId.toString());
	}
	
	default int indexOf(ModelPrimitive primitive) {
		return indexOf(primitive.id());
	}
}
