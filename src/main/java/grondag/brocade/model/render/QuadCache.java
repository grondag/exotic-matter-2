package grondag.brocade.model.render;

import javax.annotation.Nonnull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import grondag.exotic_matter.ConfigXM;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuadCache {
    public static final QuadCache INSTANCE = new QuadCache();
    public final LoadingCache<BakedQuad, BakedQuad> cache;

    public QuadCache() {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .maximumSize(ConfigXM.RENDER.quadCacheSizeLimit).initialCapacity(0xFFFF);

        if (ConfigXM.RENDER.enableQuadCacheStatistics) {
            builder = builder.recordStats();
        }

        this.cache = builder.build(new CacheLoader<BakedQuad, BakedQuad>() {

            @Override
            public @Nonnull BakedQuad load(@Nonnull BakedQuad key) throws Exception {
                return key;
            }
        });

    }

    public BakedQuad getCachedQuad(BakedQuad quadIn) {
        return this.cache.getUnchecked(quadIn);
    }
}
