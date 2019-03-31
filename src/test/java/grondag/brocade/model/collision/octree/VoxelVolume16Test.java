package grondag.brocade.collision.octree;

import org.junit.jupiter.api.Test;

import grondag.brocade.Brocade;

class VoxelVolume16Test {

    @Test
    void test() {
        long[] data = new long[128];

        // 0000000000000000
        // 0001111111111100
        // 0010000000000010
        // 0010000000000010

        // 0100000000000001
        // 0100000000000001
        // 0100000000000001
        // 1000000000000010

        // 1000000000000100
        // 1000000000000010
        // 1000000000001100
        // 0100000000001000

        // 0100000000001000
        // 0010000000001000
        // 0100000000001000
        // 0111111111111100
        final long[] section = { 0b0100000000001000001000000000100001000000000010000111111111111100L,
                0b1000000000000100100000000000001010000000000011000100000000001000L,
                0b0100000000000001010000000000000101000000000000011000000000000010L,
                0b0000000000000000000111111111110000100000000000100010000000000010L };

        // 0000000000000000
        // 0001111111111100
        // 0011111111111110
        // 0011111111111110

        // 0111111111111111
        // 0111111111111111
        // 0111111111111111
        // 1111111111111110

        // 1111111111111100
        // 1111111111111110
        // 1111111111111100
        // 0111111111111000

        // 0111111111111000
        // 0011111111111000
        // 0111111111111000
        // 0111111111111100
        final long cap[] = { 0b0111111111111000001111111111100001111111111110000111111111111100L,
                0b1111111111111100111111111111111011111111111111000111111111111000L,
                0b0111111111111111011111111111111101111111111111111111111111111110L,
                0b0000000000000000000111111111110000111111111111100011111111111110L };

        System.arraycopy(cap, 0, data, 4, 4);
        System.arraycopy(section, 0, data, 8, 4);
        System.arraycopy(section, 0, data, 12, 4);
        System.arraycopy(section, 0, data, 16, 4);
        System.arraycopy(section, 0, data, 20, 4);
        System.arraycopy(section, 0, data, 24, 4);
        System.arraycopy(section, 0, data, 28, 4);
        System.arraycopy(section, 0, data, 32, 4);
        System.arraycopy(section, 0, data, 36, 4);
        System.arraycopy(section, 0, data, 40, 4);
        System.arraycopy(section, 0, data, 44, 4);
        System.arraycopy(section, 0, data, 48, 4);
        System.arraycopy(section, 0, data, 52, 4);
        System.arraycopy(section, 0, data, 56, 4);
        System.arraycopy(cap, 0, data, 60, 4);

        for (int i = 0; i < 16; i++)
            VoxelVolume16Test.outputLayer(data, i);

        VoxelVolume16.fillVolume(data);

        assert data[64] == 0;
        assert data[65] == 0;
        assert data[66] == 0;
        assert data[67] == 0;

        for (int i = 17; i <= 31; i++)
            assertEquals(data, i, cap);

    }

    void assertEquals(long[] data, int index, long[] expected) {
        outputLayer(data, index);

        int i = index * 4;
        assert data[i] == expected[0];
        assert data[i + 1] == expected[1];
        assert data[i + 2] == expected[2];
        assert data[i + 3] == expected[3];
    }

    static void outputLayer(long[] data, int index) {
        int i = index * 4;
        Brocade.INSTANCE.info("LAYER %d", index);
        outputSubWord(data[i + 3]);
        outputSubWord(data[i + 2]);
        outputSubWord(data[i + 1]);
        outputSubWord(data[i + 0]);
        Brocade.INSTANCE.info("");
    }

    private static void outputSubWord(long bits) {
        Brocade.INSTANCE.info(pad((bits >>> 48) & 0xFFFFL));
        Brocade.INSTANCE.info(pad((bits >>> 32) & 0xFFFFL));
        Brocade.INSTANCE.info(pad((bits >>> 16) & 0xFFFFL));
        Brocade.INSTANCE.info(pad(bits & 0xFFFFL));
    }

    private static String pad(long value) {
        String s = "0000000000000000" + Long.toBinaryString(value);
        return s.substring(s.length() - 16);
    }
}
