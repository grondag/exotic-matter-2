package grondag.brocade.block;

import java.util.List;

import grondag.exotic_matter.init.IBlockItemRegistrator;
import grondag.exotic_matter.model.render.RenderLayout;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.model.state.ModelStateProperty;
import grondag.exotic_matter.world.IBlockTest;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface ISuperBlock extends IBlockItemRegistrator
{
    /**
     * Used for multiple purposes depending on the type of block. Thus the generic name.
     * Didn't find the block state property abstraction layer particularly useful for my purposes.
     */
    PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    /**
     * Contains state passed from getExtendedState to handleBlockState. Using a custom unlisted property because we need large int values and the vanilla implementation enumerates
     * all allowed values into a hashmap... Plus this hides the implementation from the block.
     */
    ModelStateProperty MODEL_STATE = new ModelStateProperty();

    String getItemStackDisplayName(ItemStack stack);
    
    /**
     * Factory for block test that should be used for border/shape joins
     * for this block.  Used in model state refresh from world.
     */
    IBlockTest blockJoinTest(IBlockAccess worldIn, IBlockState state, BlockPos pos, ISuperModelState modelState);

    RenderLayout renderLayout();

    /** 
     * Returns an instance of the default model state for this block.
     * Because model states are mutable, every call returns a new instance.
     */
    ISuperModelState getDefaultModelState();

    /**
     * Number of supported meta values for this block.
     */
    int getMetaCount();

    /** 
     * If last parameter is false, does not perform a refresh from world for world-dependent state attributes.
     * Use this option to prevent infinite recursion when need to reference some static state )
     * information in order to determine dynamic world state. Block tests are main use case for false.
     * 
     * 
     */
    ISuperModelState getModelState(ISuperBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded);

    /** 
     * Use when absolutely certain given block state is current.
     */
    ISuperModelState getModelStateAssumeStateIsCurrent(IBlockState state, ISuperBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded);
    
    /**
     * Returns model state without caching the value in any way.  May use a cached value
     * to satisfy the request.<p>
     * 
     * At least one vanilla routine passes in a block state that does not match world.
     * (After block updates, passes in previous state to detect collision box changes.) <br><br>
     * 
     * We don't want to update our current state based on stale block state, so for TE
     * blocks the refresh must be coded so we don't inject bad (stale) modelState into TE. <br><br>
     * 
     * However, we do want to honor the given world state if species is different than current.
     * We do this by directly changing species, because that is only thing that can changed
     * in model state based on block state, and also affects collision box. <br><br>
     * 
     * NOTE: there is probably still a bug here, because collision box can change based
     * on other components of model state (orthogonalAxis, for example) and those changes may not be detected
     * by path finding.
     */
    ISuperModelState computeModelState(IBlockState state, ISuperBlockAccess world, BlockPos pos, boolean refreshFromWorldIfNeeded);

    int getOcclusionKey(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side);

    ItemStack getStackFromBlock(IBlockState state, IBlockAccess world, BlockPos pos);
    
    List<ItemStack> getSubItems();
    
    /**
     * Controls material-dependent properties
     */
    BlockSubstance getSubstance(IBlockState state, IBlockAccess world, BlockPos pos);

    BlockSubstance getSubstance(IBlockAccess world, BlockPos pos);
    
    /**
     * Used to assigned substance to item stacks
     */
    default BlockSubstance defaultSubstance() { return BlockSubstance.DEFAULT; }

    /** 
     * True if this is an instance of an IFlowBlock and also a filler block.
     * Avoids performance hit of casting to the IFlowBlock Interface.
     * (Based on performance profile results.)
     */
    boolean isFlowFiller();

    /** 
     * True if this is an instance of an IFlowBlock and also a height block.
     * Avoids performance hit of casting to the IFlowBlock Interface.
     * (Based on performance profile results.)
     */
    boolean isFlowHeight();

    /**
     * With {@link #isSubstanceTranslucent(IBlockState)} makes all the block
     * test methods work when full location information not available.
     * 
     * Only addresses geometry - does this block fully occupy a 1x1x1 cube?
     * True if so. False otherwise.
     */
    boolean isGeometryFullCube(IBlockState state);

    boolean isHypermatter();

    /**
     * Only true for virtual blocks.  Avoids "instanceof" checking.
     */
    default boolean isVirtual() { return false; }

    
    /**
     * True if block at the given position is actually solid (not replaceable)
     * or is virtual and visible to the given player.
     */
    default boolean isVirtuallySolid(BlockPos pos, EntityPlayer player) { return !((Block)this).isReplaceable(player.world, pos); }
    
    /**
     * Convenience call for {@link #isVirtual()} when you don't know what type of block it is.
     * Will return false for any block that doesn't implement ISuperBlock.
     */
    public static boolean isVirtualBlock(Block block)
    {
        return block instanceof ISuperBlock && ((ISuperBlock)block).isVirtual();
    }
    
    /**
     * Convenience call for {@link #isVirtuallySolid(IBlockAccess, BlockPos, EntityPlayer)} when you don't know what type of block it is.
     * Will return false for any block that doesn't implement ISuperBlock.
     */
    public static boolean isVirtuallySolidBlock(BlockPos pos, EntityPlayer player)
    {
        return isVirtuallySolidBlock(player.world.getBlockState(pos), pos, player);
    }
    
    /**
     * Convenience call for {@link #isVirtuallySolid(IBlockAccess, BlockPos, EntityPlayer)} when you don't know what type of block it is.
     * Will return negation of {@link #isReplaceable(IBlockAccess, BlockPos)} for any block that doesn't implement ISuperBlock.
     */
    public static boolean isVirtuallySolidBlock(IBlockState state, BlockPos pos, EntityPlayer player)
    {
        Block block = state.getBlock();
        return isVirtualBlock(block)
                ? ((ISuperBlock)block).isVirtuallySolid(pos, player)
                : !block.isReplaceable(player.world, pos);
    }
    
    /**
     * World-aware version called from getDrops because logic may need more than metadata.
     * Other versions (not overriden) should not be called.
     */
    int quantityDropped(IBlockAccess world, BlockPos pos, IBlockState state);
    
    ISuperBlock setAllowSilkHarvest(boolean allow);
    
    /**
     * Sets a drop other than this block if desired.
     */
    ISuperBlock setDropItem(Item dropItem);
}