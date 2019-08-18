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

package grondag.xm.api.primitive.simple;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.xm.Xm;
import grondag.xm.api.mesh.MeshHelper;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.PolyStream;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.painting.SurfaceTopology;
import net.minecraft.util.math.Vec3d;

public class Sphere extends AbstractSimplePrimitive {
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("back", SurfaceTopology.TILED, XmSurface.FLAG_NONE).build();

    public static final XmSurface SURFACE_ALL = SURFACES.get(0);

    private PolyStream cachedQuads = null;

    public static final Sphere INSTANCE = new Sphere(Xm.idString("sphere"));

    protected Sphere(String idString) {
        super(idString, STATE_FLAG_NONE, SimpleModelStateImpl.FACTORY, s -> SURFACES);
        this.cachedQuads = generateQuads();
    }

    // mainly for run-time testing
    @Override
    public void invalidateCache() { 
        cachedQuads = generateQuads();
    }
    
    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        PolyStream cachedQuads = this.cachedQuads;
        if(cachedQuads == null) {
            cachedQuads = generateQuads();
            this.cachedQuads = cachedQuads;

        }
        if (cachedQuads.isEmpty())
            return;

        cachedQuads.origin();
        final Polygon reader = cachedQuads.reader();

        do
            target.accept(reader);
        while (cachedQuads.next());
    }

    private PolyStream generateQuads() {
        WritablePolyStream stream = PolyStreams.claimWritable();
        stream.writer().lockUV(0, false);
        stream.writer().surface(SURFACE_ALL);
        stream.saveDefaults();

        MeshHelper.makeIcosahedron(new Vec3d(.5, .5, .5), 0.6, stream, false);
        return stream.releaseToReader();
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
