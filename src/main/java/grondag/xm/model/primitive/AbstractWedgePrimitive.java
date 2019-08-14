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

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SPECIES;

import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.model.varia.BlockOrientationType;

public abstract class AbstractWedgePrimitive extends AbstractBasePrimitive {
    public AbstractWedgePrimitive(String idString) {
        super(idString, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ROTATION | STATE_FLAG_HAS_AXIS_ORIENTATION,
                SimpleModelStateImpl.FACTORY);
    }

    @Override
    public BlockOrientationType orientationType(SimpleModelState modelState) {
        return BlockOrientationType.EDGE_SIDED;
    }

    @Override
    public SimpleModelState.Mutable geometricState(SimpleModelState fromState) {
        SimpleModelState.Mutable result = this.newState();
        if(fromState.primitive().orientationType(fromState) == BlockOrientationType.EDGE_SIDED) {
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
