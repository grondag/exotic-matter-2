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

package grondag.xm.api.primitive.surface;

import grondag.xm.util.SimpleEnumCodec;

public enum SurfaceLocation {
	/** Part of outside surface. */
	OUTSIDE,

	/** Part of inside surface for inlays, insets, etc. */
	INSIDE,

	/** Part of cut surfaces between outside and inside surfaces.  */
	CUT,

	/** Faces parallel to axis. */
	SIDES,

	/** Faces orthogonal to axis. */
	ENDS,

	/** Top surface if present and potentially different from other sides. */
	TOP,

	/** Bottom surface if present and potentially different from other sides. */
	BOTTOM,

	/** Left surface if present and potentially different from other sides. */
	LEFT,

	/** Top surface if present and potentially different from other sides. */
	RIGHT,

	/** Top surface if present and potentially different from other sides. */
	FRONT,

	/** Top surface if present and potentially different from other sides. */
	BACK;

	public static final SimpleEnumCodec<SurfaceLocation> CODEC = new SimpleEnumCodec<>(SurfaceLocation.class);
	public static final int COUNT = CODEC.count;
}
