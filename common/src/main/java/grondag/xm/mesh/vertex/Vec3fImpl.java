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

package grondag.xm.mesh.vertex;

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.mesh.polygon.Vec3f;

@Internal
public class Vec3fImpl implements Vec3f {
	static final Vec3fImpl ZERO = new Vec3fImpl(0, 0, 0);

	protected float x;
	protected float y;
	protected float z;

	Vec3fImpl(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public boolean isMutable() {
		return false;
	}

	@Override
	public final float x() {
		return x;
	}

	@Override
	public final float y() {
		return y;
	}

	@Override
	public final float z() {
		return z;
	}

	public Mutable mutableCopy() {
		return new Mutable(x, y, z);
	}

	public static class Mutable extends Vec3fImpl {
		public Mutable(float x, float y, float z) {
			super(x, y, z);
		}

		public Mutable load(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public final Mutable load(Vec3fImpl fromVec) {
			x = fromVec.x;
			y = fromVec.y;
			z = fromVec.z;
			return this;
		}

		@Override
		public boolean isMutable() {
			return true;
		}

		public final Vec3fImpl toImmutable() {
			return (Vec3fImpl) Vec3f.create(x, y, z);
		}

		public final Mutable subtract(Vec3fImpl vec) {
			return this.subtract(vec.x, vec.y, vec.z);
		}

		public final Mutable subtract(float x, float y, float z) {
			return addVector(-x, -y, -z);
		}

		public final Mutable add(Vec3fImpl vec) {
			return addVector(vec.x, vec.y, vec.z);
		}

		public final Mutable addVector(float x, float y, float z) {
			this.x += x;
			this.y += y;
			this.z += z;
			return this;
		}

		public final Mutable scale(float factor) {
			x *= factor;
			y *= factor;
			z *= factor;
			return this;
		}

		public final Mutable invert() {
			x = -x;
			y = -y;
			z = -z;
			return this;
		}

		public final Mutable normalize() {
			final float mag = length();

			if (mag < 1.0E-4F) {
				x = 0;
				y = 0;
				z = 0;
			} else {
				x /= mag;
				y /= mag;
				z /= mag;
			}

			return this;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (obj instanceof final Vec3fImpl v) {
			return v.x == x && v.y == y && v.z == z;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) HashCommon.mix((Float.floatToRawIntBits(x) ^ Float.floatToRawIntBits(z)) | (long) Float.floatToRawIntBits(y) << 32);
	}
}
