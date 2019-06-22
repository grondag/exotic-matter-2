package grondag.brocade.terrain;

import grondag.brocade.init.ModShapes;
import grondag.brocade.model.state.ISuperModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public class TerrainDynamicBlock extends TerrainBlock {
    private final boolean isFiller;

    public TerrainDynamicBlock(Settings blockSettings, ISuperModelState defaultModelState, boolean isFiller) {
        super(blockSettings, defaultModelState);
        this.isFiller = isFiller;

        // make sure proper shape is set
        ISuperModelState modelState = defaultModelState.clone();
        modelState.setShape(this.isFiller ? ModShapes.TERRAIN_FILLER : ModShapes.TERRAIN_HEIGHT);
        modelState.setStatic(false);
        this.defaultModelStateBits = modelState.serializeToInts();
    }

    /**
     * Convert this block to a static version of itself if a static version was
     * given.
     */
    public void makeStatic(BlockState state, World world, BlockPos pos) {
        TerrainStaticBlock staticVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getStaticBlock(this);
        if (staticVersion == null || state.getBlock() != this)
            return;

        ISuperModelState myModelState = this.getModelStateAssumeStateIsCurrent(state, world, pos, true);
        myModelState.setStatic(true);
        //TODO: transfer heat block state?
        world.setBlockState(pos, staticVersion.getDefaultState().with(TerrainBlock.TERRAIN_TYPE, state.get(TerrainBlock.TERRAIN_TYPE)), 7);
        staticVersion.setModelState(world, pos, myModelState);
    }

    //TODO: restore or remove
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

  //TODO: restore or remove
//    @Override
//    public boolean isReplaceable(ExtendedBlockView worldIn, BlockPos pos) {
//        return SuperBlockWorldAccess.access(worldIn).terrainState(pos).isEmpty();
//    }
    
  //TODO: restore or remove
//    @Override
//    public boolean isAir(BlockState state, ExtendedBlockView world, BlockPos pos) {
//        return SuperBlockWorldAccess.access(world).terrainState(pos).isEmpty();
//    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        TerrainDynamicBlock.freezeNeighbors(world, pos, state);
        super.onBreak(world, pos, state, player);
    }

    /**
     * Looks for nearby dynamic blocks that might depend on this block for height
     * state and converts them to static blocks if possible.
     */
    public static void freezeNeighbors(World worldIn, BlockPos pos, BlockState state) {
        // only height blocks affect neighbors
        if (!TerrainBlockHelper.isFlowHeight(state))
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
