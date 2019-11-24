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

import org.apiguardian.api.API;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;

/**
 * Stream that allows appending to end in wip area but is immutable for polygons
 * already created.
 */
@API(status = EXPERIMENTAL)
public interface WritableMesh extends XmMesh {

	/**
	 * Holds WIP poly data that will be appended by next call to {@link #append()}.
	 * Is reset to defaults when append is called.
	 * <p>
	 *
	 * DO NOT HOLD A NON-LOCAL REFERENCE TO THIS.
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
	 * <p>
	 *
	 * The reader stream will not include deleted polygons, and will only include
	 * tag, link or bounds metadata if those flags are specified.
	 */
	ReadOnlyMesh releaseToReader();

	/** Creates a read-only copy without releasing allocation */
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
