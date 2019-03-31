package grondag.brocade.model.render;

import java.util.List;
import java.util.function.Consumer;



import com.google.common.collect.ImmutableList;

import grondag.brocade.primitives.polygon.IPolygon;
import grondag.fermion.structures.SimpleUnorderedArrayList;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;

public class QuadContainer {
    private static IPolygon[] EMPTY_LIST = {};
    private static int[] EMPTY_COUNTS = { 0, 0, 0, 0, 0, 0 };
    public static final QuadContainer EMPTY_CONTAINER = new QuadContainer(EMPTY_LIST, EMPTY_COUNTS,
            BlockRenderLayer.SOLID);

    // Heavy usage, many instances, so using sublists of a single immutable list to
    // improve LOR
    // I didn't profile this to make sure it's worthwhile - don't tell Knuth.
    // Only populated if baked quads are requested
    protected ImmutableList<BakedQuad>[] faceLists = null;

    private int[] occlusionHash = null;

    private int[] paintedFaceIndex = new int[Direction.VALUES.length];

    private final IPolygon[] paintedQuads;

    public final BlockRenderLayer layer;

    protected QuadContainer(IPolygon[] paintedQuads, int[] paintedFaceIndex, BlockRenderLayer layer) {
        this.paintedQuads = paintedQuads;
        this.paintedFaceIndex = paintedFaceIndex;
        this.layer = layer;
    }

    @SuppressWarnings("unchecked")
    public List<BakedQuad> getBakedQuads(Direction face) {
        // build locally and don't set until end in case another thread is racing with
        // us
        ImmutableList<BakedQuad>[] faceLists = this.faceLists;

        if (faceLists == null) {
            faceLists = new ImmutableList[7];
            {
                final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
                this.forEachPaintedQuad(null, q -> q.addBakedQuadsToBuilder(layer, builder, false));
                faceLists[6] = builder.build();
            }

            for (Direction f : Direction.VALUES) {
                final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
                this.forEachPaintedQuad(f, q -> q.addBakedQuadsToBuilder(0, builder, false));
                faceLists[f.ordinal()] = builder.build();
            }

            this.faceLists = faceLists;
        }

        return face == null ? faceLists[6] : faceLists[face.ordinal()];
    }

    public void forEachPaintedQuad(Consumer<IPolygon> consumer) {
        for (IPolygon q : this.paintedQuads)
            consumer.accept(q);
    }

    public void forEachPaintedQuad(Direction face, Consumer<IPolygon> consumer) {
        int start, end;
        if (face == null) {
            start = 0;
            end = this.paintedFaceIndex[0];
        } else {
            final int n = face.ordinal();
            start = this.paintedFaceIndex[n];
            end = n == 5 ? this.paintedQuads.length : this.paintedFaceIndex[n + 1];
        }
        while (start < end) {
            consumer.accept(this.paintedQuads[start++]);
        }
    }

    public int getOcclusionHash(Direction face) {
        if (face == null)
            return 0;

        int[] occlusionHash = this.occlusionHash;

        if (occlusionHash == null) {
            occlusionHash = new int[6];
            for (int i = 0; i < 6; i++) {
                final Direction f = Direction.VALUES[i];
                occlusionHash[f.ordinal()] = computeOcclusionHash(f);
                this.occlusionHash = occlusionHash;
            }
        }

        return occlusionHash[face.ordinal()];
    }

    private int computeOcclusionHash(Direction face) {
        QuadListKeyBuilder keyBuilder = QuadListKeyBuilder.prepareThreadLocal(face);
        this.forEachPaintedQuad(face, keyBuilder);
        return keyBuilder.getQuadListKey();
    }

    public static class Builder implements Consumer<IPolygon> {
        int size = 0;

        public final BlockRenderLayer layer;

        @SuppressWarnings("unchecked")
        final SimpleUnorderedArrayList<IPolygon>[] buckets = new SimpleUnorderedArrayList[7];

        public Builder(BlockRenderLayer layer) {
            this.layer = layer;
        }

        @Override
        public void accept(IPolygon quad) {
            final Direction facing = quad.getActualFace();
            final int index = facing == null ? 6 : facing.ordinal();

            SimpleUnorderedArrayList<IPolygon> bucket = buckets[index];
            if (bucket == null) {
                bucket = new SimpleUnorderedArrayList<IPolygon>();
                buckets[index] = bucket;
            }
            bucket.add(quad);
            size++;
        }

        public QuadContainer build() {
            if (this.size == 0)
                return EMPTY_CONTAINER;

            IPolygon[] quads = new IPolygon[this.size];
            int[] indexes = new int[6];

            int i = addAndGetSize(quads, 0, buckets[6]);

            for (int j = 0; j < 6; j++) {
                indexes[j] = i;
                i += addAndGetSize(quads, i, buckets[j]);
            }

            return new QuadContainer(quads, indexes, layer);
        }

        private final int addAndGetSize(IPolygon[] targetArray, int firstOpenIndex,
                SimpleUnorderedArrayList<IPolygon> sourceList) {
            if (sourceList == null)
                return 0;
            sourceList.copyToArray(targetArray, firstOpenIndex);
            return sourceList.size();
        }
    }
}
