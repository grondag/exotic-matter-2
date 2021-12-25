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

package grondag.xm.mesh;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.mesh.CsgMesh;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;

@Internal
public class XmMeshesImpl {
	static final int FORMAT_TAGS = MeshFormat.HAS_TAG_FLAG;
	static final int FORMAT_LINKS = MeshFormat.HAS_LINK_FLAG;

	private static final ArrayBlockingQueue<WritableMeshImpl> writables = new ArrayBlockingQueue<>(256);
	private static final ArrayBlockingQueue<MutableMeshImpl> mutables = new ArrayBlockingQueue<>(128);
	private static final ArrayBlockingQueue<CsgMeshImpl> csgStreams = new ArrayBlockingQueue<>(128);
	private static final ArrayBlockingQueue<ReadOnlyMeshImpl> readables = new ArrayBlockingQueue<>(256);

	public static WritableMesh claimWritable() {
		return claimWritable(0);
	}

	static WritableMesh claimWritable(int formatFlags) {
		WritableMeshImpl result = writables.poll();

		if (result == null) {
			result = new WritableMeshImpl();
		}

		result.prepare(formatFlags);
		return result;
	}

	static void release(WritableMeshImpl freeStream) {
		writables.offer(freeStream);
	}

	public static MutableMesh claimMutable() {
		return claimMutable(0);
	}

	static MutableMesh claimMutable(int formatFlags) {
		MutableMeshImpl result = mutables.poll();

		if (result == null) {
			result = new MutableMeshImpl();
		}

		result.prepare(formatFlags);
		return result;
	}

	static void release(MutableMeshImpl freeStream) {
		mutables.offer(freeStream);
	}

	static ReadOnlyMesh claimReadOnly(WritableMeshImpl writablePolyStream, int formatFlags) {
		ReadOnlyMeshImpl result = readables.poll();

		if (result == null) {
			result = new ReadOnlyMeshImpl();
		}

		result.load(writablePolyStream, formatFlags);
		return result;
	}

	/**
	 * Creates a stream with randomly recolored copies of the input stream.
	 * Does not modify or release the input stream.
	 */
	public static ReadOnlyMesh claimRecoloredCopy(XmMesh input) {
		final WritableMesh result = claimWritable();
		final Polygon reader = input.reader();

		if (reader.origin()) {
			final Random r = ThreadLocalRandom.current();
			final MutablePolygon writer = result.writer();

			do {
				writer.vertexCount(reader.vertexCount());
				writer.spriteDepth(reader.spriteDepth());
				writer.copyFrom(reader, true);
				writer.colorAll(0, (r.nextInt(0x1000000) & 0xFFFFFF) | 0xFF000000);
				writer.append();
			} while (reader.next());
		}

		return result.releaseToReader();
	}

	static void release(ReadOnlyMeshImpl freeStream) {
		readables.offer(freeStream);
	}

	public static CsgMeshImpl claimCsg() {
		CsgMeshImpl result = csgStreams.poll();

		if (result == null) {
			result = new CsgMeshImpl();
		}

		result.prepare();
		return result;
	}

	public static CsgMesh claimCsg(XmMesh stream) {
		final CsgMeshImpl result = claimCsg();
		result.appendAll(stream);
		return result;
	}

	static void release(CsgMeshImpl freeStream) {
		csgStreams.offer(freeStream);
	}
}
