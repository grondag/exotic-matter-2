package grondag.hard_science.simulator.device.blocks;

import java.util.Comparator;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.endpoint.Port;
import net.minecraft.util.EnumFacing;

/**
 * Container for ports within a device block.
 * Port arrangement is immutable after construction.
 * If port arrangement changes, all ports will need
 * to be disconnected and port manager rebuilt.<p>
 * 
 * Note that all face values are expected to 
 * be <em>actual</em> face.  The device block 
 * should transform nominal faces to actual faces
 * before initializing the port manager.<p>
 */
public class BlockPortManager<T extends StorageType<T>>
{
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final BlockPortManager<?> EMPTY_MANAGER = new BlockPortManager(ImmutableList.of());
    
    private final ImmutableList<Port<T>> allPorts;
    
    @SuppressWarnings("unchecked")
    private final ImmutableList<Port<T>>[] facePorts = new ImmutableList[6];
    
    @SuppressWarnings("unchecked")
    public static <V extends StorageType<V>> BlockPortManager<V> create(ImmutableList<Port<V>> ports)
    {
        return ports.isEmpty()
                ? (BlockPortManager<V>) EMPTY_MANAGER
                : new BlockPortManager<V>(ports);
    }
    
    private BlockPortManager(ImmutableList<Port<T>> ports)
    {
        if(!ports.isEmpty())
        {
            allPorts = ImmutableList.sortedCopyOf(
                    new Comparator<Port<T>>(){
                    @Override
                    public int compare(@Nullable Port<T> o1, @Nullable Port<T> o2)
                    {
                        return Integer.compare(o1.getFace().ordinal(), o2.getFace().ordinal());
                    }}, 
                    ports);
            
            EnumFacing lastFace = allPorts.get(0).getFace();
            int lastStart = 0;
            
            for(int i = 0; i < allPorts.size(); i++)
            {
                Port<T> port = allPorts.get(i);
                if(lastFace != port.getFace())
                {
                    facePorts[lastFace.ordinal()] = allPorts.subList(lastStart, i);
                    lastFace = port.getFace();
                    lastStart = i;
                }
            }
            facePorts[lastFace.ordinal()] = allPorts.subList(lastStart, allPorts.size());
        }
        else
        {
            allPorts = ImmutableList.of();
        }
        
        for(EnumFacing face : EnumFacing.VALUES)
        {
            if(facePorts[face.ordinal()] == null)
                facePorts[face.ordinal()] = ImmutableList.of();
        }
    }
    
    public ImmutableList<Port<T>> getAttachedPorts()
    {
        return allPorts
                .stream()
                .filter(p -> p.isAttached())
                .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Retrieves all ports on the given face.
     * Used in conjunction with {@link #getConnectablePorts(int, CarrierLevel, EnumFacing)}
     * to form connections between adjacent faces of a device block.<p>
     */
    public ImmutableList<Port<T>> getPorts(EnumFacing face)
    {
        return this.facePorts[face.ordinal()];
    }
    
    /**
     * Type-specific implementation for
     * {@link IDeviceBlock#getConnectablePorts(Port, EnumFacing)}<p>
     */
    public ImmutableList<Port<T>> getConnectablePorts(Port<T> fromPort, EnumFacing face)
    {
        return this.getPorts(face).stream()
                .filter(p -> p.couldAttach(fromPort))
                .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * True if no ports
     */
    public boolean isEmpty()
    {
        return this.allPorts.isEmpty();
    }
}
