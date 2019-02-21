package grondag.brocade.painting;

import java.util.HashMap;

import grondag.exotic_matter.ExoticMatter;
import grondag.exotic_matter.varia.structures.NullHandler;

/**
 * Tracks vertex processors by name to support external processor registration
 * and/or addition/removal of processors by individual mod.
 */
public class VertexProcessors {
    public static final int MAX_PROCESSORS = 128;
    private static final HashMap<String, VertexProcessor> allByName = new HashMap<>();
    private static final VertexProcessor[] allByOrdinal = new VertexProcessor[MAX_PROCESSORS];

    public static void register(VertexProcessor vp) {
        if (allByName.containsKey(vp.registryName)) {
            ExoticMatter.INSTANCE.warn(
                    "Duplicate registration of vertex processor %s was ignored. Probable bug or configuration issue.");
        } else {
            allByName.put(vp.registryName, vp);
            assert allByOrdinal[vp.ordinal] == null : "Vertex processor registered with duplicate ordinal.";
            allByOrdinal[vp.ordinal] = vp;
        }
    }

    public static VertexProcessor get(String systemName) {
        return NullHandler.defaultIfNull(allByName.get(systemName), VertexProcessorDefault.INSTANCE);
    }

    public static VertexProcessor get(int ordinal) {
        return NullHandler.defaultIfNull(allByOrdinal[ordinal], VertexProcessorDefault.INSTANCE);
    }

}
