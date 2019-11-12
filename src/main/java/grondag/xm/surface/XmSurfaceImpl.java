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

import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurface;

@API(status = INTERNAL)
class XmSurfaceImpl implements XmSurface {
    final int ordinal;
    final String nameKey;
    final SurfaceTopology topology;
    final int flags;

    XmSurfaceImpl(int ordinal, String nameKey, SurfaceTopology topology, int flags) {
        this.ordinal = ordinal;
        this.nameKey = nameKey;
        this.topology = topology;
        this.flags = flags;
    }

    @Override
    public int ordinal() {
        return ordinal;
    }

    @Override
    public String nameKey() {
        return nameKey;
    }

    @Override
    public SurfaceTopology topology() {
        return topology;
    }

    @Override
    public int flags() {
        return flags;
    }
}
