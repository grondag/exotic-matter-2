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

package grondag.xm.api.mesh;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import grondag.xm.api.mesh.polygon.Polygon;

@Experimental
public interface XmMesh {
	boolean isEmpty();

	/**
	 * Reference to poly at current read address.<br>
	 * When stream first created will point to the first poly in the stream.<br>
	 * Returns null if at end or first poly has not been written.<br>
	 *
	 * <p>THIS READ IS NOT THREAD-SAFE and should only be used the allocating thread.
	 */
	Polygon reader();

	/**
	 * Moves reader and returns it.
	 */
	Polygon reader(int address);

	/**
	 * Claims a reader appropriate for concurrent access. Will fail for streams that
	 * are mutable.
	 *
	 * <p>Reader must be released after use or stream can never be recycled to the pool
	 * when released.
	 */
	// FIXME: make these autoclosable and use try with resources when acquired
	default Polygon threadSafeReader() {
		throw new UnsupportedOperationException();
	}

	/** Always returns null as convenience. */
	@Nullable <T> T release();

	/**
	 * Virtual read-only reference to an existing poly in this stream. Use for
	 * interpolation and other poly-poly operations. Does not affect and not
	 * affected by read/write/update cursors.
	 *
	 * <p>DO NOT STORE A REFERENCE. Meant only for use in a local operation. Calls to
	 * {@link #movePolyA(int)} will produce new values.
	 */
	Polygon polyA();

	/**
	 * Sets address for {@link #polyA()} and returns same as convenience.
	 */
	Polygon polyA(int address);

	/**
	 * Secondary instance of {@link #polyA()}. For interpolation.
	 */
	Polygon polyB();

	/**
	 * Secondary instance of {@link #polyA(int)}. For interpolation.
	 */
	Polygon polyB(int address);

	/** Moves reader! */
	default void forEach(Consumer<Polygon> target) {
		final Polygon reader = reader();

		if (reader.origin()) {
			do {
				target.accept(reader);
			} while (reader.next());
		}
	}

	XmMesh EMPTY = XmMeshes.claimWritable().releaseToReader();
}
