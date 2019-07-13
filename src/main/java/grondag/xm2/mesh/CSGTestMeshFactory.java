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

package grondag.xm2.mesh;

import static grondag.xm2.state.ModelStateData.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.xm2.painting.PaintLayer;
import grondag.xm2.painting.Surface;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.CsgPolyStream;
import grondag.xm2.primitives.stream.IPolyStream;
import grondag.xm2.primitives.stream.IWritablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.StateFormat;
import grondag.xm2.surface.api.XmSurface;
import grondag.xm2.surface.impl.XmSurfaceImpl;
import grondag.xm2.surface.impl.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Box;

public class CSGTestMeshFactory extends MeshFactory {
	public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
			.add("main", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("lamp", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.build();
	
	public static final XmSurfaceImpl SURFACE_A = SURFACES.get(0);
	public static final XmSurfaceImpl SURFACE_B = SURFACES.get(1);
	
    private static final Surface SURFACE_MAIN = Surface.builder(SurfaceTopology.CUBIC)
            .withDisabledLayers(PaintLayer.CUT, PaintLayer.LAMP).build();
    private static final Surface SURFACE_LAMP = Surface.builder(SurfaceTopology.CUBIC)
            .withEnabledLayers(PaintLayer.LAMP).build();

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public CSGTestMeshFactory() {
        super(SURFACES, StateFormat.BLOCK, STATE_FLAG_NONE);
        this.cachedQuads = getTestQuads();
    }

    @Override
    public void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target) {
        if (cachedQuads.origin()) {
            IPolygon reader = cachedQuads.reader();

            do
                target.accept(reader);
            while (cachedQuads.next());
        }
    }

    private IPolyStream getTestQuads() {
        // union opposite overlapping coplanar faces
//      result = new CSGShape(QuadFactory.makeBox(new BoundingBox(0, .4, .5, 1, 1, 1), template));
//      delta = new CSGShape(QuadFactory.makeBox(new BoundingBox(.3, 0, 0, .7, .6, .5), template));
//      result = result.union(delta);

        // union opposite overlapping coplanar faces created by diff
//      result = new CSGShape(QuadFactory.makeBox(new BoundingBox(0.1, 0.1, 0.1, 0.9, 0.9, 0.9), template));
//      delta = new CSGShape(QuadFactory.makeBox(new BoundingBox(0.3, 0.03, 0.5, 0.5, 0.95, 0.7), template));  
//      result = result.difference(delta);
//      delta = new CSGShape(QuadFactory.makeBox(new BoundingBox(0.3, 0, 0, 0.4, .2, 1), template));
//      result = result.union(delta);

        // cylinder/cone test
//      result = new CSGShape(QuadFactory.makeCylinder(new Vec3d(.5, 0, .5), new Vec3d(.5, 1, .5), 0.5, 0, template));

        // icosahedron (sphere) test
//    result = new CSGShape(QuadFactory.makeIcosahedron(new Vec3d(.5, .5, .5), 0.5, template));

        CsgPolyStream quadsA = PolyStreams.claimCSG();
        quadsA.writer().setLockUV(0, true);
        quadsA.writer().setSurface(SURFACE_MAIN);
        quadsA.writer().surface(SURFACE_A);
        quadsA.saveDefaults();
        MeshHelper.makePaintableBox(new Box(0, 0.4, 0.4, 1.0, 0.6, 0.6), quadsA);

        CsgPolyStream quadsB = PolyStreams.claimCSG();
        quadsB.writer().setLockUV(0, true);
        quadsB.writer().setSurface(SURFACE_LAMP);
        quadsB.writer().surface(SURFACE_B);
        quadsB.saveDefaults();
        MeshHelper.makePaintableBox(new Box(0.2, 0, 0.4, 0.6, 1.0, 0.8), quadsB);

        IWritablePolyStream output = PolyStreams.claimWritable();
        CSG.union(quadsA, quadsB, output);

        quadsA.release();
        quadsB.release();

//      IPolyStream result = PolyStreams.claimRecoloredCopy(output);
//      output.release();
//      return result;
        return output.releaseAndConvertToReader();

//      quadsB = new CSGShape(QuadFactory.makeBox(new BoundingBox(0, 0, 0.3, 1, 1, .7), template));
//      result = result.difference(quadsB);

//      template.color = borderColor.getColorMap(EnumColorMap.HIGHLIGHT);
//      quadsB = new CSGShape(QuadFactory.makeBox(new BoundingBox(0.2, 0.2, 0, 0.8, 0.8, 1), template));
//      result = result.difference(quadsB);
//
//      template.color = borderColor.getColorMap(EnumColorMap.HIGHLIGHT);
//      quadsB = new CSGShape(QuadFactory.makeBox(new BoundingBox(0, 0, .4, 1, .4, .65), template));
//      result = result.difference(quadsB);

//      result.recolor();
    }

    @Override
    public boolean isCube(ModelState modelState) {
        return true;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState) {
        return 0;
    }

    @Override
    public boolean hasLampSurface(ModelState modelState) {
        return false;
    }
}
