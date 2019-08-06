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

import javax.annotation.Nullable;

import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.model.varia.BlockOrientationType;
import grondag.xm.terrain.TerrainState;
import net.minecraft.util.math.Direction;

public interface ModelPrimitiveState {
    ModelPrimitive primitive();

    default boolean hasAxisOrientation() {
        return primitive().hasAxisOrientation(this);
    }

    default boolean hasAxisRotation() {
        return primitive().hasAxisRotation(this);
    }

    default boolean hasAxis() {
        return primitive().hasAxis(this);
    }

    default BlockOrientationType orientationType() {
        return primitive().orientationType((ModelState) this);
    }

    default boolean isAxisOrthogonalToPlacementFace() {
        return primitive().isAxisOrthogonalToPlacementFace();
    }

    /**
     * Returns a copy of this model state with only the bits that matter for
     * geometry. Used as lookup key for block damage models.
     */
    default ModelState geometricState() {
        return primitive().geometricState((ModelState) this);
    }

    default Direction.Axis getAxis() {
        return Direction.Axis.Y;
    }

    default boolean isAxisInverted() {
        return false;
    }

    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    default ClockwiseRotation getAxisRotation() {
        return ClockwiseRotation.ROTATE_NONE;
    }

    default @Nullable TerrainState getTerrainState() {
        return null;
    };

    default long getTerrainStateKey() {
        return 0;
    }

    default int getTerrainHotness() {
        return 0;
    }

    default int primitiveBits() {
        return 0;
    }
}
