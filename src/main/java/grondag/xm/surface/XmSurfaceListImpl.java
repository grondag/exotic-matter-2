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
package grondag.xm.surface;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@API(status = INTERNAL)
class XmSurfaceListImpl implements XmSurfaceList {
    private final int size;
    private final XmSurfaceImpl[] surfaces;

    private final XmSurface lamp;
    
    XmSurfaceListImpl(XmSurfaceImpl[] surfaces) {
        this.surfaces = surfaces;
        this.size = surfaces.length;
        XmSurface lamp = null;
        for (XmSurfaceImpl surface : surfaces) {
            if (surface.isLamp()) lamp = surface;
        }
        this.lamp = lamp;
    }

    @Override
    public XmSurface get(int index) {
        return surfaces[index];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public XmSurface lamp() {
        return lamp;
    }
}
