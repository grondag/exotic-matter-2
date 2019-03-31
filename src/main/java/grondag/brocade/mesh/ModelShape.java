package grondag.brocade.mesh;



import grondag.brocade.Brocade;
import grondag.brocade.model.state.MetaUsage;
import net.minecraft.client.resource.language.I18n;

public class ModelShape<T extends ShapeMeshGenerator> {
    private static int nextOrdinal = 0;

    private final Class<T> meshFactoryClass;
    private final boolean isAvailableInGui;
    private final MetaUsage metaUsage;
    private final String systemName;
    private final int ordinal;
    private boolean factoryNeedLoad = true;
    private T factory = null;

    ModelShape(String systemName, Class<T> meshFactoryClass, MetaUsage metaUsage, boolean isAvailableInGui) {
        this.meshFactoryClass = meshFactoryClass;
        this.ordinal = nextOrdinal++;
        this.systemName = systemName;
        this.metaUsage = metaUsage;
        this.isAvailableInGui = isAvailableInGui;
        ModelShapes.allByName.put(systemName, this);
        if (this.ordinal < ModelShapes.MAX_SHAPES)
            ModelShapes.allByOrdinal.add(this);
        else
            Brocade.INSTANCE.warn("Model shape limit of %d exceeded.  Shape %s added but will not be rendered.",
                    ModelShapes.MAX_SHAPES, systemName);
    }

    ModelShape(String systemName, Class<T> meshFactoryClass, MetaUsage metaUsage) {
        this(systemName, meshFactoryClass, metaUsage, true);
    }

    public T meshFactory() {
        if (this.factoryNeedLoad) {
            try {
                this.factory = this.meshFactoryClass.newInstance();
            } catch (Exception e) {
                Brocade.INSTANCE
                        .error("Unable to load model factory for shape " + this.systemName + " due to error.", e);
            }
            factoryNeedLoad = false;
        }
        return this.factory;
    }

    public String localizedName() {
        return I18n.translate("shape." + this.systemName.toLowerCase());
    }

    public MetaUsage metaUsage() {
        return this.metaUsage;
    }

    public boolean isAvailableInGui() {
        return this.isAvailableInGui;
    }

    public int ordinal() {
        return this.ordinal;
    }

    public String systemName() {
        return this.systemName;
    }
}
