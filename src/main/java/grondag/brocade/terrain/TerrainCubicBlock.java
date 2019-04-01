package grondag.brocade.terrain;

import java.util.List;



import grondag.brocade.block.BlockSubstance;
import grondag.brocade.block.SuperSimpleBlock;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ExtendedBlockView;

public class TerrainCubicBlock extends SuperSimpleBlock implements IHotBlock {

    public TerrainCubicBlock(String blockName, BlockSubstance substance, ISuperModelState defaultModelState) {
        super(blockName, substance, defaultModelState);
        this.metaCount = 1;
    }

    /**
     * This is an egregious hack to avoid performance hit of instanceof. (Based on
     * performance profile results.) Returns true if this is a type of IFlowBlock
     */
    @Override
    public boolean isAssociatedBlock(Block other) {
        return other == TerrainBlockHelper.FLOW_BLOCK_INDICATOR || super.isAssociatedBlock(other);
    }

    // allow mined blocks to stack - consistent with appearance of a full-height
    // block
    @Override
    public int damageDropped(BlockState state) {
        return 0;
    }

    // allow mined blocks to stack - don't put an NBT on them
    @Override
    public ItemStack getStackFromBlock(BlockState state, ExtendedBlockView world, BlockPos pos) {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.damageDropped(state));
    }

    @Override
    protected List<ItemStack> createSubItems() {
        return this.defaultSubItems();
    }

    @Override
    public boolean isFlowFiller() {
        return false;
    }

    @Override
    public boolean isFlowHeight() {
        return true;
    }

    @Override
    public boolean shouldSideBeRendered(BlockState blockState, ExtendedBlockView blockAccess, BlockPos pos,
            Direction side) {
        final MutableBlockPos mpos = shouldSideBeRenderedPos.get().setPos(pos).move(side);
        BlockState neighborState = blockAccess.getBlockState(mpos);
        return !neighborState.doesSideBlockRendering(blockAccess, mpos, side.getOpposite());
    }

    @Override
    public int quantityDropped(ExtendedBlockView world, BlockPos pos, BlockState state) {
        return 1;
    }

    @Override
    public boolean isReplaceable(ExtendedBlockView worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isAir(BlockState state, ExtendedBlockView world, BlockPos pos) {
        return false;
    }

}
