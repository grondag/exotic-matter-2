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

package grondag.xm2.model.impl.primitive;

import grondag.xm2.Xm;
import net.minecraft.client.resource.language.I18n;

public class ModelShape<T extends AbstractModelPrimitive> {
    private static int nextOrdinal = 0;

    private final Class<T> meshFactoryClass;
    private final String systemName;
    private final int ordinal;
    private boolean factoryNeedLoad = true;
    private T factory = null;

    ModelShape(String systemName, Class<T> meshFactoryClass) {
        this.meshFactoryClass = meshFactoryClass;
        this.ordinal = nextOrdinal++;
        this.systemName = systemName;
        ModelShapes.allByName.put(systemName, this);
        if (this.ordinal < ModelShapes.MAX_SHAPES)
            ModelShapes.allByOrdinal.add(this);
        else
            Xm.LOG.warn(String.format("Model shape limit of %d exceeded.  Shape %s added but will not be rendered.",
                    ModelShapes.MAX_SHAPES, systemName));
    }

    public T meshFactory() {
        if (this.factoryNeedLoad) {
            try {
                this.factory = this.meshFactoryClass.newInstance();
            } catch (Exception e) {
                Xm.LOG
                        .error("Unable to load model factory for shape " + this.systemName + " due to error.", e);
            }
            factoryNeedLoad = false;
        }
        return this.factory;
    }

    public String localizedName() {
        return I18n.translate("shape." + this.systemName.toLowerCase());
    }

    public int ordinal() {
        return this.ordinal;
    }

    public String systemName() {
        return this.systemName;
    }
}
