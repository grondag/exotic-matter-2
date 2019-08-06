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

import grondag.xm.api.allocation.Reference;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.surface.XmSurfaceList;
import grondag.xm.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm.mesh.helper.PolyTransform;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface MutableModelState extends ModelState, MutableModelPrimitiveState, MutableModelWorldState, Reference.Mutable {
    /**
     * Copies what it can, excluding the primitive, and returns self.
     */
    MutableModelState copyFrom(ModelState template);

    void setStatic(boolean isStatic);

    boolean equalsIncludeStatic(Object obj);

    /** returns self as convenience method */
    void refreshFromWorld(XmBlockStateImpl state, BlockView world, BlockPos pos);

    default void paint(XmSurface surface, XmPaint paint) {
        paint(surface.ordinal(), paint.index());
    }

    default void paint(XmSurface surface, int paintIndex) {
        paint(surface.ordinal(), paintIndex);
    }

    void paint(int surfaceIndex, int paintIndex);

    default void paintAll(XmPaint paint) {
        paintAll(paint.index());
    }

    default void paintAll(int paintIndex) {
        XmSurfaceList slist = primitive().surfaces(this);
        final int limit = slist.size();
        for (int i = 0; i < limit; i++) {
            paint(i, paintIndex);
        }
    }

    /**
     * See {@link PolyTransform#rotateFace(MutableModelState, Direction)}
     */
    Direction rotateFace(Direction face);
}
