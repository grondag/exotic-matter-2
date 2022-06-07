/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.api.paint;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.paint.VertexProcessorDefault;

/**
 * Logic to apply color, brightness, glow and other attributes that depend on
 * quad, surface, or model state to each vertex in the quad. Applied after UV
 * coordinates have been assigned.
 *
 * <p>While intended to assign color values, could also be used to transform UV,
 * normal or other vertex attributes.
 */
@Experimental
@FunctionalInterface
public interface VertexProcessor {
	@SuppressWarnings("rawtypes")
	void process(MutablePolygon result, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex);

	VertexProcessor DEFAULT_VERTEX_PROCESSOR = VertexProcessorDefault.INSTANCE;
}
