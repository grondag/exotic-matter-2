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

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;

/**
 * Stream that allows appending to end in wip area but is immutable for polygons
 * already created.
 */
@Experimental
public interface WritableMesh extends XmMesh {
	/**
	 * Holds transient poly data that will be appended by next call to {@link #append()}.
	 * Is reset to defaults when append is called.
	 *
	 * <p>DO NOT HOLD A NON-LOCAL REFERENCE TO THIS.
	 */
	MutablePolygon writer();

	/**
	 * Address that will be used for next appended polygon when append is
	 * called.<br>
	 * Cannot be used with move... methods until writer is appended.
	 */
	int writerAddress();

	/**
	 * Releases this stream and returns an immutable reader stream. The reader strip
	 * will use non-pooled heap memory and thus should only be used for streams with
	 * a significant lifetime to prevent needless garbage collection.
	 *
	 * <p>The reader stream will not include deleted polygons, and will only include
	 * tag, link or bounds metadata if those flags are specified.
	 */
	ReadOnlyMesh releaseToReader();

	/** Creates a read-only copy without releasing allocation. */
	ReadOnlyMesh toReader();

	/**
	 * Makes no change to writer state, except address.
	 */
	void appendCopy(Polygon poly);

	default void appendAll(XmMesh stream) {
		final Polygon reader = stream.reader();

		if (reader.origin()) {
			do {
				assert !reader.isDeleted();
				appendCopy(reader);
			} while (reader.next());
		}
	}

	void clear();

	void splitAsNeeded();
}
