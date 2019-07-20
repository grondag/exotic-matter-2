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

package grondag.xm2.painting;

import java.util.HashMap;

import grondag.fermion.structures.NullHandler;
import grondag.xm2.Xm;

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
            Xm.LOG.warn("Duplicate registration of vertex processor %s was ignored. Probable bug or configuration issue.");
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
