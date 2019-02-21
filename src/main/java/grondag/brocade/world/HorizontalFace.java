package grondag.brocade.world;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public enum HorizontalFace {
    NORTH(Direction.NORTH), EAST(Direction.EAST), SOUTH(Direction.SOUTH), WEST(Direction.WEST);

    public static final HorizontalFace[] VALUES = HorizontalFace.values();
    public static final int COUNT = VALUES.length;

    private static final HorizontalFace HORIZONTAL_FACE_LOOKUP[] = new HorizontalFace[6];

    static {
        for (HorizontalFace hFace : HorizontalFace.values()) {
            HORIZONTAL_FACE_LOOKUP[hFace.face.ordinal()] = hFace;
        }
    }

    public final Direction face;

    public final Vec3i directionVector;

    private HorizontalFace(Direction face) {
        this.face = face;
        this.directionVector = face.getVector();
    }

    public static HorizontalFace find(Direction face) {
        return HorizontalFace.HORIZONTAL_FACE_LOOKUP[face.ordinal()];
    }

    public HorizontalFace getLeft() {
        if (this.ordinal() == 0) {
            return HorizontalFace.values()[3];
        } else {
            return HorizontalFace.values()[this.ordinal() - 1];
        }
    }

    public HorizontalFace getRight() {
        if (this.ordinal() == 3) {
            return HorizontalFace.values()[0];
        } else {
            return HorizontalFace.values()[this.ordinal() + 1];
        }
    }

}