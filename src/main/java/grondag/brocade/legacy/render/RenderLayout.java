package grondag.brocade.legacy.render;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.fermion.structures.BinaryEnumSet;
import net.minecraft.block.BlockRenderLayer;

/**
 * Answers question how many layers a block/model can render in. Used to respond
 * to inquiries from renderer about which layers a block can render in.
 * <p>
 * 
 * Regular class to allow static access during enum initialization.
 *
 */
public class RenderLayout {
    private static final BinaryEnumSet<BlockRenderLayer> BENUMSET_BLOCK_RENDER_LAYER = new BinaryEnumSet<BlockRenderLayer>(
            BlockRenderLayer.class);

    private static final RenderLayout[] lookup = new RenderLayout[BENUMSET_BLOCK_RENDER_LAYER.combinationCount()];

    static {
        for (int i = 0; i < lookup.length; i++) {
            lookup[i] = new RenderLayout(BENUMSET_BLOCK_RENDER_LAYER.getValuesForSetFlags(i));
        }
    }

    public static ImmutableList<RenderLayout> ALL_LAYOUTS = ImmutableList.copyOf(lookup);
    public static final int COMBINATION_COUNT = BENUMSET_BLOCK_RENDER_LAYER.combinationCount();
    public static final RenderLayout SOLID_ONLY = lookup[BENUMSET_BLOCK_RENDER_LAYER
            .getFlagForValue(BlockRenderLayer.SOLID)];
    public static final RenderLayout TRANSLUCENT_ONLY = lookup[BENUMSET_BLOCK_RENDER_LAYER
            .getFlagForValue(BlockRenderLayer.TRANSLUCENT)];
    public static final RenderLayout SOLID_AND_TRANSLUCENT = lookup[BENUMSET_BLOCK_RENDER_LAYER
            .getFlagsForIncludedValues(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT)];
    public static final RenderLayout NONE = lookup[BENUMSET_BLOCK_RENDER_LAYER.getFlagsForIncludedValues()];

    /**
     * Sizes quad container
     */
    public final int blockLayerCount;

    public final int ordinal;

    public final List<BlockRenderLayer> blockLayerList;

    private RenderLayout(BlockRenderLayer... passes) {
        this.ordinal = BENUMSET_BLOCK_RENDER_LAYER.getFlagsForIncludedValues(passes);
        this.blockLayerList = ImmutableList.copyOf(passes);
        this.blockLayerCount = this.blockLayerList.size();
    }

    public final boolean containsBlockRenderLayer(BlockRenderLayer layer) {
        return BENUMSET_BLOCK_RENDER_LAYER.isFlagSetForValue(layer, this.ordinal);
    }
}
