package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.world.PackedBlockPos;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.device.DeviceManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.endpoint.IPortLayout;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.util.EnumFacing;

/**
 * Basic combined device block & block manager implementation for single-block machines.
 */
public class SimpleBlockHandler implements IDeviceBlock, IDeviceBlockManager, IDeviceComponent
{
    private final IDevice owner;
    private final long packedBlockPos;
    
    private BlockPortManager<StorageTypeStack> itemPortManager;
    private BlockPortManager<StorageTypePower> powerPortManager;
    private BlockPortManager<StorageTypeFluid> fluidPortManager;
    
    /**
     * Sets up ports and parents.
     * Assumes device block position is given by device location. 
     * Thus should not be called until device has a location.<p>
     * 
     * Channel will be used as the channel ID for any block or item
     * ports that rely on it for carrier segregation. (Fluid ports
     * segregate using channels derived from fluid carried.)<p>
     * 
     * PortLayout should already be transformed (if necessary)
     * so that port facings are actual, in-world facings instead
     * of nominal facing of the port layout for this device.
     */
    public SimpleBlockHandler(IDevice owner, IPortLayout portLayout)
    {
        this.owner = owner;
        this.packedBlockPos = PackedBlockPos.pack(owner.getLocation());
        this.fluidPortManager = BlockPortManager.create(portLayout.createFluidPorts(owner));
        this.itemPortManager = BlockPortManager.create(portLayout.createItemPorts(owner));
        this.powerPortManager = BlockPortManager.create(portLayout.createPowerPorts(owner));
    }
    
    @Override
    public Collection<IDeviceBlock> blocks()
    {
        return ImmutableList.of(this);
    }

    @Override
    public void connect()
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("SimpleBlockHandler.connect: " + this.description());

        DeviceManager.blockManager().addOrUpdateDelegate(this);
        this.onAdded();
    }

    protected void onAdded()
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("SimpleBlockHandler.onAdded: " + this.description());

        if(   this.itemPortManager.isEmpty()
           && this.powerPortManager.isEmpty()
           && this.fluidPortManager.isEmpty()) return;
        
        // build list of adjacent device blocks and the face
        // to which they are adjacent
        ImmutableList.Builder<Pair<IDeviceBlock, EnumFacing>> builder = ImmutableList.builder();
        for(EnumFacing face : EnumFacing.VALUES)
        {
            IDeviceBlock neighbor = this.getNeighbor(face);
            if(neighbor != null)
            {
                builder.add(Pair.of(neighbor, face));
            }
        }
        ImmutableList<Pair<IDeviceBlock, EnumFacing>> neighbors = builder.build();
        
        if(neighbors.isEmpty()) return;
        
        this.doConnect(itemPortManager, LogisticsService.ITEM_SERVICE, neighbors);
        this.doConnect(powerPortManager, LogisticsService.POWER_SERVICE, neighbors);
        this.doConnect(fluidPortManager, LogisticsService.FLUID_SERVICE, neighbors);
    }

    private <T extends StorageType<T>> void doConnect(
            @Nullable BlockPortManager<T> myPortManager,
            LogisticsService<T> service,
            ImmutableList<Pair<IDeviceBlock, EnumFacing>> neighbors)
    {
        if(myPortManager.isEmpty()) return;
        
        service.executor.execute(()->
        {
            for(Pair<IDeviceBlock, EnumFacing> n : neighbors)
            {
                EnumFacing face = n.getValue();
                
                for(Port<T> myPort : myPortManager.getPorts(face))
                {
                    // this is always called when this block is new
                    // so none of my ports should be connected yet
                    if(myPort.isAttached())
                    {
                        assert false : "Found attached port during port connection - skipping";
                        continue;
                    }
                    
                    for(Port<T> matePort : 
                        n.getLeft().getConnectablePorts(
                                myPort, face.getOpposite()))
                    {
                        if(service.connect(myPort, matePort)) break;
                    }
                }
            }
        }, false);
    }
    
    @Override
    public void disconnect()
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("SimpleBlockHandler.disconnect: " + this.description());
        
        // NB: will call back to onRemoval(), which contains logic 
        // for breaking connections
        DeviceManager.blockManager().removeDelegate(this);        
    }

    @Override
    public long packedBlockPos()
    {
        return this.packedBlockPos;
    }

    @Override
    public int dimensionID()
    {
        return this.owner.getLocation().dimensionID();
    }

    @Override
    public IDevice device()
    {
        return this.owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends StorageType<T>> BlockPortManager<T> portManager(T storageType)
    {
        switch(storageType.enumType)
        {
        case FLUID:
            return (BlockPortManager<T>) this.fluidPortManager;
            
        case ITEM:
            return (BlockPortManager<T>) this.itemPortManager;
            
        case POWER:
            return (BlockPortManager<T>) this.powerPortManager;
            
        case PRIVATE:
            assert false : "Reference to private storage type";
            return null;
        default:
            assert false : "Unhandled enum mapping";
            return null;
        }
    }
        
    @Override
    public <T extends StorageType<T>> List<Port<T>> getAttachedPorts(T storageType)
    {
        return this.portManager(storageType).getAttachedPorts();
    }

    @Override
    public @Nullable <T extends StorageType<T>> Iterable<Port<T>> getConnectablePorts(@Nonnull Port<T> fromPort, EnumFacing actualFace)
    {
        return this.portManager(fromPort.storageType())
                .getConnectablePorts(fromPort, actualFace);
    }
    
    @Override
    public void onRemoval()
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("SimpleBlockHandler.onRemoval: " + this.description());
        
        this.doDisconnect(this.itemPortManager, LogisticsService.ITEM_SERVICE);
        this.doDisconnect(this.powerPortManager, LogisticsService.POWER_SERVICE);
        this.doDisconnect(this.fluidPortManager, LogisticsService.FLUID_SERVICE);
    }
    
    private <T extends StorageType<T>> void doDisconnect(
            @Nullable BlockPortManager<T> portManager,
            LogisticsService<T> service)
    {
        if(portManager.isEmpty()) return;
        
        service.executor.execute(() -> 
        {
            for(Port<T> ps : portManager.getAttachedPorts())
            {
                service.disconnect(ps);
            }
        }, false);
    }

}
