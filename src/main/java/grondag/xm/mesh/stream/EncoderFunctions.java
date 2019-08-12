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

package grondag.xm.mesh.stream;

import grondag.fermion.intstream.IntStream;
import grondag.xm.mesh.helper.NormalQuantizer;

public abstract class EncoderFunctions {
    public static final int BAD_ADDRESS = Integer.MIN_VALUE;

    @FunctionalInterface
    public static interface ByteGetter {
        int get(IntStream stream, int byteIndex, int baseAddress);
        
        static ByteGetter ZERO  = (s, b, a) -> 0;
    }

    @FunctionalInterface
    public static interface ByteSetter {
        void set(IntStream stream, int baseAddress, int byteIndex, int value);
        
        static ByteSetter VOID = (s, a, b, v) -> {};
    }

    @FunctionalInterface
    public static interface IntGetter {
        int get(IntStream stream, int baseAddress);
        
        static IntGetter ZERO  = (s, b) -> 0;
    }

    @FunctionalInterface
    public static interface IntSetter {
        void set(IntStream stream, int baseAddress, int value);
        
        static IntSetter VOID = (s, b, v) -> {};
    }

    @FunctionalInterface
    public static interface FloatGetter {
        float get(IntStream stream, int baseAddress);
        
        static FloatGetter ZERO  = (s, b) -> 0f;
    }

    @FunctionalInterface
    public static interface FloatSetter {
        void set(IntStream stream, int baseAddress, float value);
        
        static FloatSetter VOID = (s, b, v) -> {};
    }

    @FunctionalInterface
    public static interface FloatSetter2 {
        void set(IntStream stream, int baseAddress, float u, float v);
        
        static FloatSetter2 VOID = (s, b, u, v) -> {};
    }

    @FunctionalInterface
    public static interface FloatSetter3 {
        void set(IntStream stream, int baseAddress, float x, float y, float z);
        
        static FloatSetter3 VOID = (s, b, x, y, z) -> {};
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
        if (value != 0xFFFFFFFF)
            throw new UnsupportedOperationException();
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
