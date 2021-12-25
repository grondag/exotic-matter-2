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

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;

@Internal
class MutableMeshImpl extends WritableMeshImpl implements MutableMesh {
	protected final StreamBackedMutablePolygon editor = new StreamBackedMutablePolygon();

	protected MutableMeshImpl() {
		super();
		editor.mesh = this;
	}

	@Override
	protected void prepare(int formatFlags) {
		super.prepare(formatFlags);
		editor.stream = stream;
	}

	@Override
	public void clear() {
		super.clear();
		editor.invalidate();
	}

	@Override
	protected void doRelease() {
		super.doRelease();
		editor.stream = null;
	}

	@Override
	protected void returnToPool() {
		XmMeshesImpl.release(this);
	}

	@Override
	public MutablePolygon editor() {
		return editor;
	}

	@Override
	public MutablePolygon editor(int address) {
		validateAddress(address);
		editor.moveTo(address);
		return editor;
	}

	@Override
	protected void appendCopy(Polygon polyIn, int withFormat) {
		final boolean needReaderLoad = reader.baseAddress == writeAddress;

		// formatFlags for writer poly should already include mutable
		assert MeshFormat.isMutable(withFormat);

		int newFormat = MeshFormat.setLayerCount(withFormat, polyIn.spriteDepth());
		newFormat = MeshFormat.setVertexCount(newFormat, polyIn.vertexCount());
		stream.set(writeAddress, newFormat);
		internal.moveTo(writeAddress);
		internal.copyFrom(polyIn, true);
		writeAddress += MeshFormat.polyStride(newFormat, true);

		if (needReaderLoad) {
			reader.loadFormat();
		}
	}
}
