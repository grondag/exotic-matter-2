/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.primitive.surface;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.client.resources.language.I18n;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import grondag.xm.api.paint.SurfaceTopology;

// WIP: Fabric deps

@Experimental
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
	 *
	 * <p>If the surface is full-brightness, need to re-color all vertices to white.
	 */
	int FLAG_LAMP_GRADIENT = 4;

	/**
	 * If set, generator will assume surface is emissive. Interacts with
	 * {@link #FLAG_LAMP_GRADIENT}.
	 */
	int FLAG_LAMP = 8;

	int ordinal();

	String nameKey();

	@Environment(EnvType.CLIENT)
	default String name() {
		return I18n.get(nameKey());
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

	default boolean isLamp() {
		return (flags() & FLAG_LAMP) == FLAG_LAMP;
	}
}
