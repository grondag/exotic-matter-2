package grondag.brocade.collision;

import grondag.fermion.cache.IntSimpleCacheLoader;
import grondag.fermion.cache.IntSimpleLoadingCache;
import grondag.fermion.functions.IBoxBoundsObjectFunction;
import net.minecraft.util.math.BoundingBox;

/**
 * Caches AABB instances that share the same packed key. Mods can use many
 * collision boxes, so this helps reduce memory use and garbage.
 */
public class CollisionBoxStore {
    private static final IntSimpleLoadingCache<BoundingBox> boxCache = new IntSimpleLoadingCache<BoundingBox>(
            new BoxLoader(), 0xFFF);

    public static BoundingBox getBox(int boxKey) {
        return boxCache.get(boxKey);
    }

    static final IBoxBoundsObjectFunction<BoundingBox> boxMaker = (minX, minY, minZ, maxX, maxY, maxZ) -> {
        return new BoundingBox(minX / 8f, minY / 8f, minZ / 8f, maxX / 8f, maxY / 8f, maxZ / 8f);
    };

    private static class BoxLoader implements IntSimpleCacheLoader<BoundingBox> {
        @Override
        public BoundingBox load(int boxKey) {
            return CollisionBoxEncoder.forBoundsObject(boxKey, boxMaker);
        }
    }
}
