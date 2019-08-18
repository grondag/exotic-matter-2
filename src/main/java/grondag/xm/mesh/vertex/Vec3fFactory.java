package grondag.xm.mesh.vertex;

import grondag.xm.api.mesh.polygon.Vec3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class Vec3fFactory {
    private Vec3fFactory() {}
    
    private static final Vec3f[] FACES = new Vec3f[6];

    static {
        FACES[Direction.UP.ordinal()] = create(Direction.UP.getVector());
        FACES[Direction.DOWN.ordinal()] = create(Direction.DOWN.getVector());
        FACES[Direction.EAST.ordinal()] = create(Direction.EAST.getVector());
        FACES[Direction.WEST.ordinal()] = create(Direction.WEST.getVector());
        FACES[Direction.NORTH.ordinal()] = create(Direction.NORTH.getVector());
        FACES[Direction.SOUTH.ordinal()] = create(Direction.SOUTH.getVector());
    }
    
    public static final Vec3f ZERO = Vec3fImpl.ZERO;

    public static Vec3f forFace(Direction face) {
        return FACES[face.ordinal()];
    }

    public static Vec3f create(Vec3i vec) {
        return create(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vec3f create(float x, float y, float z) {
            return Vec3fCache.INSTANCE.get(x, y, z);
    }
}
