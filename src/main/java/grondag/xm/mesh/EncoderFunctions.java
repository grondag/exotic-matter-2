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
import grondag.xm.mesh.helper.NormalQuantizer;

@Internal
abstract class EncoderFunctions {
	public static final int BAD_ADDRESS = Integer.MIN_VALUE;

	@FunctionalInterface
	public interface ByteGetter {
		int get(IntStream stream, int byteIndex, int baseAddress);

		ByteGetter ZERO = (s, b, a) -> 0;
	}

	@FunctionalInterface
	public interface ByteSetter {
		void set(IntStream stream, int baseAddress, int byteIndex, int value);

		ByteSetter VOID = (s, a, b, v) -> { };
	}

	@FunctionalInterface
	public interface IntGetter {
		int get(IntStream stream, int baseAddress);

		IntGetter ZERO = (s, b) -> 0;
	}

	@FunctionalInterface
	public interface IntSetter {
		void set(IntStream stream, int baseAddress, int value);

		IntSetter VOID = (s, b, v) -> { };
	}

	@FunctionalInterface
	public interface FloatGetter {
		float get(IntStream stream, int baseAddress);

		FloatGetter ZERO = (s, b) -> 0f;
	}

	@FunctionalInterface
	public interface FloatSetter {
		void set(IntStream stream, int baseAddress, float value);

		FloatSetter VOID = (s, b, v) -> { };
	}

	@FunctionalInterface
	public interface FloatSetter2 {
		void set(IntStream stream, int baseAddress, float u, float v);

		FloatSetter2 VOID = (s, b, u, v) -> { };
	}

	@FunctionalInterface
	public interface FloatSetter3 {
		void set(IntStream stream, int baseAddress, float x, float y, float z);

		FloatSetter3 VOID = (s, b, x, y, z) -> { };
	}

	public static final ByteGetter GET_BYTE_FAIL = (stream, address, byteIndex) -> {
		throw new UnsupportedOperationException();
	};
	public static final ByteGetter GET_BYTE = (stream, address, byteIndex) -> (stream.get(address) >>> byteIndex * 8) & 0xFF;

	public static final ByteSetter SET_BYTE = (stream, address, byteIndex, value) -> {
		final int shift = 8 * byteIndex;
		final int mask = 0xFF << shift;
		stream.set(address, (stream.get(address) & ~mask) | ((value << shift) & mask));
	};

	public static final ByteSetter SET_BYTE_FAIL = (stream, address, byteIndex, value) -> {
		throw new UnsupportedOperationException();
	};

	public static final IntGetter GET_INT_FAIL = (stream, address) -> {
		throw new UnsupportedOperationException();
	};
	public static final IntGetter GET_INT = (stream, address) -> stream.get(address);

	public static final IntGetter GET_INT_WHITE = (stream, address) -> 0xFFFFFFFF;
	public static final IntSetter SET_INT_WHITE = (stream, address, value) -> {
		if (value != 0xFFFFFFFF) {
			throw new UnsupportedOperationException();
		}
	};

	public static final IntGetter GET_HALF_INT_LOW = (stream, address) -> stream.get(address) & 0xFFFF;
	public static final IntGetter GET_HALF_INT_HIGH = (stream, address) -> (stream.get(address) >>> 16) & 0xFFFF;

	public static final IntSetter SET_INT_FAIL = (stream, address, value) -> {
		throw new UnsupportedOperationException();
	};
	public static final IntSetter SET_INT = (stream, address, value) -> stream.set(address, value);

	public static final IntSetter SET_HALF_INT_LOW = (stream, address, value) -> stream.set(address, (stream.get(address) & 0xFFFF0000) | (value & 0xFFFF));
	public static final IntSetter SET_HALF_INT_HIGH = (stream, address, value) -> stream.set(address, (stream.get(address) & 0x0000FFFF) | (value << 16));

	public static final FloatGetter GET_FLOAT_FAIL = (stream, address) -> {
		throw new UnsupportedOperationException();
	};
	public static final FloatGetter GET_FLOAT = (stream, address) -> Float.intBitsToFloat(stream.get(address));

	public static final FloatSetter SET_FLOAT_FAIL = (stream, address, value) -> {
		throw new UnsupportedOperationException();
	};
	public static final FloatSetter SET_FLOAT = (stream, address, value) -> stream.set(address, Float.floatToRawIntBits(value));

	public static final FloatSetter2 SET_FLOAT2_FAIL = (stream, address, u, v) -> {
		throw new UnsupportedOperationException();
	};
	public static final FloatSetter2 SET_FLOAT2 = (stream, address, u, v) -> {
		stream.set(address, Float.floatToRawIntBits(u));
		stream.set(address + 1, Float.floatToRawIntBits(v));
	};

	public static final FloatSetter3 SET_FLOAT3_FAIL = (stream, address, x, y, z) -> {
		throw new UnsupportedOperationException();
	};
	public static final FloatSetter3 SET_FLOAT3 = (stream, address, x, y, z) -> {
		stream.set(address, Float.floatToRawIntBits(x));
		stream.set(address + 1, Float.floatToRawIntBits(y));
		stream.set(address + 2, Float.floatToRawIntBits(z));
	};

	public static final FloatGetter GET_NORMAL_X_QUANTIZED = (stream, address) -> NormalQuantizer.unpackX(stream.get(address));
	public static final FloatGetter GET_NORMAL_Y_QUANTIZED = (stream, address) -> NormalQuantizer.unpackY(stream.get(address));
	public static final FloatGetter GET_NORMAL_Z_QUANTIZED = (stream, address) -> NormalQuantizer.unpackZ(stream.get(address));
	public static final FloatSetter3 SET_NORMAL_XYZ_QUANTIZED = (stream, address, x, y, z) -> stream.set(address, NormalQuantizer.pack(x, y, z));
}
