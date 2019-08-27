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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.MutablePrimitiveModelState;
import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

@API(status = EXPERIMENTAL)
public interface ModelPrimitive<R extends PrimitiveModelState<R, W>, W extends MutablePrimitiveModelState<R,W>> {
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
    default OrientationType orientationType(R modelState) {
        return OrientationType.NONE;
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
    
    boolean doesShapeMatch(R from, R to);

    default boolean isMultiBlock() {
        return false;
    }
    
    default void invalidateCache() { }
}
