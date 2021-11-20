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
