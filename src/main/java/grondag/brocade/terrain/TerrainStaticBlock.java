package grondag.brocade.terrain;

import grondag.brocade.init.ModShapes;
import grondag.brocade.legacy.block.SuperBlockPlus;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.World;



public class TerrainStaticBlock extends SuperBlockPlus implements IHotBlock {
    private final boolean isFiller;

    public TerrainStaticBlock(Settings blockSettings, ISuperModelState defaultModelState, BlockEntityType<?> blockEntityType, boolean isFiller) {
        super(blockSettings, defaultModelState, blockEntityType);
        this.isFiller = isFiller;

        // make sure proper shape is set
        ISuperModelState modelState = defaultModelState.clone();
        modelState.setShape(this.isFiller ? ModShapes.TERRAIN_FILLER : ModShapes.TERRAIN_HEIGHT);
        this.defaultModelStateBits = modelState.serializeToInts();
    }

    @Override
    public boolean isFlowFiller() {
        return isFiller;
    }

    @Override
    public boolean isFlowHeight() {
        return !isFiller;
    }
    
//  TODO: remove or restore
//    @Override
//    public int quantityDropped(ExtendedBlockView world, BlockPos pos, BlockState state) {
//        double volume = 0;
//        ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, state, pos, true);
//        for (BoundingBox box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState)) {
//            volume += Useful.volumeAABB(box);
//        }
//
//        return (int) Math.min(9, volume * 9);
//    }

    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them
     * static.
     */
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        TerrainDynamicBlock.freezeNeighbors(world, pos, state);
        super.onBreak(world, pos, state, player);
    }

    /**
     * Prevent neighboring dynamic blocks from updating geometry by making them
     * static.
     */
    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
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
        //TODO: transfer heat block state?
        return dynamicVersion.getDefaultState().with(TerrainBlock.TERRAIN_TYPE, state.get(TerrainBlock.TERRAIN_TYPE));
    }
}
