package grondag.brocade.terrain;

import java.util.List;

import javax.annotation.Nonnull;

import grondag.exotic_matter.ConfigXM;
import grondag.exotic_matter.block.BlockSubstance;
import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.block.SuperBlockStackHelper;
import grondag.exotic_matter.block.SuperBlockWorldAccess;
import grondag.exotic_matter.block.SuperSimpleBlock;
import grondag.exotic_matter.init.ModShapes;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.varia.WorldLightOpacity;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TerrainDynamicBlock extends SuperSimpleBlock implements IHotBlock
{
    private final boolean isFiller;
    
    public TerrainDynamicBlock(String blockName, BlockSubstance substance, ISuperModelState defaultModelState, boolean isFiller)
    {
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
     * This is an egregious hack to avoid performance hit of instanceof.
     * (Based on performance profile results.)
     * Returns true if this is a type of IFlowBlock
     */
    @Override
    public boolean isAssociatedBlock(Block other)
    {
        return other == TerrainBlockHelper.FLOW_BLOCK_INDICATOR || super.isAssociatedBlock(other);
    }

    @Override
    public boolean isFlowFiller()
    {
        return isFiller;
    }

    @Override
    public boolean isFlowHeight()
    {
        return !isFiller;
    }
  
    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        // See Config.render().enableFaceCullingOnFlowBlocks for explanation
        // Exploits special case - adjacent dynamic blocks *always* cover each other's faces
        // however, due to uneven render chunk updates they may not do so immediately
        // most visible on surface  blocks, so always render sides if block is on a chunk boundary

        if(WorldHelper.isOnRenderChunkBoundary(pos))
            return true;
        
        final MutableBlockPos mpos = shouldSideBeRenderedPos.get().setPos(pos).move(side);
        
        IBlockState neighborState = blockAccess.getBlockState(mpos);

        // up face isn't reliably covered due to nature of block topology - one quadrant could be cut off
        // and other could simply be flat due to differences in neighbor height and face simplification
        if(side == EnumFacing.UP && neighborState.getBlock() instanceof TerrainDynamicBlock)
        {
            TerrainState tState = SuperBlockWorldAccess.access(blockAccess).terrainState(neighborState, mpos);
                return !tState.isFullCube();
        }
        
        if(ConfigXM.RENDER.enableFaceCullingOnFlowBlocks && TerrainBlockHelper.isFlowBlock(neighborState.getBlock()))
        {
            int myOcclusionKey = this.getOcclusionKey(blockState, blockAccess, pos, side);
            int otherOcclusionKey = ((ISuperBlock)neighborState.getBlock()).getOcclusionKey(neighborState, blockAccess, mpos, side.getOpposite());
            return myOcclusionKey != otherOcclusionKey;
        }
        else
        {
            return !neighborState.doesSideBlockRendering(blockAccess, mpos, side.getOpposite());
        }
    }

    /**
     * Very expensive to check this for terrain blocks.  Only a thin layer of them on top of terrain - better just to render the quads.
     */
    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        return false;
    }
    
    @Override
    protected List<ItemStack> createSubItems()
    {
        List<ItemStack> items = super.createSubItems();
        
        for(ItemStack stack : items)
        {
            int meta = stack.getMetadata();
            ISuperModelState modelState = SuperBlockStackHelper.getStackModelState(stack);
            int level = this.isFiller ? TerrainState.BLOCK_LEVELS_INT - 1 : TerrainState.BLOCK_LEVELS_INT - meta;
            int [] quadrants = new int[] {level, level, level, level};
            TerrainState flowState = new TerrainState(level, quadrants, quadrants, 0);
            modelState.setTerrainState(flowState);
            SuperBlockStackHelper.setStackModelState(stack, modelState);
        }
        return items;
    }

    /**
     * Convert this block to a static version of itself if a static version was given.
     */
    public void makeStatic(IBlockState state, World world, BlockPos pos)
    {
        Block staticVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getStaticBlock(this);
        if(staticVersion == null || state.getBlock() != this) return;

        ISuperModelState myModelState = SuperBlockWorldAccess.access(world).getModelState(this, state, pos, true);
        myModelState.setStatic(true);
        world.setBlockState(pos, staticVersion.getDefaultState()
                .withProperty(ISuperBlock.META, state.getValue(ISuperBlock.META)), 7);
        ((TerrainStaticBlock)staticVersion).setModelState(world, pos, myModelState);
    }
    
    @Override
    public int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state)
    {
        double volume = 0;
        ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, state, pos, true);
        for(AxisAlignedBB box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState))
        {
            volume += Useful.volumeAABB(box);
        }

        return (int) Math.min(9, volume * 9);
    }
 
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return SuperBlockWorldAccess.access(worldIn).terrainState(pos).isEmpty();
    }

    @Override
    public boolean isAir(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        return SuperBlockWorldAccess.access(world).terrainState(pos).isEmpty();
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest)
    {
        TerrainDynamicBlock.freezeNeighbors(world, pos, state);
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    // setting to false drops AO light value
    @Override
    public boolean isFullCube(IBlockState state)
    {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;    
    }
    

    @Override
    protected WorldLightOpacity worldLightOpacity(IBlockState state)
    {
        return super.worldLightOpacity(state);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        // don't have enough information without world access or extended state
        // to determine if is full cube.
        return false;
    }
    
    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return SuperBlockWorldAccess.access(world).terrainState(state, pos).isFullCube();
    }
    
    @Override
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        return true;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        
        /// FIXME: is this right?  Retest after vertex normals are fixed
        return 0;
        // prevent filler blocks from blocking light to height block below
//        return this.isFiller ? 0 : super.getLightOpacity(state, world, pos);
    }
    
    /** 
     * Looks for nearby dynamic blocks that might depend on this block for height state
     * and converts them to static blocks if possible. 
     */
    public static void freezeNeighbors(World worldIn, BlockPos pos, IBlockState state)
    {
        //only height blocks affect neighbors
        if(!TerrainBlockHelper.isFlowHeight(state.getBlock())) return;
                
        IBlockState targetState;
        Block targetBlock;
        
        for(int x = -2; x <= 2; x++)
        {
            for(int z = -2; z <= 2; z++)
            {
                for(int y = -4; y <= 4; y++)
                {
//                    if(!(x == 0 && y == 0 && z == 0))
                    {
                        BlockPos targetPos = pos.add(x, y, z);
                        targetState = worldIn.getBlockState(targetPos);
                        targetBlock = targetState.getBlock();
                        if(targetBlock instanceof TerrainDynamicBlock)
                        {
                            ((TerrainDynamicBlock)targetBlock).makeStatic(targetState, worldIn, targetPos);
                        }
                    }
                }
            }
        }
    }
}
