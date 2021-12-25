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

import io.vram.sc.unordered.SimpleUnorderedArrayList;

import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.primitive.surface.XmSurfaceListBuilder;

@Internal
public class XmSurfaceListBuilderImpl implements XmSurfaceListBuilder {
	private final SimpleUnorderedArrayList<XmSurfaceImpl> surfaces = new SimpleUnorderedArrayList<>();

	XmSurfaceListBuilderImpl() {
	}

	public XmSurfaceListBuilderImpl(XmSurfaceListImpl baseList) {
		for (int i = 0; i < baseList.size(); ++i) {
			surfaces.add((XmSurfaceImpl) baseList.get(i));
		}
	}

	@Override
	public XmSurfaceListBuilder add(String nameKey, SurfaceTopology topology, int flags) {
		surfaces.add(new XmSurfaceImpl(surfaces.size(), nameKey, topology, flags));
		return this;
	}

	@Override
	public XmSurfaceList build() {
		final int size = surfaces.size();
		final XmSurfaceImpl[] output = new XmSurfaceImpl[size];

		for (int i = 0; i < size; i++) {
			output[i] = surfaces.get(i);
		}

		surfaces.clear();
		return new XmSurfaceListImpl(output);
	}

	public static XmSurfaceListBuilder builder() {
		return new XmSurfaceListBuilderImpl();
	}

	public static XmSurfaceListBuilder builder(XmSurfaceList baseList) {
		return new XmSurfaceListBuilderImpl((XmSurfaceListImpl) baseList);
	}
}
