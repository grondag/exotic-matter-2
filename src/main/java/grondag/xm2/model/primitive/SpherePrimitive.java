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

import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.xm2.api.model.ModelState;
import grondag.xm2.api.surface.XmSurface;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.mesh.stream.IPolyStream;
import grondag.xm2.mesh.stream.IWritablePolyStream;
import grondag.xm2.mesh.stream.PolyStreams;
import grondag.xm2.model.state.StateFormat;
import grondag.xm2.model.varia.MeshHelper;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.surface.XmSurfaceImpl;
import grondag.xm2.surface.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Vec3d;

public class SpherePrimitive extends AbstractModelPrimitive {
    public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
            .add("back", SurfaceTopology.TILED, XmSurface.FLAG_NONE).build();

    public static final XmSurfaceImpl SURFACE_ALL = SURFACES.get(0);

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public SpherePrimitive(String idString) {
        super(idString, SURFACES, StateFormat.BLOCK, STATE_FLAG_NONE);
        this.cachedQuads = generateQuads();
    }

    @Override
    public void produceQuads(ModelState modelState, Consumer<IPolygon> target) {
        if (cachedQuads.isEmpty())
            return;

        cachedQuads.origin();
        IPolygon reader = cachedQuads.reader();

        do
            target.accept(reader);
        while (cachedQuads.next());
    }

    private IPolyStream generateQuads() {
        IWritablePolyStream stream = PolyStreams.claimWritable();
        stream.writer().setLockUV(0, false);
        stream.writer().surface(SURFACE_ALL);
        stream.saveDefaults();

        MeshHelper.makeIcosahedron(new Vec3d(.5, .5, .5), 0.6, stream, false);
        return stream.releaseAndConvertToReader();
    }
}
