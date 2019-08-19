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
package grondag.xm.api.primitive.surface;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.paint.SurfaceTopology;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;

@API(status = EXPERIMENTAL)
public interface XmSurface {
    int FLAG_NONE = 0;

    /**
     * If unset, border and masonry painters will not render on this surface. Leave
     * unset for topologies that don't play well with borders.
     */
    int FLAG_ALLOW_BORDERS = 1;

    /**
     * If set, texture painting should not vary by axis orthogonal to the surface.
     * Ignored if {@link #textureSalt} is non-zero.
     */
    int FLAG_IGNORE_DEPTH_FOR_RADOMIZATION = 2;

    /**
     * If set, generator will assign colors to vertexes to indicate proximity to
     * lamp surface. Vertices next to lamp have color WHITE and those away have
     * color BLACK. If the lighting mode for the surface is shaded, then quad bake
     * should color vertices to form a gradient.
     * <p>
     * 
     * If the surface is full-brightness, need to re-color all vertices to white.
     */
    int FLAG_LAMP_GRADIENT = 4;

    /**
     * If set, generator will assume surface is emissive. Interacts with
     * {@link #FLAG_LAMP_GRADIENT}
     * <p>
     */
    int FLAG_LAMP = 8;

    int ordinal();

    String nameKey();

    @Environment(EnvType.CLIENT)
    default String name() {
        return I18n.translate(nameKey());
    }

    SurfaceTopology topology();

    int flags();

    default boolean ignoreDepthForRandomization() {
        return (flags() & FLAG_IGNORE_DEPTH_FOR_RADOMIZATION) == FLAG_IGNORE_DEPTH_FOR_RADOMIZATION;
    }

    default boolean allowBorders() {
        return (flags() & FLAG_ALLOW_BORDERS) == FLAG_ALLOW_BORDERS;
    }

    default boolean isLampGradient() {
        return (flags() & FLAG_LAMP_GRADIENT) == FLAG_LAMP_GRADIENT;
    }
}
