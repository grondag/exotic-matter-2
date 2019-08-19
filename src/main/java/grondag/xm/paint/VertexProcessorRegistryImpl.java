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

package grondag.xm.paint;

import grondag.xm.Xm;
import grondag.xm.api.paint.VertexProcessor;
import grondag.xm.api.paint.VertexProcessorRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("unchecked")
public class VertexProcessorRegistryImpl extends DefaultedRegistry<VertexProcessor> implements VertexProcessorRegistry {
   
    public static final VertexProcessor DEFAULT_VERTEX_PROCESSOR = VertexProcessorDefault.INSTANCE;
    
    public static final VertexProcessorRegistryImpl INSTANCE;

    static {
        INSTANCE = (VertexProcessorRegistryImpl) Registry.REGISTRIES.add(Xm.id("vertex_proc"), 
                (MutableRegistry<?>) new VertexProcessorRegistryImpl(NONE_ID.toString()));
    }

    VertexProcessorRegistryImpl(String defaultIdString) {
        super(defaultIdString);
    }

    @Override
    public VertexProcessor add(Identifier id, VertexProcessor processor) {
        return super.add(id, processor);
    }
}
