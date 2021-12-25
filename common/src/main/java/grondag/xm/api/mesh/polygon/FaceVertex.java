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

package grondag.xm.api.mesh.polygon;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Used to express quads on a face (2D). By default u,v map directly to x, y on
 * the given face
 */
@Experimental
public class FaceVertex {
	public final float x;
	public final float y;
	public final float depth;
	protected final short glow;

	public FaceVertex(float x, float y, float depth) {
		this(x, y, depth, 0);
	}

	public FaceVertex(float x, float y, float depth, int glow) {
		this.x = x;
		this.y = y;
		this.depth = depth;
		this.glow = (short) (glow & 0xFF);
	}

	public int glow() {
		return glow;
	}

	@Override
	public FaceVertex clone() {
		assert false : "Why u clone immutable object?";
		return new FaceVertex(x, y, depth, glow);
	}

	public FaceVertex withXY(float x, float y) {
		return new FaceVertex(x, y, depth, glow);
	}

	public FaceVertex withDepth(float depth) {
		return new FaceVertex(x, y, depth, glow);
	}

	public FaceVertex withColor(int color) {
		return new FaceVertex.Colored(x, y, depth, color, glow);
	}

	public FaceVertex withGlow(int glow) {
		return new FaceVertex(x, y, depth, glow);
	}

	public FaceVertex withUV(float u, float v) {
		return new FaceVertex.UV(x, y, depth, u, v, glow);
	}

	public int color() {
		return 0xFFFFFFFF;
	}

	/**
	 * This value is logical 0-1 within the texture for this face. NOT 0-16. And NOT
	 * interpolated for the sprite.
	 *
	 * <p>Note that the V orientation is flipped from the Y axis used for vertices.
	 * Origin is at the top left for textures, vs. bottom left for vertex
	 * coordinates. This means the default values for u, v will be x, 1-y.
	 *
	 * <p>The bottom face is handled differently and RawQuad will flip it
	 * automatically..
	 */
	public float u() {
		return x;
	}

	/**
	 * See {@link #u()}.
	 */
	public float v() {
		return 1 - y;
	}

	public static class Colored extends FaceVertex {
		private final int color;

		public Colored(float x, float y, float depth, int color, int glow) {
			super(x, y, depth, glow);
			this.color = color;
		}

		public Colored(float x, float y, float depth, float u, float v, int color, int glow) {
			super(x, y, depth, glow);
			this.color = color;
		}

		@Override
		public FaceVertex clone() {
			return new FaceVertex.Colored(x, y, depth, color, glow);
		}

		@Override
		public int color() {
			return color;
		}

		@Override
		public FaceVertex withXY(float x, float y) {
			return new FaceVertex.Colored(x, y, depth, color, glow);
		}

		@Override
		public FaceVertex withDepth(float depth) {
			return new FaceVertex.Colored(x, y, depth, color, glow);
		}

		@Override
		public FaceVertex withColor(int color) {
			return new FaceVertex.Colored(x, y, depth, color, glow);
		}

		@Override
		public FaceVertex withGlow(int glow) {
			return new FaceVertex.Colored(x, y, depth, color, glow);
		}

		@Override
		public FaceVertex withUV(float u, float v) {
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}
	}

	public static class UV extends FaceVertex {
		private final float u;
		private final float v;

		public UV(float x, float y, float depth, float u, float v) {
			this(x, y, depth, u, v, 0);
		}

		public UV(float x, float y, float depth, float u, float v, int glow) {
			super(x, y, depth, glow);
			this.u = u;
			this.v = v;
		}

		@Override
		public float u() {
			return u;
		}

		@Override
		public float v() {
			return v;
		}

		@Override
		public FaceVertex clone() {
			assert false : "Why u clone immutable object?";
			return new FaceVertex.UV(x, y, depth, u, v, glow);
		}

		@Override
		public FaceVertex withXY(float x, float y) {
			return new FaceVertex.UV(x, y, depth, u, v, glow);
		}

		@Override
		public FaceVertex withDepth(float depth) {
			return new FaceVertex.UV(x, y, depth, u, v, glow);
		}

		@Override
		public FaceVertex withColor(int color) {
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}

		@Override
		public FaceVertex withGlow(int glow) {
			return new FaceVertex.UV(x, y, depth, u, v, glow);
		}

		@Override
		public FaceVertex withUV(float u, float v) {
			return new FaceVertex.UV(x, y, depth, u, v, glow);
		}
	}

	public static class UVColored extends FaceVertex {
		private final float u;
		private final float v;
		private final int color;

		public UVColored(float x, float y, float depth, float u, float v, int color, int glow) {
			super(x, y, depth, glow & 0xFF);
			this.u = u;
			this.v = v;
			this.color = color;
		}

		@Override
		public int color() {
			return color;
		}

		@Override
		public float u() {
			return u;
		}

		@Override
		public float v() {
			return v;
		}

		@Override
		public FaceVertex clone() {
			assert false : "Why u clone immutable object?";
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}

		@Override
		public FaceVertex withXY(float x, float y) {
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}

		@Override
		public FaceVertex withDepth(float depth) {
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}

		@Override
		public FaceVertex withColor(int color) {
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}

		@Override
		public FaceVertex withGlow(int glow) {
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}

		@Override
		public FaceVertex withUV(float u, float v) {
			return new FaceVertex.UVColored(x, y, depth, u, v, color, glow);
		}
	}
}
