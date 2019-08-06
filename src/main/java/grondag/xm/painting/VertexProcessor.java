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

package grondag.xm.painting;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.polygon.IMutablePolygon;

/**
 * Logic to apply color, brightness, glow and other attributes that depend on
 * quad, surface, or model state to each vertex in the quad. Applied after UV
 * coordinates have been assigned.
 * <p>
 * 
 * While intended to assign color values, could also be used to transform UV,
 * normal or other vertex attributes.
 */
public abstract class VertexProcessor {
    // 0 is reserved for default instance because model state default ordinal value
    // is zero
    private static int nextOrdinal = 1;

    public final String registryName;
    public final int ordinal;

    protected VertexProcessor(String registryName) {
        this(registryName, nextOrdinal++);
    }

    /**
     * For default instance only
     */
    protected VertexProcessor(String registryName, int ordinal) {
        this.ordinal = ordinal;
        this.registryName = registryName;
    }

    // UGLY: fix parameter order to make consistent with related methods
    public abstract void process(IMutablePolygon result, int textureIndex, ModelState modelState, XmSurface surface, XmPaint paint);
}
