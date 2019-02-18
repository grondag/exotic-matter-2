package grondag.brocade.terrain;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.block.ISuperBlockAccess;
import grondag.exotic_matter.world.PackedBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TerrainBlockHelper 
{
    public static final Block FLOW_BLOCK_INDICATOR = new Block(Material.AIR);
    
    /**
     * Convenience method to check for flow block. 
     */
    public static boolean isFlowBlock(Block block)
    {
        return block.isAssociatedBlock(FLOW_BLOCK_INDICATOR);
    }
    
    /**
     * Convenience method to check for filler block. 
     */
    public static boolean isFlowFiller(Block block)
    {
        return block.isAssociatedBlock(FLOW_BLOCK_INDICATOR) && ((ISuperBlock)block).isFlowFiller();
    }
    
    /**
     * Convenience method to check for height block. 
     */
    public static boolean isFlowHeight(Block block)
    {
        return block.isAssociatedBlock(FLOW_BLOCK_INDICATOR) && ((ISuperBlock)block).isFlowHeight();
    }
    
    public static int getHotness(Block block)
    {
        return block.isAssociatedBlock(FLOW_BLOCK_INDICATOR)
                ? ((IHotBlock)block).heatLevel()
                : 0;
    }
    
    /**
     * Use for height blocks.
     * Returns a value from 1 to 12 to indicate the center height of this block.
     * Returns zero if not a flow block.
     */
    public static int getFlowHeightFromState(IBlockState state)
    {
        if(isFlowHeight(state.getBlock()))
        {
            return Math.max(1, TerrainState.BLOCK_LEVELS_INT - state.getValue(ISuperBlock.META));
        }
        else
        {
            return 0;
        }
        
    }
    
    /** 
     * Use for height blocks.
     * Stores a value from 1 to 12 to indicate the center height of this block 
     */
    public static IBlockState stateWithDiscreteFlowHeight(IBlockState state, int value)
    {
        return state.withProperty(ISuperBlock.META, Math.min(TerrainState.BLOCK_LEVELS_INT - 1, Math.max(0, TerrainState.BLOCK_LEVELS_INT - value)));
    }

    public static IBlockState stateWithFlowHeight(IBlockState state, float value)
    {
        return stateWithDiscreteFlowHeight(state, (int) Math.round(value * TerrainState.BLOCK_LEVELS_INT));
    }

    /**
     * Shorthand for {@link #adjustFillIfNeeded(World, BlockPos, Predicate)} with no predicate.
     */
    @Nullable
    public static IBlockState adjustFillIfNeeded(TerrainWorldAdapter worldObj, long packedBasePos)
    {
        return adjustFillIfNeeded(worldObj, packedBasePos, null);
    }
    
    /**
     * Adds or removes a filler block if needed.
     * Also replaces static filler blocks with dynamic version.
     * Returns the blockstate that was set if it was changed.<p>
     * 
     * Optional predicate can limit what block states are updated.
     * If provided, only block states where predicate test returns
     * true will be changed.
     * 
     */
    @Nullable
    public static IBlockState adjustFillIfNeeded(TerrainWorldAdapter worldObj, final long packedBasePos, @Nullable Predicate<IBlockState> filter)
    {
        final IBlockState baseState = worldObj.getBlockState(packedBasePos);
        final Block baseBlock = baseState.getBlock();
        
        if(TerrainBlockHelper.isFlowHeight(baseBlock)) return null;
        
        // Checks for non-displaceable block
        if(filter != null && !filter.test(baseState)) return null;
        
        final int SHOULD_BE_AIR = -1;
        
        ISuperBlock fillBlock = null;

        IBlockState update = null; 
                
        int targetMeta = SHOULD_BE_AIR;
        
        /**
         * If space is occupied with a non-displaceable block, will be ignored.
         * Otherwise, possible target states: air, fill +1, fill +2
         * 
         * Should be fill +1 if block below is a heightblock and needs a fill >= 1;
         * Should be a fill +2 if block below is not a heightblock and block
         * two below needs a fill = 2;
         * Otherwise should be air.
         */
        final long posBelow = PackedBlockPos.down(packedBasePos,  1);
        final IBlockState stateBelow = worldObj.getBlockState(posBelow);
        
        if(isFlowHeight(stateBelow.getBlock()) 
                && worldObj.terrainState(stateBelow, posBelow).topFillerNeeded() > 0)
        {
            targetMeta = 0;

            fillBlock = (ISuperBlock) TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateBelow.getBlock());
        }
        else 
        {
            final long posTwoBelow = PackedBlockPos.down(packedBasePos,  2);
            final IBlockState stateTwoBelow = worldObj.getBlockState(posTwoBelow);
            
            if((isFlowHeight(stateTwoBelow.getBlock()) 
                    && worldObj.terrainState(stateTwoBelow, posTwoBelow).topFillerNeeded() == 2))
            {
                targetMeta = 1;
                fillBlock = (ISuperBlock) TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateTwoBelow.getBlock());
            }
        }

        if(isFlowFiller(baseBlock))
        {
            if(targetMeta == SHOULD_BE_AIR)
            {
                update = Blocks.AIR.getDefaultState();
                worldObj.setBlockState(packedBasePos, update);
            }
            else if(fillBlock != null && (baseState.getValue(ISuperBlock.META) != targetMeta || baseBlock != fillBlock))
            {
                update = ((Block)fillBlock).getDefaultState().withProperty(ISuperBlock.META, targetMeta);
                worldObj.setBlockState(packedBasePos, update);
            }
            //confirm filler needed and adjustIfEnabled/remove if needed
        }
        else if(targetMeta != SHOULD_BE_AIR && fillBlock != null)
        {
            update = ((Block)fillBlock).getDefaultState().withProperty(ISuperBlock.META, targetMeta);
            worldObj.setBlockState(packedBasePos, update);
        }
        
        return update;
    }
    
    /** 
     * Returns true of geometry of flow block should be a full cube based on self and neighboring flow blocks.
     * Returns false if otherwise or if is not a flow block. 
     */
    public static boolean shouldBeFullCube(IBlockState blockState, ISuperBlockAccess blockAccess, BlockPos pos)
    {
        return blockAccess.terrainState(blockState, pos).isFullCube();
    }
    
    /**
     * Use for filler blocks.
     * Returns values from +1 to +2.
     */
    public static int getYOffsetFromState(IBlockState state)
    {
        return state.getValue(ISuperBlock.META) + 1;
    }
    
    /**
     * Use for filler blocks.
     * Stores values from -2 to -1 and +1 to +2.
     */
    public static IBlockState stateWithYOffset(IBlockState state, int value)
    {
        return state.withProperty(ISuperBlock.META, Math.min(1, Math.max(0, value - 1)));
    }
}
