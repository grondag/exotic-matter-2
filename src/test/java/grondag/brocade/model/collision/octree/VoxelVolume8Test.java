package grondag.brocade.collision.octree;

import org.junit.jupiter.api.Test;

import grondag.brocade.Brocade;

class VoxelVolume8Test {

    @Test
    void test() {
        long[] data = new long[16];

        // 00000000
        // 00011100
        // 00100010
        // 01000001
        // 10000010
        // 10001100
        // 01001000
        // 01111100
        final long section = 0b0000000000011100001000100100000110000010100011000100100001111100L;

        // 00000000
        // 00011100
        // 00111110
        // 01111111
        // 11111110
        // 11111100
        // 01111000
        // 01111100
        final long cap = 0b0000000000011100001111100111111111111110111111000111100001111100L;

        data[0] = 0;
        data[1] = cap;
        data[2] = section;
        data[3] = section;
        data[4] = section;
        data[5] = section;
        data[6] = section;
        data[7] = cap;

        VoxelVolume8.fillVolume(data);
        outputCarvedLayers(data);
        assert data[8] == 0;
        assert data[9] == cap;
        assert data[10] == cap;
        assert data[11] == cap;
        assert data[12] == cap;
        assert data[13] == cap;
        assert data[14] == cap;
        assert data[15] == cap;

    }

    void outputCarvedLayers(long[] data) {
        for (int i = 8; i < 16; i++)
            outputLayer(data, i);
    }

    void outputLayer(long[] data, int index) {
        Brocade.INSTANCE.info("LAYER %d", index);
        long bits = data[index];
        Brocade.INSTANCE.info(Long.toBinaryString(bits & 0xFFL));
        Brocade.INSTANCE.info(Long.toBinaryString((bits >> 8) & 0xFFL));
        Brocade.INSTANCE.info(Long.toBinaryString((bits >> 16) & 0xFFL));
        Brocade.INSTANCE.info(Long.toBinaryString((bits >> 24) & 0xFFL));
        Brocade.INSTANCE.info(Long.toBinaryString((bits >> 32) & 0xFFL));
        Brocade.INSTANCE.info(Long.toBinaryString((bits >> 40) & 0xFFL));
        Brocade.INSTANCE.info(Long.toBinaryString((bits >> 48) & 0xFFL));
        Brocade.INSTANCE.info(Long.toBinaryString((bits >> 56) & 0xFFL));
        Brocade.INSTANCE.info("");
    }
}
