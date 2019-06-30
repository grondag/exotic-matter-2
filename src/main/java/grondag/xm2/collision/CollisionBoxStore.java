package grondag.xm2.collision;

import grondag.fermion.cache.IntSimpleCacheLoader;
import grondag.fermion.cache.IntSimpleLoadingCache;
import grondag.fermion.functions.IBoxBoundsObjectFunction;
import net.minecraft.util.math.Box;

/**
 * Caches AABB instances that share the same packed key. Mods can use many
 * collision boxes, so this helps reduce memory use and garbage.
 */
public class CollisionBoxStore {
    private static final IntSimpleLoadingCache<Box> boxCache = new IntSimpleLoadingCache<Box>(
            new BoxLoader(), 0xFFF);

    public static Box getBox(int boxKey) {
        return boxCache.get(boxKey);
    }

    static final IBoxBoundsObjectFunction<Box> boxMaker = (minX, minY, minZ, maxX, maxY, maxZ) -> {
        return new Box(minX / 8f, minY / 8f, minZ / 8f, maxX / 8f, maxY / 8f, maxZ / 8f);
    };

    private static class BoxLoader implements IntSimpleCacheLoader<Box> {
        @Override
        public Box load(int boxKey) {
            return CollisionBoxEncoder.forBoundsObject(boxKey, boxMaker);
        }
    }
}
