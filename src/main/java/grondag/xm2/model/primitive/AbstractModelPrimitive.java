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

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.MutableModelState;
import grondag.xm2.model.state.StateFormat;
import grondag.xm2.surface.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.Identifier;

public abstract class AbstractModelPrimitive implements ModelPrimitive {
    private final XmSurfaceListImpl surfaces;

    /**
     * used by ModelState to know why type of state representation is needed by this
     * shape
     */
    public final StateFormat stateFormat;

    private final Identifier id;

    /**
     * bits flags used by ModelState to know which optional state elements are
     * needed by this shape
     */
    private final int stateFlags;

    protected AbstractModelPrimitive(Identifier id, XmSurfaceListImpl surfaces, StateFormat stateFormat,
	    int stateFlags) {
	this.surfaces = surfaces;
	this.stateFormat = stateFormat;
	this.stateFlags = stateFlags;
	this.id = id;
    }

    protected AbstractModelPrimitive(String idString, XmSurfaceListImpl surfaces, StateFormat stateFormat,
	    int stateFlags) {
	this(new Identifier(idString), surfaces, stateFormat, stateFlags);
    }

    @Override
    public int stateFlags(MutableModelState modelState) {
	return stateFlags;
    }

    @Override
    public Identifier id() {
	return id;
    }

    @Override
    public XmSurfaceListImpl surfaces() {
	return surfaces;
    }
}
