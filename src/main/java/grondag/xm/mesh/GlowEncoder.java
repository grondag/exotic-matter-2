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

import static grondag.xm.mesh.MeshFormat.VERTEX_GLOW_NONE;
import static grondag.xm.mesh.MeshFormat.VERTEX_GLOW_PER_VERTEX;
import static grondag.xm.mesh.MeshFormat.VERTEX_GLOW_SAME;
import static grondag.xm.mesh.MeshFormat.getVertexCount;
import static grondag.xm.mesh.MeshFormat.getVertexGlowFormat;
import static grondag.xm.mesh.MeshFormat.isMutable;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.fermion.intstream.IntStream;

@Internal
abstract class GlowEncoder {
	private static final GlowEncoder NO_GLOW = new GlowEncoder() {
		@Override
		public final int getGlow(IntStream stream, int glowAddress, int vertexIndex) {
			return 0;
		}

		@Override
		public final void setGlow(IntStream stream, int glowAddress, int vertexIndex, int glow) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected final int stride(int format) {
			return 0;
		}

		@Override
		public int glowFormat() {
			return VERTEX_GLOW_NONE;
		}
	};

	private static final GlowEncoder SAME_GLOW = new GlowEncoder() {
		@Override
		public final int getGlow(IntStream stream, int glowAddress, int vertexIndex) {
			return stream.get(glowAddress);
		}

		@Override
		public final void setGlow(IntStream stream, int glowAddress, int vertexIndex, int glow) {
			stream.set(glowAddress, glow);
		}

		@Override
		protected final int stride(int format) {
			return 1;
		}

		@Override
		public int glowFormat() {
			return VERTEX_GLOW_SAME;
		}
	};

	private static final GlowEncoder VERTEX_GLOW = new GlowEncoder() {
		@Override
		public final int getGlow(IntStream stream, int glowAddress, int vertexIndex) {
			final int streamIndex = glowAddress + (vertexIndex >> 2);
			final int byteIndex = vertexIndex & 3;
			final int shift = 8 * byteIndex;
			return (stream.get(streamIndex) >> shift) & 0xFF;
		}

		@Override
		public final void setGlow(IntStream stream, int glowAddress, int vertexIndex, int glow) {
			final int streamIndex = glowAddress + (vertexIndex >> 2);
			final int byteIndex = vertexIndex & 3;
			final int shift = 8 * byteIndex;
			final int mask = 0xFF << shift;
			stream.set(streamIndex, (stream.get(streamIndex) & ~mask) | ((glow & 0xFF) << shift));
		}

		@Override
		protected final int stride(int format) {
			return (getVertexCount(format) + 3) / 4;
		}

		@Override
		public int glowFormat() {
			return VERTEX_GLOW_PER_VERTEX;
		}
	};

	/**
	 * All mutable formats will have per-vertex glow.
	 */
	public static GlowEncoder get(int format) {
		final int glowFormat = getVertexGlowFormat(format);
		return glowFormat == VERTEX_GLOW_PER_VERTEX || isMutable(format) ? VERTEX_GLOW : glowFormat == VERTEX_GLOW_SAME ? SAME_GLOW : NO_GLOW;
	}

	public abstract int glowFormat();

	public abstract int getGlow(IntStream stream, int glowAddress, int vertexIndex);

	public abstract void setGlow(IntStream stream, int glowAddress, int vertexIndex, int glow);

	protected abstract int stride(int format);
}
