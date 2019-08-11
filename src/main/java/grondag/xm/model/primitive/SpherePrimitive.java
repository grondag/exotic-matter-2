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

package grondag.xm.model.primitive;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.polygon.IPolygon;
import grondag.xm.mesh.stream.IPolyStream;
import grondag.xm.mesh.stream.IWritablePolyStream;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.model.varia.MeshHelper;
import grondag.xm.painting.SurfaceTopology;
import grondag.xm.surface.XmSurfaceImpl;
import grondag.xm.surface.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Vec3d;

public class SpherePrimitive extends AbstractBasePrimitive {
    public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder().add("back", SurfaceTopology.TILED, XmSurface.FLAG_NONE).build();

    public static final XmSurfaceImpl SURFACE_ALL = SURFACES.get(0);

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public SpherePrimitive(String idString) {
        super(idString, STATE_FLAG_NONE, SimpleModelStateImpl.FACTORY);
        this.cachedQuads = generateQuads();
    }

    @Override
    public XmSurfaceListImpl surfaces(SimpleModelState modelState) {
        return SURFACES;
    }

    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<IPolygon> target) {
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

    @Override
    public SimpleModelState.Mutable geometricState(SimpleModelState fromState) {
        return defaultState().mutableCopy();
    }

    @Override
    public boolean doesShapeMatch(SimpleModelState from, SimpleModelState to) {
        return from.primitive() == to.primitive();
    }
}
