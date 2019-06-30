package grondag.xm2.primitives.stream;

import grondag.fermion.intstream.IIntStream;
import grondag.fermion.varia.NormalQuantizer;

public abstract class EncoderFunctions {
    public static final int BAD_ADDRESS = Integer.MIN_VALUE;

    @FunctionalInterface
    public static interface ByteGetter {
        int get(IIntStream stream, int byteIndex, int baseAddress);
    }

    @FunctionalInterface
    public static interface ByteSetter {
        void set(IIntStream stream, int baseAddress, int byteIndex, int value);
    }

    @FunctionalInterface
    public static interface IntGetter {
        int get(IIntStream stream, int baseAddress);
    }

    @FunctionalInterface
    public static interface IntSetter {
        void set(IIntStream stream, int baseAddress, int value);
    }

    @FunctionalInterface
    public static interface FloatGetter {
        float get(IIntStream stream, int baseAddress);
    }

    @FunctionalInterface
    public static interface FloatSetter {
        void set(IIntStream stream, int baseAddress, float value);
    }

    @FunctionalInterface
    public static interface FloatSetter2 {
        void set(IIntStream stream, int baseAddress, float u, float v);
    }

    @FunctionalInterface
    public static interface FloatSetter3 {
        void set(IIntStream stream, int baseAddress, float x, float y, float z);
    }

    public static final ByteGetter GET_BYTE_FAIL = (stream, address, byteIndex) -> {
        throw new UnsupportedOperationException();
    };
    public static final ByteGetter GET_BYTE = (stream, address, byteIndex) -> (stream.get(address) >>> byteIndex * 8)
            & 0xFF;

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

    public static final IntSetter SET_HALF_INT_LOW = (stream, address, value) -> stream.set(address,
            (stream.get(address) & 0xFFFF0000) | (value & 0xFFFF));
    public static final IntSetter SET_HALF_INT_HIGH = (stream, address, value) -> stream.set(address,
            (stream.get(address) & 0x0000FFFF) | (value << 16));

    public static final FloatGetter GET_FLOAT_FAIL = (stream, address) -> {
        throw new UnsupportedOperationException();
    };
    public static final FloatGetter GET_FLOAT = (stream, address) -> Float.intBitsToFloat(stream.get(address));

    public static final FloatSetter SET_FLOAT_FAIL = (stream, address, value) -> {
        throw new UnsupportedOperationException();
    };
    public static final FloatSetter SET_FLOAT = (stream, address, value) -> stream.set(address,
            Float.floatToRawIntBits(value));

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

    public static final FloatGetter GET_NORMAL_X_QUANTIZED = (stream, address) -> NormalQuantizer
            .unpackX(stream.get(address));
    public static final FloatGetter GET_NORMAL_Y_QUANTIZED = (stream, address) -> NormalQuantizer
            .unpackY(stream.get(address));
    public static final FloatGetter GET_NORMAL_Z_QUANTIZED = (stream, address) -> NormalQuantizer
            .unpackZ(stream.get(address));
    public static final FloatSetter3 SET_NORMAL_XYZ_QUANTIZED = (stream, address, x, y, z) -> stream.set(address,
            NormalQuantizer.pack(x, y, z));
}
