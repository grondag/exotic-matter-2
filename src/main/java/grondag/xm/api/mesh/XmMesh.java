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
package grondag.xm.api.mesh;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.polygon.Polygon;

@API(status = EXPERIMENTAL)
public interface XmMesh {
	boolean isEmpty();

	/**
	 * Reference to poly at current read address.<br>
	 * When stream first created will point to the first poly in the stream.<br>
	 * Returns null if at end or first poly has not been written.<br>
	 *
	 * THIS READ IS NOT THREAD-SAFE and should only be used the allocating thread.
	 */
	Polygon reader();

	/**
	 * Moves reader and returns it.
	 */
	Polygon reader(int address);

	/**
	 * Claims a reader appropriate for concurrent access. Will fail for streams that
	 * are mutable.
	 * <p>
	 *
	 * Reader must be released after use or stream can never be recycled to the pool
	 * when released.
	 */
	default Polygon threadSafeReader() {
		throw new UnsupportedOperationException();
	}

	/** always returns null as convenience */
	@Nullable <T> T release();

	/**
	 * Virtual read-only reference to an existing poly in this stream. Use for
	 * interpolation and other poly-poly operations. Does not affect and not
	 * affected by read/write/update cursors.
	 * <p>
	 *
	 * DO NOT STORE A REFERENCE. Meant only for use in a local operation. Calls to
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
