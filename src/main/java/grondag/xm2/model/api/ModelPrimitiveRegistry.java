package grondag.xm2.model.api;

import java.util.function.Consumer;

import grondag.xm2.model.impl.ModelPrimitiveRegistryImpl;
import net.minecraft.util.Identifier;

public interface ModelPrimitiveRegistry {
	static ModelPrimitiveRegistry INSTANCE = ModelPrimitiveRegistryImpl.INSTANCE;
	
	boolean register(Identifier id, ModelPrimitive primitive);
	
	ModelPrimitive get(int primitiveIndex);
	
	ModelPrimitive get(Identifier primitiveId);
	
	void forEach(Consumer<ModelPrimitive> consumer);
	
	int count();

	int indexOf(Identifier id);
	
	default int indexOf(ModelPrimitive primitive) {
		return indexOf(primitive.id());
	}
}
