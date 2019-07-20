package grondag.hard_science.simulator.transport.endpoint;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.carrier.Channel;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

/**
 * Immutable set of ports on a single face of a block.
 * Instances returned with the same set of ports are 
 * guaranteed to be the same instance and can thus be
 * compared using an == test.
 */
public class PortFace extends ForwardingSet<PortDescription<?>>
{
    private static HashMap<BitSet, PortFace> faces = new HashMap<BitSet, PortFace>();
    
    public static PortFace find(PortDescription<?>... ports)
    {
        BitSet bits = new BitSet();
        for(PortDescription<?> p : ports)
        {
            bits.set(p.transientID());
        }
        
        PortFace result = faces.get(bits);
        if(result == null)
        {
            synchronized(faces)
            {
                result = faces.get(bits);
                if(result == null)
                {
                    result = new PortFace(ports);
                    faces.put(bits, result);
                }
            }
        }
        return result;
    }
    
    /**
     * Maps device channels to other instances.
     * Will be null if this port faces has no device channel ports.
     * See {@link #withDeviceChannel(int)}
     */
    private final PortFace[] withDeviceChannel;
    
    private final ImmutableSet<PortDescription<?>> wrapped;
    
    private final ImmutableList<PortDescription<?>> itemPorts;
    
    private final ImmutableList<PortDescription<?>> fluidPorts;
    
    private final ImmutableList<PortDescription<?>> powerPorts;

    /**
     * Caches the results of {@link #couldConnectWith(PortFace)}
     * There are certainly more space-efficient ways to do this, 
     * but this is simple and fast.
     */
    private final Object2BooleanOpenHashMap<PortFace> connectCache = new Object2BooleanOpenHashMap<PortFace>();
 
    
    @SuppressWarnings("unchecked")
    private PortFace(PortDescription<?>... ports)
    {
        ImmutableList.Builder<PortDescription<?>> itemBuilder = ImmutableList.builder();
        ImmutableList.Builder<PortDescription<?>> fluidBuilder = ImmutableList.builder();
        ImmutableList.Builder<PortDescription<?>> powerBuilder = ImmutableList.builder();
        
        boolean needsDeviceMap = false;
        for(PortDescription<?> p : ports)
        {
            needsDeviceMap = needsDeviceMap || p.getChannel() == Channel.CONFIGURABLE_FOLLOWS_DEVICE;
            switch(p.storageType().enumType)
            {
            case FLUID:
                fluidBuilder.add((PortDescription<StorageTypeFluid>) p);
                break;
            case ITEM:
                itemBuilder.add((PortDescription<StorageTypeStack>) p);
                break;
            case POWER:
                powerBuilder.add((PortDescription<StorageTypePower>) p);
                break;
                
            case PRIVATE:
                assert false : "Port references private storage type";
                break;

            default:
                assert false : "Missing enum mapping.";
                break;
            
            }
        }
        
        this.itemPorts = itemBuilder.build();
        this.fluidPorts = fluidBuilder.build();
        this.powerPorts = powerBuilder.build();
        
        this.withDeviceChannel = needsDeviceMap ? new PortFace[16] : null;
        
        this.wrapped = ImmutableSet.copyOf(ports);
    }

    @Override
    protected Set<PortDescription<?>> delegate()
    {
        return wrapped;
    }
    
    /**
     * If this port face contains power or item ports
     * that are keyed to a device channel, returns the 
     * port face that results from setting those ports
     * to a specific channel. Power/Item device channels
     * equate to species, and range from 0-15.<p>
     * 
     * If this port face does not contain any ports
     * keyed to the device channel (in other words, represents
     * a face where those ports have already been set to 
     * the device channel) then returns self.<p>
     * 
     * Instances returned from this method will be the same 
     * instance that would be returned from {@link #find(PortDescription...)}.
     */
    public PortFace withDeviceChannel(int deviceChannel)
    {
        if(withDeviceChannel == null) return this;
        
        PortFace result = withDeviceChannel[deviceChannel];
        if(result == null)
        {
            synchronized(withDeviceChannel)
            {
                result = withDeviceChannel[deviceChannel];
                if(result == null)
                {
                    PortDescription<?>[] newPorts = this.toArray(new PortDescription[this.size()]);
                    for(int i = 0; i < newPorts.length; i++)
                    {
                        if(newPorts[i].getChannel() == Channel.CONFIGURABLE_FOLLOWS_DEVICE)
                        {
                            newPorts[i] = newPorts[i].withChannel(deviceChannel);
                        }
                    }
                    result = find(newPorts);
                    withDeviceChannel[deviceChannel] = result;
                }
            }
        }
        return result;
    }
    
    /**
     * True if this layout has at least one port on the given
     * face that could connect with other face provided, assuming
     * the faces are adjacent.
     */
    public boolean couldConnectWith(PortFace other)
    {
        if(this.connectCache.containsKey(other))
        {
            return this.connectCache.getBoolean(other);
        }
        else
        {
            boolean result = couldConnectWithImpl(other);
            this.connectCache.put(other, result);
            return result;
        }   
    }
    
    /**
     * Dumb and slow but result is always cached so whatever.
     */
    private boolean couldConnectWithImpl(PortFace other)
    {
        if(this.isEmpty() || other.isEmpty()) return false;
        for(PortDescription<?> fromPort : this)
        {
            for(PortDescription<?> toPort : other)
            {
                if(fromPort.couldAttach(toPort)) return true;
            }
        }
        return false;
    }
    
    public ImmutableList<PortDescription<?>> ports(StorageType<?> storageType)
    {
        switch(storageType.enumType)
        {
        case FLUID:
            return this.fluidPorts;
            
        case ITEM:
            return this.itemPorts;
            
        case POWER:
            return this.powerPorts;

        default:
            return ImmutableList.of();
        
        }
    }
}
