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
package grondag.xm.model.state;

import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.ModelPrimitive;

class ModelStateVaria {

    /**
     * Populates state flags for a given model state.
     * 
     * Results are returns as STATE_FLAG_XXXX values from ModelState for easy
     * persistence and usage within that class.
     */
    @SuppressWarnings("unchecked")
    static final <T extends AbstractPrimitiveModelState<T>> int getFlags(AbstractPrimitiveModelState<T> state) {
        final ModelPrimitive<T> mesh = state.primitive();

        int flags = ModelStateFlags.STATE_FLAG_IS_POPULATED | mesh.stateFlags((T) state);

        final int surfCount = mesh.surfaces((T) state).size();
        for (int i = 0; i < surfCount; i++) {
            XmPaint p = state.paint(i);
            final int texDepth = p.textureDepth();
            for (int j = 0; j < texDepth; j++) {
                flags |= p.texture(j).stateFlags();
            }
        }

        return flags;
    }
}
