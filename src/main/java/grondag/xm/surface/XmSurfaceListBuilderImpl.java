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

import grondag.fermion.sc.unordered.SimpleUnorderedArrayList;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.primitive.surface.XmSurfaceListBuilder;

public class XmSurfaceListBuilderImpl implements XmSurfaceListBuilder {
    private final SimpleUnorderedArrayList<XmSurfaceImpl> surfaces = new SimpleUnorderedArrayList<>();

    XmSurfaceListBuilderImpl() {
    }

    @Override
    public XmSurfaceListBuilder add(String nameKey, SurfaceTopology topology, int flags) {
        surfaces.add(new XmSurfaceImpl(surfaces.size(), nameKey, topology, flags));
        return this;
    }

    @Override
    public XmSurfaceList build() {
        final int size = surfaces.size();
        XmSurfaceImpl[] output = new XmSurfaceImpl[size];
        for (int i = 0; i < size; i++) {
            output[i] = surfaces.get(i);
        }
        surfaces.clear();
        return new XmSurfaceListImpl(output);
    }
    
    public static XmSurfaceListBuilder builder() {
        return new XmSurfaceListBuilderImpl();
    }
}
