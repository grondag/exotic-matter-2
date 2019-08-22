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
package grondag.xm.api.primitive.base;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SPECIES;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.orientation.CubeRotation;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.modelstate.SimpleModelStateImpl;

@API(status = EXPERIMENTAL)
public abstract class AbstractWedge extends AbstractSimplePrimitive {
    protected static final int KEY_COUNT = CubeRotation.COUNT * 3;
    
    protected static int computeKey(int edgeIndex, boolean isCorner, boolean isInside) {
        return edgeIndex * 3 + (isCorner ? (isInside ? 1 : 2) : 0);
    }

    protected final ReadOnlyMesh[] CACHE = new ReadOnlyMesh[KEY_COUNT];
    
    public AbstractWedge(String idString, Function<SimpleModelState, XmSurfaceList> surfaceFunc) {
        super(idString, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ROTATION | STATE_FLAG_HAS_AXIS_ORIENTATION,
                SimpleModelStateImpl.FACTORY, surfaceFunc);
    }
    
    // mainly for run-time testing
    @Override
    public void invalidateCache() { 
        Arrays.fill(CACHE, null);
    }

    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        final int edgeIndex = modelState.orientationIndex();
        final boolean isCorner = isCorner(modelState);
        final boolean isInside = isInsideCorner(modelState);
        final int key = computeKey(edgeIndex, isCorner, isInside);
        
        ReadOnlyMesh mesh = CACHE[key];
        if(mesh == null) {
            mesh = buildPolyStream(edgeIndex, isCorner, isInside);
            CACHE[key] = mesh;
        }
        
        final Polygon reader = mesh.threadSafeReader();
        if (reader.origin()) {

            do {
                target.accept(reader);
            } while (reader.next());
            
        }
        reader.release();
    }

    protected abstract ReadOnlyMesh buildPolyStream(int edgeIndex, boolean isCorner, boolean isInside);
    
    @Override
    public OrientationType orientationType(SimpleModelState modelState) {
        return OrientationType.ROTATION;
    }

    @Override
    public SimpleModelState.Mutable geometricState(SimpleModelState fromState) {
        SimpleModelState.Mutable result = this.newState();
        if(fromState.primitive().orientationType(fromState) == OrientationType.ROTATION) {
            result.orientationIndex(fromState.orientationIndex());
            result.primitiveBits(fromState.primitiveBits());
        }
        return result;
    }

    @Override
    public boolean doesShapeMatch(SimpleModelState from, SimpleModelState to) {
        return from.primitive() == to.primitive()
                && from.orientationIndex() == to.orientationIndex()
                && from.primitiveBits() == to.primitiveBits();
    }

    private static final int CORNER_FLAG = 1;
    private static final int INSIDE_FLAG = 2;
    
    public static boolean isCorner(SimpleModelState modelState) {
        return (modelState.primitiveBits() & CORNER_FLAG) == CORNER_FLAG;
    }

    public static void setCorner(boolean isCorner, SimpleModelState.Mutable modelState) {
        final int oldBits = modelState.primitiveBits();
        modelState.primitiveBits(isCorner ? oldBits | CORNER_FLAG : oldBits & ~CORNER_FLAG);
    }
    
    /** 
     * Only applies when {@link #isCorner(SimpleModelState)} == (@code true}.
     * @param modelState  State of this primitive.
     * @return {@code true} when inside corner, {@code false} when outside corner.
     */
    public static boolean isInsideCorner(SimpleModelState modelState) {
        return (modelState.primitiveBits() & INSIDE_FLAG) == INSIDE_FLAG;
    }

    /**
     * See {@link #isInsideCorner(SimpleModelState)}
     * @param isCorner
     * @param modelState
     */
    public static void setInsideCorner(boolean isCorner, SimpleModelState.Mutable modelState) {
        final int oldBits = modelState.primitiveBits();
        modelState.primitiveBits(isCorner ? oldBits | INSIDE_FLAG : oldBits & ~INSIDE_FLAG);
    }
}
