package grondag.hard_science.simulator.device.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.world.PackedBlockPos;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.device.IDevice;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Tracks all extant device blocks and facilitates
 * block-related events and communications between devices.
 */
public class DeviceWorldManager
{
    private Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<IDeviceBlock>> worldBlocks
     = new Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<IDeviceBlock>>();

    private Long2ObjectOpenHashMap<IDeviceBlock> getBlocksForDimension(int dimensionID)
    {
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = worldBlocks.get(dimensionID);
        if(blocks == null)
        {
            synchronized(worldBlocks)
            {
                blocks = worldBlocks.get(dimensionID);
                if(blocks == null)
                {
                    blocks = new Long2ObjectOpenHashMap<IDeviceBlock>();
                    worldBlocks.put(dimensionID, blocks);
                }
            }
        }
        return blocks;
    }
    
    @Nullable
    public IDeviceBlock getBlockDelegate(int dimensionID, long packedBlockPos)
    {
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = this.getBlocksForDimension(dimensionID);
        return blocks == null ? null : blocks.get(packedBlockPos);
    }
    
    @Nullable
    public IDeviceBlock getBlockDelegate(World world, BlockPos pos)
    {
        return this.getBlockDelegate(world.provider.getDimension(), PackedBlockPos.pack(pos));
    }
    
    /**
     * Should be called by devices during {@link IDevice#onConnect()}
     * or whenever a connected device adds or changes a block. 
     * Tracks the device block so that it is discoverable.<p>
     * 
     * Does NOT form connections or notify neighbors of the new block. 
     * The caller is responsible for doing so.  Done this way
     * because some device blocks are inherently unable to 
     * form connections or are entirely contained within other
     * blocks owned by the same device.<p>
     * 
     * DOES notify old block of removal if a block was already present.
     * In this case, the old block is responsible for handling
     * tear down of existing connections, notifying neighbors, etc.
     * 
     */
    public void addOrUpdateDelegate(IDeviceBlock block)
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceWorldManager.addOrUpdateDelegate: " + block.description());
        
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = this.getBlocksForDimension(block.dimensionID());
        synchronized(blocks)
        {
            IDeviceBlock oldBlock = blocks.put(block.packedBlockPos(), block);
            if(oldBlock != null) oldBlock.onRemoval();
        }
    }

    /**
     * Should be called by devices during {@link IDevice#onDisconnect()}
     * or whenever a connected device removes a connection. 
     * Prior connection information is for assertion checking in test/dev env.<p>
     * 
     * DOES notify the block of removal via {@link IDeviceBlock#onRemoval()}
     * This is the signal for the old block to handle
     * tear down of existing connections, notifying neighbors, etc.
     */
    public void removeDelegate(IDeviceBlock block)
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceWorldManager.removeDelegate: " + block.description());
        
        Long2ObjectOpenHashMap<IDeviceBlock> blocks = this.getBlocksForDimension(block.dimensionID());
        synchronized(blocks)
        {
            assert blocks.remove(block.packedBlockPos()) == block
                    : "Mismatched request to remove device block";
            block.onRemoval();
        }
    }
    
    public void clear()
    {
        this.worldBlocks.clear();
    }
   
}
