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

import grondag.fermion.intstream.IntStream;
import grondag.fermion.intstream.IntStreams;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;

@Internal
class WritableMeshImpl extends AbstractXmMesh implements WritableMesh {
	private static final int MAX_STRIDE;

	static {
		final int maxFormat = MeshFormat.MUTABLE_FLAG | MeshFormat.HAS_LINK_FLAG | MeshFormat.HAS_TAG_FLAG;

		MAX_STRIDE = 1 + StaticEncoder.INTEGER_WIDTH + PolyEncoder.get(maxFormat).stride();
	}

	protected final StreamBackedMutablePolygon writer;
	protected final StreamBackedPolygon copyFrom = new StreamBackedPolygon();

	protected IntStream writerStream;
	protected IntStream defaultStream;

	/**
	 * Format flags used for the writer polygon. Always includes mutable flag. Also
	 * the main stream format for mutable subclass.
	 */
	protected int formatFlags = 0;

	WritableMeshImpl() {
		super();
		writer = new StreamBackedMutablePolygon();
		// note this never changes - writer stream is dedicated,
		// and we already write at the start of it
		writer.baseAddress = 0;
		writer.mesh = this;
		copyFrom.mesh = this;
	}

	@Override
	public void clear() {
		stream.clear();
		originAddress = newOrigin();
		writeAddress = originAddress;
		// force error on read
		reader.invalidate();
		polyA.invalidate();
		polyB.invalidate();
		internal.invalidate();
		clearDefaults();
		loadDefaults();
	}

	protected final void prepare(IntStream stream, int formatFlags) {
		super.prepare(stream);
		copyFrom.stream = stream;
		defaultStream = IntStreams.claim();
		writerStream = IntStreams.claim();
		writer.stream = writerStream;
		this.formatFlags = formatFlags | MeshFormat.MUTABLE_FLAG;
		clearDefaults();
		loadDefaults();
	}

	protected void prepare(int formatFlags) {
		prepare(IntStreams.claim(), formatFlags);
	}

	@Override
	protected final void prepare(IntStream stream) {
		prepare(stream, 0);
	}

	@Override
	protected void doRelease() {
		super.doRelease();
		copyFrom.stream = null;
		defaultStream.release();
		writerStream.release();
		defaultStream = null;
		writerStream = null;
	}

	@Override
	protected void returnToPool() {
		XmMeshesImpl.release(this);
	}

	@Override
	public MutablePolygon writer() {
		return writer;
	}

	@Override
	public int writerAddress() {
		return writeAddress;
	}

	final void append() {
		appendCopy(writer, formatFlags);
		loadDefaults();
	}

	void saveDefaults() {
		writer.clearFaceNormal();
		defaultStream.clear();
		defaultStream.copyFrom(0, writerStream, 0, MeshFormat.polyStride(writer.format(), false));
	}

	void clearDefaults() {
		defaultStream.clear();
		defaultStream.set(0, MeshFormat.setVertexCount(formatFlags, 4));
		writer.stream = defaultStream;
		writer.loadFormat();

		writer.loadStandardDefaults();

		writer.stream = writerStream;
		writer.loadFormat();
	}

	void loadDefaults() {
		writerStream.clear();
		writerStream.copyFrom(0, defaultStream, 0, MAX_STRIDE);
		writer.loadFormat();
	}

	@Override
	public ReadOnlyMesh releaseToReader() {
		final ReadOnlyMesh result = toReader();
		release();
		return result;
	}

	@Override
	public ReadOnlyMesh toReader() {
		return XmMeshesImpl.claimReadOnly(this, 0);
	}

	@Override
	public void appendCopy(Polygon poly) {
		appendCopy(poly, formatFlags);
	}

	@Override
	public void splitAsNeeded() {
		final Polygon reader = reader();

		if (reader.origin()) {
			final int limit = writerAddress();

			do {
				final int readAddress = reader.address();
				splitIfNeeded(readAddress);

				if (reader.address() != readAddress) {
					reader.moveTo(readAddress);
				}
			} while (reader.next() && reader.address() < limit);
		}
	}

	int splitIfNeeded(int targetAddress) {
		internal.moveTo(targetAddress);
		final int inCount = internal.vertexCount();

		if (inCount == 3 || (inCount == 4 && internal.isConvex())) {
			return Polygon.NO_LINK_OR_TAG;
		}

		final int firstSplitAddress = writerAddress();

		int head = inCount - 1;
		int tail = 2;
		writer
			.copyFrom(internal, false)
			.vertexCount(4)
			.copyVertexFrom(0, internal, head)
			.copyVertexFrom(1, internal, 0)
			.copyVertexFrom(2, internal, 1)
			.copyVertexFrom(3, internal, tail)
			.append();

		while (head - tail > 1) {
			final int vCount = head - tail == 2 ? 3 : 4;
			internal.moveTo(targetAddress);
			writer
				.copyFrom(internal, false)
				.vertexCount(vCount)
				.copyVertexFrom(0, internal, head)
				.copyVertexFrom(1, internal, tail)
				.copyVertexFrom(2, internal, ++tail);

			if (vCount == 4) {
				writer.copyVertexFrom(3, internal, --head);

				// if we end up with a convex quad
				// backtrack and output a tri instead
				if (!writer.isConvex()) {
					head++;
					tail--;
					writer
						.copyFrom(internal, false)
						.vertexCount(3)
						.copyVertexFrom(0, internal, head)
						.copyVertexFrom(1, internal, tail)
						.copyVertexFrom(2, internal, ++tail);
				}
			}

			append();
		}

		reader(targetAddress).delete();
		return firstSplitAddress;
	}
}
