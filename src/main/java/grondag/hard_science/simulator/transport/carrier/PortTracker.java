package grondag.hard_science.simulator.transport.carrier;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import grondag.exotic_matter.varia.structures.SimpleUnorderedArrayList;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.Port;

/**
 * Encapsulated set used by Carrier to
 * track ports.  Main feature in addition to set
 * functionality is maintaining a list of all
 * upward-connected bridge ports.
 */
public class PortTracker<T extends StorageType<T>>
{
    /**
     * See {@link Carrier#bridgeVersion()}
     * Don't access directly, use {@link #updateBridgeVersion()}.
     */
    private static final AtomicInteger BRIDGE_VERSION_COUNTER = new AtomicInteger(0);

    /**
     * See {@link Carrier#bridgeVersion()}
     */
    private int bridgeVersion;
    
    private final HashSet<Port<T>> ports = new HashSet<Port<T>>();
    
    /**
     * Circuit that owns this tracker.  Used to know what side
     * of bridge we are on.
     */
    private final Carrier<T> owner;

    /**
     * List of bridge ports where our owner is the external circuit.
     * These should mean that we are on the low side of the bridge,
     * because the internal circuit will belong to the bridge device.
     */
    private final SimpleUnorderedArrayList<Port<T>> bridges
        = new SimpleUnorderedArrayList<Port<T>>();
    
    /**
     * Unique upwards carrier circuits accessible from our
     * owner via bridge ports.
     */
    ImmutableSet<Carrier<T>> parents = ImmutableSet.of();
    
    public PortTracker(Carrier<T> owner)
    {
        this.owner = owner;
        this.updateBridgeVersion();
    }
    
    private void updateBridgeVersion()
    {
        this.bridgeVersion = BRIDGE_VERSION_COUNTER.incrementAndGet();
    }
    
    public boolean contains(Port<T> portInstance)
    {
        return this.ports.contains(portInstance);
    }

   
    public void add(Port<T> p)
    {
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("PortTracker.add: circuit = %d, portState = %s",
                    this.owner.carrierAddress(),
                    p.toString());
        
        assert p.internalCircuit() == this.owner 
                || p.externalCircuit() == this.owner
                : "PortTracker.add: port state circuits are bothh null or do not match this circuit.";
                    
        if(this.ports.add(p))
        {
            if(p.getMode().isBridge())
            {
                if(p.externalCircuit() == this.owner)
                {
                    HardScience.INSTANCE.info("PortTracker.add: circuit = %d, downward side - updating bridges and version",
                            this.owner.carrierAddress());
                    
                    this.bridges.addIfNotPresent(p);
                    this.updateBridgeVersion();
                    this.refreshParents();
                }
                else
                {
                    HardScience.INSTANCE.info("PortTracker.add: circuit = %d, upward side - updating version only",
                            this.owner.carrierAddress());
                    
                    // opening assertion implies internalCircuit is our owner
                    // don't need to track the bridge on the internal circuit
                    // but we do need to mark it dirty for route tracking
                    this.updateBridgeVersion();
                }
            }
        }
    }

    public void remove(Port<T> p)
    {
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("PortTracker.remove: circuit = %d, portState = %s",
                    this.owner.carrierAddress(),
                    p.toString());
        
        assert p.internalCircuit() == this.owner 
                || p.externalCircuit() == this.owner
                : "PortTracker.add: port state circuits are both null or do not match this circuit.";
        
        if(this.ports.remove(p))
        {
            
            if(p.getMode().isBridge())
            {
                if(p.externalCircuit() == this.owner)
                {
                    HardScience.INSTANCE.info("PortTracker.remove: circuit = %d, downward side - updating bridges and version",
                            this.owner.carrierAddress());
                    
                    this.bridges.removeIfPresent(p);
                    this.updateBridgeVersion();
                    this.refreshParents();
                }
                else 
                {
                    HardScience.INSTANCE.info("PortTracker.remove: circuit = %d, upward side - updating version only",
                            this.owner.carrierAddress());
                    
                    // opening assertion implies internalCircuit is our owner
                    // don't need to track the bridge on the internal circuit
                    // but we do need to mark it dirty for route tracking
                    this.updateBridgeVersion();
                }
            }
        }
    }

    public void addAll(Iterable<Port<T>> other)
    {
        other.forEach(p -> this.add(p));
    }

    /**
     * Returns immutable list of current ports.
     * Allows for iteration while ensuring all updates occur via
     * {@link #add(Port)} and {@link #remove(Port)} adhering
     * to all logic and preventing concurrent modification exception. <p>
     */
    public ImmutableList<Port<T>> snapshot()
    {
        return ImmutableList.copyOf(this.ports);
    }

    public void clear()
    {
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("PortTracker.clear: circuit = %d",
                    this.owner.carrierAddress());
        
        this.ports.clear();
        this.bridges.clear();
        this.refreshParents();
    }

    public int size()
    {
        return this.ports.size();
    }
    
    private void refreshParents()
    {
        this.parents = null;
    }
    
    /**
     * All upward carrier circuits accessible from owning carrier via bridge ports.
     */
    public ImmutableSet<Carrier<T>> parents()
    {
        if(this.parents == null)
        {
            if(this.bridges.isEmpty())
            {
                this.parents = ImmutableSet.of();
            }
            else if(this.bridges.size() == 1)
            {
                this.parents = ImmutableSet.of(this.bridges.get(0).internalCircuit());
            }
            else
            {
                ImmutableSet.Builder<Carrier<T>> builder = ImmutableSet.builder();
                this.bridges.forEach(p -> builder.add(p.internalCircuit()));
                this.parents = builder.build();
            }
        }
        return this.parents;
    }
    
    
    /**
     * See {@link Carrier#bridgeVersion()}
     */
    public int bridgeVersion()
    {
        return this.bridgeVersion;
    }
    
    /**
     * Handles implementation of {@link Carrier#mergeInto(Carrier)}
     */
    protected void mergeInto(PortTracker<T> into)
    {
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("PortTracker.mergeInto: from = %d, to = %d",
                    this.owner.carrierAddress(),
                    into.owner.carrierAddress());
        
        this.movePorts(ImmutableList.copyOf(this.ports), into);
    }
    
    /**
     * Moves ports in the given list from this tracker into the other,
     * Remove all the ports before swapping, and does all swaps before adding. 
     * If we did ports individuals then carrier group ports would no longer 
     * be associated with this circuit during removal and would fail assertion checks
     */
    private void movePorts(List<Port<T>> targets, PortTracker<T> into)
    {
        targets.forEach(p -> this.remove(p));
        
        targets.forEach(p -> p.swapCircuit(this.owner, into.owner));
        
        into.addAll(targets);
    }
    
    /**
     * Handles implementation of {@link Carrier#movePorts(Carrier, Predicate)}
     */
    public void movePorts(PortTracker<T> into, Predicate<Port<T>> predicate)
    {
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("PortTracker.movePorts: from = %d, to = %d",
                    this.owner.carrierAddress(),
                    into.owner.carrierAddress());
        
        this.movePorts(this.ports.stream().filter(predicate).collect(Collectors.toList()), into);
    }
}
