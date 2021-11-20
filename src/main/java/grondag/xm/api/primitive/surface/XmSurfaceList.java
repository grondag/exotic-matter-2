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
