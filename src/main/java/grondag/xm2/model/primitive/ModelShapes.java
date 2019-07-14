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

package grondag.xm2.model.primitive;

import java.util.ArrayList;
import java.util.HashMap;

public class ModelShapes {

    public static final int MAX_SHAPES = 128;
    static final HashMap<String, ModelShape<?>> allByName = new HashMap<>();
    static final ArrayList<ModelShape<?>> allByOrdinal = new ArrayList<>();

    public static <V extends AbstractModelPrimitive> ModelShape<V> create(String systemName, Class<V> meshFactoryClass) {
        return new ModelShape<V>(systemName, meshFactoryClass);
    }

    public static ModelShape<?> get(String systemName) {
        return ModelShapes.allByName.get(systemName);
    }

    public static ModelShape<?> get(int ordinal) {
        return ModelShapes.allByOrdinal.get(ordinal);
    }
}
