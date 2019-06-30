package grondag.xm2.connect.api.state;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

import grondag.xm2.connect.api.model.BlockEdge;
import grondag.xm2.connect.api.model.FaceEdge;
import grondag.xm2.connect.api.world.BlockNeighbors;
import net.minecraft.util.math.Direction;

@API(status = STABLE)
public enum SimpleJoinFaceState {
    NO_FACE(0),
    TOP(FaceEdge.TOP_EDGE.ordinalBit),
    BOTTOM(FaceEdge.BOTTOM_EDGE.ordinalBit),
    LEFT(FaceEdge.LEFT_EDGE.ordinalBit),
    RIGHT(FaceEdge.RIGHT_EDGE.ordinalBit),
    TOP_BOTTOM(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit),
    LEFT_RIGHT(FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
    TOP_BOTTOM_RIGHT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
    TOP_BOTTOM_LEFT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit),
    TOP_LEFT_RIGHT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
    BOTTOM_LEFT_RIGHT(FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
    TOP_LEFT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit),
    TOP_RIGHT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
    BOTTOM_LEFT(FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit),
    BOTTOM_RIGHT(FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
    ALL(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit);

    private static final SimpleJoinFaceState[] LOOKUP = new SimpleJoinFaceState[16];

    private final int bitFlags;

    static {
        for (SimpleJoinFaceState state : SimpleJoinFaceState.values()) {
            LOOKUP[state.bitFlags] = state;
        }
    }

    private SimpleJoinFaceState(int faceBits) {
        this.bitFlags = faceBits;

    }

    private static SimpleJoinFaceState find(int faceBits) {
        return LOOKUP[(faceBits & 15)];
    }

    private static final FaceEdge[] EDGES = FaceEdge.values();

    public static SimpleJoinFaceState find(Direction face, SimpleJoinState join) {
        int faceFlags = 0;

        SimpleJoinFaceState fjs;

        if (join.isJoined(face)) {
            fjs = SimpleJoinFaceState.NO_FACE;
        } else {
            for (FaceEdge fside : EDGES) {
                if (join.isJoined(fside.toWorld(face))) {
                    faceFlags |= fside.ordinalBit;
                }
            }

            fjs = SimpleJoinFaceState.find(faceFlags);
        }
        return fjs;
    }

    
    public static SimpleJoinFaceState find(Direction face, BlockNeighbors tests) {
        int faceFlags = 0;

        SimpleJoinFaceState fjs;

        if (tests.result(face)) {
            fjs = SimpleJoinFaceState.NO_FACE;
        } else {
            for (FaceEdge fside : EDGES) {
                Direction joinFace = fside.toWorld(face);
                if (tests.result(joinFace) && !tests.result(BlockEdge.find(face, joinFace))) {
                    faceFlags |= fside.ordinalBit;
                }
            }

            fjs = SimpleJoinFaceState.find(faceFlags);
        }
        return fjs;
    }

    public boolean isJoined(FaceEdge side) {
        return (this.bitFlags & side.ordinalBit) == side.ordinalBit;
    }

    public boolean isJoined(Direction toFace, Direction onFace) {
        FaceEdge side = FaceEdge.fromWorld(toFace, onFace);
        return side == null ? false : this.isJoined(side);
    }
}
