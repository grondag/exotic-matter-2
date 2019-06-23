package grondag.brocade.dispatch;
//package grondag.brocade.model.varia;
//
//import java.util.List;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//
//
//
//import com.google.common.collect.ImmutableList;
//
//import grondag.brocade.Brocade;
//import grondag.brocade.legacy.block.ISuperBlock;
//import grondag.brocade.legacy.block.SuperBlockStackHelper;
//import grondag.brocade.legacy.block.SuperModelItemOverrideList;
//import grondag.brocade.legacy.render.QuadContainer;
//import grondag.brocade.legacy.render.RenderLayout;
//import grondag.fermion.cache.ObjectSimpleCacheLoader;
//import grondag.fermion.cache.ObjectSimpleLoadingCache;
//import grondag.brocade.painting.QuadPaintManager;
//import grondag.brocade.painting.SurfaceTopology;
//import grondag.brocade.primitives.QuadHelper;
//import grondag.brocade.primitives.polygon.IMutablePolygon;
//import grondag.brocade.primitives.polygon.IPolygon;
//import grondag.brocade.primitives.polygon.IStreamReaderPolygon;
//import grondag.brocade.primitives.stream.DispatchPolyStream;
//import grondag.brocade.primitives.stream.PolyStreams;
//import grondag.brocade.model.state.ISuperModelState;
//import net.minecraft.block.BlockState;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.render.model.BakedQuad;
//import net.minecraft.client.render.model.BakedModel;
//import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
//import net.minecraft.client.renderer.block.model.ItemOverrideList;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.vertex.VertexFormat;
//import net.minecraft.item.ItemStack;
//import net.minecraft.block.BlockRenderLayer;
//import net.minecraft.util.math.Direction;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.client.MinecraftForgeClient;
//import net.minecraftforge.client.model.IModel;
//import net.minecraftforge.common.model.IModelState;
//import net.minecraftforge.common.property.IExtendedBlockState;
//
//
//
//
//public class SuperDispatcher {
//    public static final SuperDispatcher INSTANCE = new SuperDispatcher();
//    public static final String RESOURCE_BASE_NAME = "super_dispatcher";
//
//    public final DispatchDelegate[] delegates;
//
//    // custom loading cache is at least 2X faster than guava LoadingCache for our
//    // use case
//    private final ObjectSimpleLoadingCache<ISuperModelState, DispatchPolyStream> modelCache = new ObjectSimpleLoadingCache<ISuperModelState, DispatchPolyStream>(
//            new BlockCacheLoader(), 0xFFFF);
//    private final ObjectSimpleLoadingCache<ISuperModelState, SimpleItemBlockModel> itemCache = new ObjectSimpleLoadingCache<ISuperModelState, SimpleItemBlockModel>(
//            new ItemCacheLoader(), 0xFFF);
//    /**
//     * contains quads for use by block damage rendering based on shape only and with
//     * appropriate UV mapping
//     */
//    private final ObjectSimpleLoadingCache<ISuperModelState, QuadContainer> damageCache = new ObjectSimpleLoadingCache<ISuperModelState, QuadContainer>(
//            new DamageCacheLoader(), 0x4FF);
//
//    private class BlockCacheLoader implements ObjectSimpleCacheLoader<ISuperModelState, DispatchPolyStream> {
//        @Override
//        public DispatchPolyStream load(ISuperModelState key) {
//            // PERF: need a way to release these when no longer needed in the cache
//            // add finalizer parameter to cache
//
//            DispatchPolyStream result = PolyStreams.claimDispatch();
//            provideFormattedQuads(key, false, result);
//            result.build();
//            return result;
//        }
//    }
//
//    private class ItemCacheLoader implements ObjectSimpleCacheLoader<ISuperModelState, SimpleItemBlockModel> {
//        @Override
//        public SimpleItemBlockModel load(ISuperModelState key) {
//            ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<BakedQuad>();
//            // PERF: create way to generate baked directly from mutable without a transient
//            // immutable poly
//            // will reduce mem allocation overhead
//            provideFormattedQuads(key, true, p -> {
//                p.addBakedQuadsToBuilder(null, builder, true);
//            });
//
//            return new SimpleItemBlockModel(builder.build(), true);
//        }
//    }
//
//    private class DamageCacheLoader implements ObjectSimpleCacheLoader<ISuperModelState, QuadContainer> {
//        @Override
//        public QuadContainer load(ISuperModelState key) {
//            QuadContainer.Builder builder = new QuadContainer.Builder(BlockRenderLayer.SOLID);
//            key.getShape().meshFactory().produceShapeQuads(key, q -> {
//                IMutablePolygon mutable = q.claimCopy();
//
//                // arbitrary choice - just needs to be a simple non-null texture
//                mutable.setTextureName(0, grondag.brocade.init.BrocadeTextures.BLOCK_COBBLE.sampleTextureName());
//
//                // Need to scale UV on non-cubic surfaces to be within a 1 block boundary.
//                // This causes breaking textures to be scaled to normal size.
//                // If we didn't do this, bigtex block break textures would appear abnormal.
//                if (mutable.getSurface().topology == SurfaceTopology.TILED) {
//                    // This is simple for tiled surface because UV scale is always 1.0
//                    mutable.setMinU(0, 0);
//                    mutable.setMaxU(0, 1);
//                    mutable.setMinV(0, 0);
//                    mutable.setMaxV(0, 1);
//                }
//
//                builder.accept(mutable.toPainted());
//                mutable.release();
//            });
//            return builder.build();
//        }
//    }
//
//    private SuperDispatcher() {
//        this.delegates = new DispatchDelegate[RenderLayout.ALL_LAYOUTS.size()];
//        for (RenderLayout layout : RenderLayout.ALL_LAYOUTS) {
//            DispatchDelegate newDelegate = new DispatchDelegate(layout);
//            this.delegates[layout.ordinal] = newDelegate;
//        }
//    }
//
//    public void clear() {
//        modelCache.clear();
//        itemCache.clear();
//    }
//
//    public int getOcclusionKey(ISuperModelState modelState, Direction face) {
//        if (!modelState.getRenderLayout().containsBlockRenderLayer(BlockRenderLayer.SOLID))
//            return 0;
//
//        return modelCache.get(modelState).getOcclusionHash(face);
//    }
//
//    private void provideFormattedQuads(ISuperModelState modelState, boolean isItem, Consumer<IPolygon> target) {
//        final QuadPaintManager paintManager = QuadPaintManager.get();
//        modelState.getShape().meshFactory().produceShapeQuads(modelState, paintManager);
//        paintManager.producePaintedQuads(modelState, isItem, target);
//    }
//
//    public BakedModel handleItemState(BakedModel originalModel, ItemStack stack) {
//        ISuperModelState key = stack.getItem() instanceof CraftingItem ? ((CraftingItem) stack.getItem()).modelState
//                : SuperBlockStackHelper.getStackModelState(stack);
//
//        return key == null ? originalModel : itemCache.get(key);
//    }
//
//    public DispatchDelegate getDelegate(ISuperBlock block) {
//        return this.delegates[block.renderLayout().ordinal];
//    }
//
//    /**
//     * Ugly but only used during load. Retrieves delegates for our custom model
//     * loader.
//     */
//    public DispatchDelegate getDelegate(String resourceString) {
//        int start = resourceString.lastIndexOf(SuperDispatcher.RESOURCE_BASE_NAME)
//                + SuperDispatcher.RESOURCE_BASE_NAME.length();
//        int index;
//        if (resourceString.contains("item")) {
//            int end = resourceString.lastIndexOf(".");
//            index = Integer.parseInt(resourceString.substring(start, end));
//        } else {
//            index = Integer.parseInt(resourceString.substring(start));
//        }
//        return this.delegates[index];
//    }
//
//    /**
//     * Delegate to use for generic crafting item rendering
//     */
//    public DispatchDelegate getItemDelegate() {
//        return this.delegates[RenderLayout.TRANSLUCENT_ONLY.ordinal];
//    }
//
//    public class DispatchDelegate implements BakedModel, IModel, IPipelinedBakedModel {
//        private final String modelResourceString;
//        private final RenderLayout blockRenderLayout;
//
//        private DispatchDelegate(RenderLayout blockRenderLayout) {
//            this.modelResourceString = Brocade.INSTANCE
//                    .prefixResource(SuperDispatcher.RESOURCE_BASE_NAME + blockRenderLayout.ordinal);
//            this.blockRenderLayout = blockRenderLayout;
//        }
//
//        /** only used for block layer version */
//        public String getModelResourceString() {
//            return this.modelResourceString;
//        }
//
//        /**
//         * For the debug renderer - enumerates all painted quads for all layers. Pass in
//         * an extended block state.
//         */
//        public void forAllPaintedQuads(IExtendedBlockState state, Consumer<IPolygon> consumer) {
//            final ISuperModelState modelState = state.getValue(ISuperBlock.MODEL_STATE);
//            final IStreamReaderPolygon reader = modelCache.get(modelState).claimThreadSafeReader();
//
//            if (reader.hasValue()) {
//                do
//                    consumer.accept(reader);
//                while (reader.next());
//            }
//
//            reader.release();
//        }
//
//        @Override
//        public boolean mightRenderInLayer(BlockRenderLayer forLayer) {
//            return forLayer == null || this.blockRenderLayout.containsBlockRenderLayer(forLayer);
//        }
//
//        private void produceQuadsInner(IStreamReaderPolygon reader, int firstAddress,
//                IPipelinedQuadConsumer quadConsumer) {
//            if (firstAddress == IPolygon.NO_LINK_OR_TAG)
//                return;
//
//            reader.moveTo(firstAddress);
//
//            do
//                quadConsumer.accept(reader);
//            while (reader.nextLink());
//        }
//
//        @Override
//        public void produceQuads(IPipelinedQuadConsumer quadConsumer) {
//            @SuppressWarnings("null")
//            final ISuperModelState modelState = ((IExtendedBlockState) quadConsumer.blockState())
//                    .getValue(ISuperBlock.MODEL_STATE);
//            final DispatchPolyStream stream = modelCache.get(modelState);
//            final IStreamReaderPolygon reader = stream.claimThreadSafeReader();
//            final BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
//
//            produceQuadsInner(reader, stream.firstAddress(layer, null), quadConsumer);
//
//            if (quadConsumer.shouldOutputSide(Direction.DOWN))
//                produceQuadsInner(reader, stream.firstAddress(layer, Direction.DOWN), quadConsumer);
//
//            if (quadConsumer.shouldOutputSide(Direction.UP))
//                produceQuadsInner(reader, stream.firstAddress(layer, Direction.UP), quadConsumer);
//
//            if (quadConsumer.shouldOutputSide(Direction.EAST))
//                produceQuadsInner(reader, stream.firstAddress(layer, Direction.EAST), quadConsumer);
//
//            if (quadConsumer.shouldOutputSide(Direction.WEST))
//                produceQuadsInner(reader, stream.firstAddress(layer, Direction.WEST), quadConsumer);
//
//            if (quadConsumer.shouldOutputSide(Direction.NORTH))
//                produceQuadsInner(reader, stream.firstAddress(layer, Direction.NORTH), quadConsumer);
//
//            if (quadConsumer.shouldOutputSide(Direction.SOUTH))
//                produceQuadsInner(reader, stream.firstAddress(layer, Direction.SOUTH), quadConsumer);
//
//            reader.release();
//        }
//
//        @Override
//        public TextureAtlasSprite getParticleTexture() {
//            // should not ever be used
//            return MinecraftClient.getMinecraft().getTextureMapBlocks().getMissingSprite();
//        }
//
//        @Override
//        public List<BakedQuad> getQuads(BlockState state, Direction side, long rand) {
//            if (state == null)
//                return QuadHelper.EMPTY_QUAD_LIST;
//
//            final ISuperModelState modelState = ((IExtendedBlockState) state).getValue(ISuperBlock.MODEL_STATE);
//
//            final BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
//
//            // If no renderIntent set then probably getting request from block breaking
//            if (layer == null) {
//                QuadContainer qc = damageCache.get(modelState.geometricState());
//                return qc.getBakedQuads(side);
//            } else {
//                final DispatchPolyStream stream = modelCache.get(modelState);
//
//                final int address = stream.firstAddress(layer, side);
//                if (address == IPolygon.NO_LINK_OR_TAG)
//                    return ImmutableList.of();
//
//                // PERF - will be bad when Acuity disabled. Maybe implement IBakedQuad interface
//                // in 1.13?
//                // Would need to finalize concurrent readers so that streams get released
//                final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
//                final IStreamReaderPolygon reader = stream.claimThreadSafeReader();
//
//                reader.moveTo(address);
//                do
//                    reader.addBakedQuadsToBuilder(layer, builder, false);
//                while (reader.nextLink());
//
//                reader.release();
//
//                return builder.build();
//            }
//        }
//
//        @Override
//        public boolean isAmbientOcclusion() {
//            return true;
//        }
//
//        @Override
//        public boolean isBuiltInRenderer() {
//            return false;
//        }
//
//        @Override
//        public boolean isGui3d() {
//            return true;
//        }
//
//        @Override
//        public ItemOverrideList getOverrides() {
//            return new SuperModelItemOverrideList(SuperDispatcher.this);
//        }
//
//        @Override
//        public ItemCameraTransforms getItemCameraTransforms() {
//            return ItemCameraTransforms.DEFAULT;
//        }
//
//        @Override
//        public BakedModel bake(IModelState state, VertexFormat format,
//                Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
//            return this;
//        }
//    }
//}