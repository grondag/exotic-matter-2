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

package grondag.xm.surface;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@Internal
class XmSurfaceListImpl implements XmSurfaceList {
	private final int size;
	private final XmSurfaceImpl[] surfaces;

	private final XmSurface lamp;

	XmSurfaceListImpl(XmSurfaceImpl[] surfaces) {
		this.surfaces = surfaces;
		size = surfaces.length;
		XmSurface lamp = null;

		for (final XmSurfaceImpl surface : surfaces) {
			if (surface.isLamp()) {
				lamp = surface;
			}
		}

		this.lamp = lamp;
	}

	@Override
	public XmSurface get(int index) {
		return surfaces[index];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public XmSurface lamp() {
		return lamp;
	}
}
