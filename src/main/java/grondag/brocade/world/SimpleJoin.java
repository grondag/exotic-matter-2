package grondag.brocade.world;

import grondag.fermion.varia.DirectionHelper;
import net.minecraft.util.math.Direction;

public class SimpleJoin {

    private final byte joins;

    public static final int STATE_COUNT = 64; // 2^6

    public SimpleJoin(NeighborBlocks.NeighborTestResults testResults) {
        this.joins = getIndex(testResults);
    }

    public static byte getIndex(NeighborBlocks.NeighborTestResults testResults) {
        byte j = 0;
        for (int i = 0; i < 6; i++) {
            if (testResults.result(DirectionHelper.fromOrdinal(i))) {
                j |= NeighborBlocks.FACE_FLAGS[i];
            }
        }
        return j;
    }

    public SimpleJoin(boolean up, boolean down, boolean east, boolean west, boolean north, boolean south) {
        byte j = 0;
        if (up)
            j |= NeighborBlocks.FACE_FLAGS[Direction.UP.ordinal()];
        if (down)
            j |= NeighborBlocks.FACE_FLAGS[Direction.DOWN.ordinal()];
        if (east)
            j |= NeighborBlocks.FACE_FLAGS[Direction.EAST.ordinal()];
        if (west)
            j |= NeighborBlocks.FACE_FLAGS[Direction.WEST.ordinal()];
        if (north)
            j |= NeighborBlocks.FACE_FLAGS[Direction.NORTH.ordinal()];
        if (south)
            j |= NeighborBlocks.FACE_FLAGS[Direction.SOUTH.ordinal()];
        this.joins = j;
    }

    public SimpleJoin(int index) {
        this.joins = (byte) index;
    }

    public boolean isJoined(Direction face) {
        return (joins & NeighborBlocks.FACE_FLAGS[face.ordinal()]) == NeighborBlocks.FACE_FLAGS[face.ordinal()];
    }

    public int getIndex() {
        return (int) joins;
    }
}