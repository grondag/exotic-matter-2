package grondag.brocade.world;

import net.minecraft.util.math.Vec3i;

public enum HorizontalCorner
{
    NORTH_EAST(HorizontalFace.NORTH, HorizontalFace.EAST),
    NORTH_WEST(HorizontalFace.NORTH, HorizontalFace.WEST),
    SOUTH_EAST(HorizontalFace.SOUTH, HorizontalFace.EAST),
    SOUTH_WEST(HorizontalFace.SOUTH, HorizontalFace.WEST);

    public static final HorizontalCorner[] VALUES = HorizontalCorner.values();
    public static final int COUNT = VALUES.length;
    
    private static final HorizontalCorner[][] HORIZONTAL_CORNER_LOOKUP = new HorizontalCorner[4][4];
    
    static
    {
        for(HorizontalCorner corner : HorizontalCorner.values())
        {
            HORIZONTAL_CORNER_LOOKUP[corner.face1.ordinal()][corner.face2.ordinal()] = corner;
            HORIZONTAL_CORNER_LOOKUP[corner.face2.ordinal()][corner.face1.ordinal()] = corner;
        }
    }
    
    public final HorizontalFace face1;
    public final HorizontalFace face2;

    public final Vec3i directionVector;

    private HorizontalCorner(HorizontalFace face1, HorizontalFace face2)
    {
        this.face1 = face1;
        this.face2 = face2;
        this.directionVector = new Vec3i(face1.face.getVector().getX() + face2.face.getVector().getX(), 0, face1.face.getVector().getZ() + face2.face.getVector().getZ());    }

    public static HorizontalCorner find(HorizontalFace face1, HorizontalFace face2)
    {
        return HorizontalCorner.HORIZONTAL_CORNER_LOOKUP[face1.ordinal()][face2.ordinal()];
    }

}