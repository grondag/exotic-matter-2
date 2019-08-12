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

package grondag.xm.api.primitive;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ROTATION;

import java.util.function.Consumer;

import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.surface.XmSurfaceList;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.model.varia.BlockOrientationType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public interface ModelPrimitive<R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> {
    /**
     * Used for registration and serialization of model state.
     */
    Identifier id();

    /**
     * Used for fast, transient serialization. Recommended that implementations
     * override this and cache value to avoid map lookups.
     */
    default int index() {
        return ModelPrimitiveRegistry.INSTANCE.indexOf(this);
    }

    /**
     * This convention is used by XM2 but 3rd-party primitives can use a different
     * one.
     */
    default String translationKey() {
        return "xm2_primitive_name." + id().getNamespace() + "." + id().getPath();
    }

    XmSurfaceList surfaces(R modelState);

    default XmSurface lampSurface(R modelState) {
        return null;
    }
    
    /**
     * Override if shape has an orientation to be selected during placement.
     */
    default BlockOrientationType orientationType(R modelState) {
        return BlockOrientationType.NONE;
    }

    int stateFlags(R modelState);

    /**
     * Output polygons must be quads or tris. Consumer MUST NOT hold references to
     * any of the polys received.
     */
    void produceQuads(R modelState, Consumer<Polygon> target);

    R defaultState();

    W geometricState(R fromState);

    default W newState() {
        return defaultState().mutableCopy();
    }

    W fromBuffer(PacketByteBuf buf);
    

    W fromTag(CompoundTag tag);
    
    /**
     * If true, shape can be placed on itself to become bigger.
     */
    default boolean isAdditive() {
        return false;
    }

    /**
     * Override to true for blocks like stairs and wedges. CubicPlacementHandler
     * will know they need to be placed in a corner instead of a face.
     */
    default boolean isAxisOrthogonalToPlacementFace() {
        return false;
    }

    default boolean hasAxis(R modelState) {
        return (stateFlags(modelState) & STATE_FLAG_HAS_AXIS) == STATE_FLAG_HAS_AXIS;
    }

    default boolean hasAxisOrientation(R modelState) {
        return (stateFlags(modelState) & STATE_FLAG_HAS_AXIS_ORIENTATION) == STATE_FLAG_HAS_AXIS_ORIENTATION;
    }

    default boolean hasAxisRotation(R modelState) {
        return (stateFlags(modelState) & STATE_FLAG_HAS_AXIS_ROTATION) == STATE_FLAG_HAS_AXIS_ROTATION;
    }

    boolean doesShapeMatch(R from, R to);

    default boolean isMultiBlock() {
        return false;
    }
    
    default void invalidateCache() { }
}
