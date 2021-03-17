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
package grondag.xm.mesh;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.fermion.intstream.IntStreams;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.polygon.Polygon;

@Internal
class ReadOnlyMeshImpl extends AbstractXmMesh implements ReadOnlyMesh {
	void load(WritableMeshImpl streamIn, int formatFlags) {
		prepare(IntStreams.claim(streamIn.stream.capacity()));

		if (!streamIn.isEmpty()) {
			final Polygon reader = streamIn.reader();
			reader.origin();
			do {
				appendCopy(reader, formatFlags);
			} while (reader.next());
		}

		stream.compact();
	}

	@Override
	protected void returnToPool() {
		XmMeshesImpl.release(this);
	}

	@Override
	public Polygon threadSafeReader() {
		return threadSafeReaderImpl();
	}
}
