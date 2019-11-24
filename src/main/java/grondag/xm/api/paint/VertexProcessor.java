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
package grondag.xm.api.paint;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.paint.VertexProcessorRegistryImpl;

/**
 * Logic to apply color, brightness, glow and other attributes that depend on
 * quad, surface, or model state to each vertex in the quad. Applied after UV
 * coordinates have been assigned.
 * <p>
 *
 * While intended to assign color values, could also be used to transform UV,
 * normal or other vertex attributes.
 */
@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface VertexProcessor {
	@SuppressWarnings("rawtypes")
	void process(MutablePolygon result, BaseModelState modelState, XmSurface surface, XmPaint paint, int textureIndex);

	VertexProcessor DEFAULT_VERTEX_PROCESSOR = VertexProcessorRegistryImpl.DEFAULT_VERTEX_PROCESSOR;
}
