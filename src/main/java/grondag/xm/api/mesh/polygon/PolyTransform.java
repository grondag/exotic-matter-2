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
package grondag.xm.api.mesh.polygon;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.orientation.CubeRotation;
import grondag.xm.mesh.helper.PolyTransformImpl;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface PolyTransform extends Consumer<MutablePolygon> {
    @Override
    void accept(MutablePolygon poly);

    /**
     * Find appropriate transformation assuming base model is oriented with as follows:
     * Axis = Y with positive orientation if orientation applies.<p>
     *
     * For the default rotation, generally, {@code DOWN} is considered the "bottom"
     * and {@code SOUTH} is the "back" when facing the "front" of the primitive.<p>
     *
     * For primitives oriented to a corner, the default corner is "bottom, right, back"
     * in the frame just described, or {@code DOWN}, {@code SOUTH}, {@code EAST} in terms
     * of specific faces.
     */
    @SuppressWarnings("rawtypes")
    static PolyTransform get(BaseModelState modelState) {
        return PolyTransformImpl.get(modelState);
    }

    static PolyTransform forEdgeRotation(int ordinal) {
        return PolyTransformImpl.forEdgeRotation(ordinal);
    }

    static PolyTransform get(CubeRotation corner) {
        return PolyTransformImpl.get(corner);
    }

    static PolyTransform get(Axis axis) {
        return PolyTransformImpl.get(axis);
    }

    static PolyTransform get(Direction face) {
        return PolyTransformImpl.get(face);
    }
}
