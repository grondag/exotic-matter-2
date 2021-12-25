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

import static grondag.xm.mesh.MeshFormat.VERTEX_GLOW_NONE;
import static grondag.xm.mesh.MeshFormat.VERTEX_GLOW_PER_VERTEX;
import static grondag.xm.mesh.MeshFormat.VERTEX_GLOW_SAME;
import static grondag.xm.mesh.MeshFormat.getVertexCount;
import static grondag.xm.mesh.MeshFormat.getVertexGlowFormat;
import static grondag.xm.mesh.MeshFormat.isMutable;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.intstream.IntStream;

@Internal
abstract class GlowEncoder {
	private static final GlowEncoder NO_GLOW = new GlowEncoder() {
		@Override
		public int getGlow(IntStream stream, int glowAddress, int vertexIndex) {
			return 0;
		}

		@Override
		public void setGlow(IntStream stream, int glowAddress, int vertexIndex, int glow) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected int stride(int format) {
			return 0;
		}

		@Override
		public int glowFormat() {
			return VERTEX_GLOW_NONE;
		}
	};

	private static final GlowEncoder SAME_GLOW = new GlowEncoder() {
		@Override
		public int getGlow(IntStream stream, int glowAddress, int vertexIndex) {
			return stream.get(glowAddress);
		}

		@Override
		public void setGlow(IntStream stream, int glowAddress, int vertexIndex, int glow) {
			stream.set(glowAddress, glow);
		}

		@Override
		protected int stride(int format) {
			return 1;
		}

		@Override
		public int glowFormat() {
			return VERTEX_GLOW_SAME;
		}
	};

	private static final GlowEncoder VERTEX_GLOW = new GlowEncoder() {
		@Override
		public int getGlow(IntStream stream, int glowAddress, int vertexIndex) {
			final int streamIndex = glowAddress + (vertexIndex >> 2);
			final int byteIndex = vertexIndex & 3;
			final int shift = 8 * byteIndex;
			return (stream.get(streamIndex) >> shift) & 0xFF;
		}

		@Override
		public void setGlow(IntStream stream, int glowAddress, int vertexIndex, int glow) {
			final int streamIndex = glowAddress + (vertexIndex >> 2);
			final int byteIndex = vertexIndex & 3;
			final int shift = 8 * byteIndex;
			final int mask = 0xFF << shift;
			stream.set(streamIndex, (stream.get(streamIndex) & ~mask) | ((glow & 0xFF) << shift));
		}

		@Override
		protected int stride(int format) {
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
