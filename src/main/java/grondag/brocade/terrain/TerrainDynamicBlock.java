package grondag.brocade.terrain;

import java.util.List;



import grondag.brocade.BrocadeConfig;
import grondag.brocade.block.BlockSubstance;
import grondag.brocade.block.SuperBlockWorldAccess;
import grondag.brocade.init.ModShapes;
import grondag.brocade.legacy.block.ISuperBlock;
import grondag.brocade.legacy.block.SuperBlockStackHelper;
import grondag.brocade.legacy.block.SuperSimpleBlock;
import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.varia.WorldLightOpacity;
import grondag.fermion.varia.Useful;
import grondag.fermion.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.World;



public class TerrainDynamicBlock extends SuperSimpleBlock implements IHotBlock {
    private final boolean isFiller;

    public TerrainDynamicBlock(String blockName, BlockSubstance substance, ISuperModelState defaultModelState,
            boolean isFiller) {
        super(blockName, substance, defaultModelState);
        this.isFiller = isFiller;
        this.metaCount = this.isFiller ? 2 : TerrainState.BLOCK_LEVELS_INT;

        // make sure proper shape is set
        ISuperModelState modelState = defaultModelState.clone();
        modelState.setShape(this.isFiller ? ModShapes.TERRAIN_FILLER : ModShapes.TERRAIN_HEIGHT);
        modelState.setStatic(false);
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
        // See Config.render().enableFaceCullingOnFlowBlocks for explanation
        // Exploits special case - adjacent dynamic blocks *always* cover each other's
        // faces
        // however, due to uneven render chunk updates they may not do so immediately
        // most visible on surface blocks, so always render sides if block is on a chunk
        // boundary

        if (WorldHelper.isOnRenderChunkBoundary(pos))
            return true;

        final MutableBlockPos mpos = shouldSideBeRenderedPos.get().setPos(pos).move(side);

        BlockState neighborState = blockAccess.getBlockState(mpos);

        // up face isn't reliably covered due to nature of block topology - one quadrant
        // could be cut off
        // and other could simply be flat due to differences in neighbor height and face
        // simplification
        if (side == Direction.UP && neighborState.getBlock() instanceof TerrainDynamicBlock) {
            TerrainState tState = SuperBlockWorldAccess.access(blockAccess).terrainState(neighborState, mpos);
            return !tState.isFullCube();
        }

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

    /**
     * Convert this block to a static version of itself if a static version was
     * given.
     */
    public void makeStatic(BlockState state, World world, BlockPos pos) {
        Block staticVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getStaticBlock(this);
        if (staticVersion == null || state.getBlock() != this)
            return;

        ISuperModelState myModelState = SuperBlockWorldAccess.access(world).getModelState(this, state, pos, true);
        myModelState.setStatic(true);
        world.setBlockState(pos,
                staticVersion.getDefaultState().withProperty(ISuperBlock.META, state.getValue(ISuperBlock.META)), 7);
        ((TerrainStaticBlock) staticVersion).setModelState(world, pos, myModelState);
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

    @Override
    public boolean isReplaceable(ExtendedBlockView worldIn, BlockPos pos) {
        return SuperBlockWorldAccess.access(worldIn).terrainState(pos).isEmpty();
    }

    @Override
    public boolean isAir(BlockState state, ExtendedBlockView world, BlockPos pos) {
        return SuperBlockWorldAccess.access(world).terrainState(pos).isEmpty();
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos,
            EntityPlayer player, boolean willHarvest) {
        TerrainDynamicBlock.freezeNeighbors(world, pos, state);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    // setting to false drops AO light value
    @Override
    public boolean isFullCube(BlockState state) {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;
    }

    @Override
    protected WorldLightOpacity worldLightOpacity(BlockState state) {
        return super.worldLightOpacity(state);
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state, ExtendedBlockView world, BlockPos pos) {
        return SuperBlockWorldAccess.access(world).terrainState(state, pos).isFullCube();
    }

    @Override
    public boolean getUseNeighborBrightness(BlockState state) {
        return true;
    }

    @Override
    public int getLightOpacity(BlockState state, ExtendedBlockView world, BlockPos pos) {

        /// FIXME: is this right? Retest after vertex normals are fixed
        return 0;
        // prevent filler blocks from blocking light to height block below
//        return this.isFiller ? 0 : super.getLightOpacity(state, world, pos);
    }

    /**
     * Looks for nearby dynamic blocks that might depend on this block for height
     * state and converts them to static blocks if possible.
     */
    public static void freezeNeighbors(World worldIn, BlockPos pos, BlockState state) {
        // only height blocks affect neighbors
        if (!TerrainBlockHelper.isFlowHeight(state.getBlock()))
            return;

        BlockState targetState;
        Block targetBlock;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -4; y <= 4; y++) {
//                    if(!(x == 0 && y == 0 && z == 0))
                    {
                        BlockPos targetPos = pos.add(x, y, z);
                        targetState = worldIn.getBlockState(targetPos);
                        targetBlock = targetState.getBlock();
                        if (targetBlock instanceof TerrainDynamicBlock) {
                            ((TerrainDynamicBlock) targetBlock).makeStatic(targetState, worldIn, targetPos);
                        }
                    }
                }
            }
        }
    }
}
