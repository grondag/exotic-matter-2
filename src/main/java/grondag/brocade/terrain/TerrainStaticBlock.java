package grondag.brocade.terrain;

import java.util.List;



import grondag.brocade.BrocadeConfig;
import grondag.brocade.block.BlockSubstance;
import grondag.brocade.block.ISuperBlock;
import grondag.brocade.block.SuperBlockStackHelper;
import grondag.brocade.block.SuperBlockWorldAccess;
import grondag.brocade.block.SuperStaticBlock;
import grondag.brocade.init.ModShapes;
import grondag.brocade.model.state.ISuperModelState;
import grondag.fermion.varia.Useful;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.World;



public class TerrainStaticBlock extends SuperStaticBlock implements IHotBlock {
    private final boolean isFiller;

    public TerrainStaticBlock(String blockName, BlockSubstance substance, ISuperModelState defaultModelState,
            boolean isFiller) {
        super(blockName, substance, defaultModelState);
        this.isFiller = isFiller;
        this.metaCount = this.isFiller ? 2 : TerrainState.BLOCK_LEVELS_INT;

        // make sure proper shape is set
        ISuperModelState modelState = defaultModelState.clone();
        modelState.setShape(this.isFiller ? ModShapes.TERRAIN_FILLER : ModShapes.TERRAIN_HEIGHT);
        this.defaultModelStateBits = modelState.serializeToInts();
    }

    /**
     * This is an egregious hack to avoid performance hit of instanceof. (Based on
     * performance profile results.) Returns true if this is a type of IFlowBlock
     */
    @Override
    public boolean isAssociatedBlock(Block other) {
        return other == TerrainBlockHelper.FLOW_BLOCK_INDICATOR || super.isAssociatedBlock(other);
    }

    @Override
    public boolean isFlowFiller() {
        return isFiller;
    }

    @Override
    public boolean isFlowHeight() {
        return !isFiller;
    }

    @Override
    
    public boolean shouldSideBeRendered(BlockState blockState, ExtendedBlockView blockAccess, BlockPos pos,
            Direction side) {
        final MutableBlockPos mpos = shouldSideBeRenderedPos.get().setPos(pos).move(side);

        // see Config.render().enableFaceCullingOnFlowBlocks for explanation
        BlockState neighborState = blockAccess.getBlockState(mpos);
        if (BrocadeConfig.RENDER.enableFaceCullingOnFlowBlocks && TerrainBlockHelper.isFlowBlock(neighborState.getBlock())) {
            int myOcclusionKey = this.getOcclusionKey(blockState, blockAccess, pos, side);
            int otherOcclusionKey = ((ISuperBlock) neighborState.getBlock()).getOcclusionKey(neighborState, blockAccess,
                    mpos, side.getOpposite());
            return myOcclusionKey != otherOcclusionKey;
        } else {
            return !neighborState.doesSideBlockRendering(blockAccess, mpos, side.getOpposite());
        }
    }

    /**
     * Very expensive to check this for terrain blocks. Only a thin layer of them on
     * top of terrain - better just to render the quads.
     */
    @Override
    public boolean doesSideBlockRendering(BlockState state, ExtendedBlockView world, BlockPos pos, Direction face) {
        return false;
    }

    @Override
    protected List<ItemStack> createSubItems() {
        List<ItemStack> items = super.createSubItems();

        for (ItemStack stack : items) {
            int meta = stack.getMetadata();
            ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(stack);
            int level = this.isFiller ? TerrainState.BLOCK_LEVELS_INT - 1 : TerrainState.BLOCK_LEVELS_INT - meta;
            int[] quadrants = new int[] { level, level, level, level };
            TerrainState flowState = new TerrainState(level, quadrants, quadrants, 0);
            modelState.setTerrainState(flowState);
            SuperBlockStackHelper.setStackModelState(stack, modelState);
        }
        return items;
    }

    @Override
    public int quantityDropped(ExtendedBlockView world, BlockPos pos, BlockState state) {
        double volume = 0;
        ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, state, pos, true);
        for (BoundingBox box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState)) {
            volume += Useful.volumeAABB(box);
        }

        return (int) Math.min(9, volume * 9);
    }

    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them
     * static.
     */
    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos,
            EntityPlayer player, boolean willHarvest) {
        TerrainDynamicBlock.freezeNeighbors(world, pos, state);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them
     * static.
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state,
            LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TerrainDynamicBlock.freezeNeighbors(worldIn, pos, state);
    }

    /**
     * Convert this block to a dynamic version of itself if one is known.
     */
    public void makeDynamic(BlockState state, World world, BlockPos pos) {
        BlockState newState = dynamicState(state, world, pos);
        if (newState != state)
            world.setBlockState(pos, newState, 3);
    }

    /**
     * Returns dynamic version of self if one is known. Otherwise returns self.
     */
    public BlockState dynamicState(BlockState state, ExtendedBlockView world, BlockPos pos) {
        Block dynamicVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getDynamicBlock(this);
        if (dynamicVersion == null || state.getBlock() != this)
            return state;

        return dynamicVersion.getDefaultState().withProperty(ISuperBlock.META, state.getValue(ISuperBlock.META));
    }

    // setting to false drops AO light value
    @Override
    public boolean isFullCube(BlockState state) {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;
    }

    @Override
    public boolean getUseNeighborBrightness(BlockState state) {
        return true;
    }

    @Override
    public int getLightOpacity(BlockState state, ExtendedBlockView world, BlockPos pos) {
        // FIXME: is this right? Retest after vertex normals are fixed
        // prevent filler blocks from blocking light to height block below
        return this.isFiller ? 0 : super.getLightOpacity(state, world, pos);
    }
}
