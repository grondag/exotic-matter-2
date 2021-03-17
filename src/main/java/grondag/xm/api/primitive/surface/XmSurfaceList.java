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

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.surface.XmSurfaceListBuilderImpl;

@Experimental
public interface XmSurfaceList {
	static XmSurfaceListBuilder builder() {
		return XmSurfaceListBuilderImpl.builder();
	}

	static XmSurfaceListBuilder builder(XmSurfaceList appendTo) {
		return XmSurfaceListBuilderImpl.builder(appendTo);
	}

	int size();

	XmSurface get(int index);

	@Nullable
	XmSurface lamp();

	XmSurfaceList ALL = builder().add("all", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();
}
