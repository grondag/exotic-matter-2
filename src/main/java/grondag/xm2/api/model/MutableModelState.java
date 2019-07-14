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

package grondag.xm2.api.model;

import grondag.fermion.serialization.IReadWriteNBT;
import grondag.fermion.serialization.PacketSerializable;
import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.api.surface.XmSurface;
import grondag.xm2.api.surface.XmSurfaceList;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.mesh.helper.PolyTransform;
import grondag.xm2.terrain.TerrainState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface MutableModelState extends ModelState, MutableModelPrimitiveState, MutableModelWorldState, IReadWriteNBT, PacketSerializable {
    int[] serializeToInts();

    void setStatic(boolean isStatic);

    boolean equalsIncludeStatic(Object obj);

    /** returns self as convenience method */
    MutableModelState refreshFromWorld(XmBlockStateImpl state, BlockView world, BlockPos pos);

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
        XmSurfaceList slist = primitive().surfaces();
        final int limit = slist.size();
        for (int i = 0; i < limit; i++) {
            paint(i, paintIndex);
        }
    }
    
    void setAxis(Direction.Axis axis);

    void setAxisInverted(boolean isInverted);

    /** usage is determined by shape */
    void setStaticShapeBits(long bits);

    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    void setAxisRotation(ClockwiseRotation rotation);

    /**
     * Multiblock shapes also get a full 64 bits of information - does not update
     * from world
     */
    void setMultiBlockBits(long bits);

    void setTerrainState(TerrainState flowState);

    void setTerrainStateKey(long terrainStateKey);

    /**
     * See {@link PolyTransform#rotateFace(MutableModelState, Direction)}
     */
    Direction rotateFace(Direction face);
}
