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

package grondag.xm.api.modelstate;

import java.util.function.Function;

import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.surface.XmSurfaceList;
import grondag.xm.mesh.helper.PolyTransform;
import net.minecraft.util.math.Direction;

public interface MutableModelState extends ModelState, MutableModelPrimitiveState, MutableModelWorldState {
    void release();
    
    void retain();
    
    /**
     * Copies what it can, excluding the primitive, and returns self.
     */
    MutableModelState copyFrom(ModelState template);

    MutableModelState setStatic(boolean isStatic);

    boolean equalsIncludeStatic(Object obj);

    default MutableModelState paint(XmSurface surface, XmPaint paint) {
        return paint(surface.ordinal(), paint.index());
    }

    default MutableModelState paint(XmSurface surface, int paintIndex) {
        return paint(surface.ordinal(), paintIndex);
    }

    MutableModelState paint(int surfaceIndex, int paintIndex);

    default MutableModelState paintAll(XmPaint paint) {
        return paintAll(paint.index());
    }

    default MutableModelState paintAll(int paintIndex) {
        XmSurfaceList slist = primitive().surfaces(this);
        final int limit = slist.size();
        for (int i = 0; i < limit; i++) {
            paint(i, paintIndex);
        }
        return this;
    }

    /**
     * See {@link PolyTransform#rotateFace(MutableModelState, Direction)}
     */
    Direction rotateFace(Direction face);

    default <T> T applyAndRelease(Function<MutableModelState, T> func) {
        final T result = func.apply(this);
        this.release();
        return result;
    }

    default ModelState releaseToImmutable() {
        final ModelState result = this.toImmutable();
        this.release();
        return result;
    }
}
