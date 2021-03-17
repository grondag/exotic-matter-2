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
