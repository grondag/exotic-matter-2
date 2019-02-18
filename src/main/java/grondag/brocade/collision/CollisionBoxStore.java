package grondag.brocade.collision;

import grondag.exotic_matter.cache.IntSimpleCacheLoader;
import grondag.exotic_matter.cache.IntSimpleLoadingCache;
import grondag.exotic_matter.varia.functions.IBoxBoundsObjectFunction;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Caches AABB instances that share the same packed key.  Mods can 
 * use many collision boxes, so this helps reduce memory use and garbage.
 */
public class CollisionBoxStore
{
    private static final IntSimpleLoadingCache<AxisAlignedBB> boxCache = new IntSimpleLoadingCache<AxisAlignedBB>(new BoxLoader(),  0xFFF);

    public static AxisAlignedBB getBox(int boxKey)
    {
        return boxCache.get(boxKey);
    }
    
    static final IBoxBoundsObjectFunction<AxisAlignedBB> boxMaker = (minX, minY, minZ, maxX, maxY, maxZ) ->
    {
        return new AxisAlignedBB(minX / 8f, minY / 8f, minZ / 8f, 
                maxX / 8f, maxY / 8f, maxZ / 8f);
    };
    
    private static class BoxLoader implements IntSimpleCacheLoader<AxisAlignedBB>
    {
        @Override
        public AxisAlignedBB load(int boxKey)
        {
            return CollisionBoxEncoder.forBoundsObject(boxKey, boxMaker);
        }
    }
}
