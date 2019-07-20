package grondag.hard_science.simulator.transport.carrier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import grondag.exotic_matter.simulator.Simulator;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.StoragePacket;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.endpoint.PortMode;
import grondag.hard_science.simulator.transport.routing.Legs;

/**
 * Represents physically (perhaps wirelessly) connected 
 * transport media shared by multiple port instances. 
 * Is pathway for actual transport. All ports on the circuit
 * must be of the same type and channel, and must be
 * connected in a way appropriate for the carrier type.<p>
 * 
 * References to a Carrier are held by the ports
 * on a DeviceBlock or (for wireless device) directly by
 * a device.  When a port becomes connected to another ports
 * with a compatible carrier, the port obtains a shared 
 * reference to the carrier circuit formed by connecting 
 * with other ports. (Or a new, isolated circuit if 
 * there are only two ports so far.)<p>
 * 
 * Methods related to network topology should ONLY be
 * called from the connection manager thread and will 
 * throw an exception if called otherwise.  This avoids
 * the need for synchronization of these methods.
 *
 */
public class Carrier<T extends StorageType<T>> implements ITypedStorage<T>
{
    public final int channel;

    private final PortTracker<T> ports;
    
    private Legs<T> legs = null;
    
    private static final AtomicInteger nextAddress = new AtomicInteger(1);
    
    private final int carrierAddress = nextAddress.getAndIncrement();
    
    protected long lastTickSeen = -1;
    
    protected long utilization = 0;
    
    /**
     * Set to false when a circuit is discarded as network
     * structure changes.  Circuit will no longer transmit
     * once it is made invalid.  Allows {@link #consumeCapacity(StoragePacket, boolean)}
     * to run on any thread and/or with stale routes and 
     * still remain relatively consistent with network topology.<p>
     * 
     * Worst case, the route won't actually be valid because the 
     * device moved but all the circuits would still be live
     * and will probably happen so soon after change that player
     * won't notice any discrepancy.
     */
    private boolean isValid = true;
    
    private final T storageType;
    
    private final CarrierLevel level;
    
    private final long capacityPerTick;
    
//    /**
//     * Collection of nodes on this carrier keyed by device ID.
//     * Relies on the fact that all ports for a given device
//     * on the same carrier have the same node.  The extra ports
//     * are only used for fault tolerance.
//     */
//    private final Int2ObjectOpenHashMap<TransportNode> nodesByDeviceID
//        = new Int2ObjectOpenHashMap<TransportNode>();
    
    public Carrier(T storageType, CarrierLevel level, int channel)
    {
        this.ports = new PortTracker<T>(this);
        this.storageType = storageType;
        this.level = level;
        this.channel = Channel.channelOverride(channel, level, storageType);
        this.capacityPerTick = storageType.transportCapacity(level, channel);
    }

    /**
     * Transient unique identifier for network links/buses/gateways. 
     * Not type-specific, not persisted. 
     * Immutable in game session unless or until physical media structure
     * is disrupted. <p>
     * 
     * Used to build dynamically-constructed routing information.
     */
    public int carrierAddress()
    {
        return this.carrierAddress;
    }
    
    /**
     * Note this does NOT set the carrier circuit on the port. 
     * Simply registers the port with this carrier if
     * it is compatible and throws an exception if not.<p>
     * 
     * Mode should be set on port before this is called so
     * can detect bridges or handle other mode-specific behavior<p>
     * 
     * @param portInstance port to add
     * @param isInternal if true, will check for compatibility with internal side
     * of port. If false, will check external side.
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void attach(Port<T> portInstance, boolean isInternal)
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("Carrier.attach: Attaching port %s (%s) to circuit %d.",
                    portInstance.toString(),
                    isInternal ? "internal" : "external",
                    this.carrierAddress());
        
        assert !this.ports.contains(portInstance)
            : "Carrier attach request from existing port on carrier.";
        
        // level check is mode-dependent because bridge ports have a lower-tier
        // external carrier but they use their internal carrier as the
        // when operating in bridge mode
        CarrierLevel portLevel = isInternal 
                ? portInstance.internalLevel()
                : portInstance.externalLevel();
        
        if(portLevel != this.level())
        {
            throw new UnsupportedOperationException("Carrier.attach: mismatched levels");
        }
        
        if(portInstance.getMode() == PortMode.CARRIER
                || (isInternal && portInstance.getMode().isBridge()))
        {
            // channel must match for carrier ports 
            // and the internal side of bridge ports
            if(portInstance.getChannel() != this.channel)
            {
                throw new UnsupportedOperationException("Carrier.attach: mismatched channels.");
            }
        }
        
        this.ports.add(portInstance);
    }

    /**
     * Removes reference to portInstance from this circuit but does NOT
     * update port to remove reference to this circuit. Is expected to 
     * be called from {@link Port#detach()} which handles that. <p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void detach(Port<T> portInstance)
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("Carrier.detach: Removing port %s from circuit %d.",
                    portInstance.toString(),
                    this.carrierAddress());
        
        assert this.ports.contains(portInstance)
            : "Carrier dettach request from port not on carrier.";
        
        this.ports.remove(portInstance);
    }
    
    /**
     * Incurs cost of transporting resources and 
     * returns the number of resources that could be 
     * successfully transported.<p>
     * 
     * Should only execute within the logistics service
     * so that we can honor simulated results if they are
     * later requested for execution.
     * 
     * If force == true, will incur cost and transport
     * full amount even the circuit is already saturated.  
     * Overage will be carried over into subsequent ticks 
     * until the circuit is no longer saturated.<p>
     */
    public synchronized long transmit(long quantity, boolean force, boolean simulate)
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        assert quantity >= 0 : "negative quantity in transmit request";
        
        if(quantity <= 0 || !this.isValid) return 0;
        
        this.refreshUtilization();
        long result = force ? quantity : Math.min(quantity, this.capacityPerTick - this.utilization);
        if(!simulate && result > 0) this.utilization += result;
        return result;
    }
    
    /**
     * Decays utilization based on current simulation tick.
     * Call before any capacity-dependent operation.
     */
    protected void refreshUtilization()
    {
        long currentTick = Simulator.currentTick();
        
        if(currentTick > this.lastTickSeen)
        {
            this.utilization = Math.max(0, this.utilization 
                    - this.capacityPerTick * (currentTick - this.lastTickSeen));
            this.lastTickSeen = currentTick;
        }
    }
    
    /**
     * All ports that were on this circuit are transferred
     * to the new circuit. There are no checks or notifications
     * to devices or transport nodes, because the ports/nodes/circuit
     * was already assumed to be valid.<p>
     * 
     * MAKES THIS CIRCUIT INVALID<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     * 
     */
    public void mergeInto(Carrier<T> into)
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        this.makeInvalid();
        this.ports.mergeInto(into.ports);
    }

    public int portCount()
    {
        return this.ports.size();
    }

    /**
     * Moves ports in this circuit matching the provided predicate
     * to the new circuit.<p>
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void movePorts(Carrier<T> into, Predicate<Port<T>> predicate)
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        
        this.ports.movePorts(into.ports, predicate);
    }
    
    /**
     * All upward carrier circuits accessible from this circuit via bridge ports.
     */
    public ImmutableSet<Carrier<T>> parents()
    {
        return this.ports.parents();
    }
    
    /**
     * For use by PortTracker to inform peer instances of bridge circuit changes.
     */
    protected PortTracker<T> portTracker()
    {
        return this.ports;
    }

    /**
     * Use to track currency of information in routing data.
     * All circuits share a globally unique, monotonically increasing
     * bridge version sequence. This means that any given set of circuits, 
     * will have a max version value at the time the set is created, 
     * and if any circuit in the set changes, the new max must be
     * greater than the old max. <p>
     * 
     * Bridge version is incremented to the next global value
     * any time a bridge is added or removed from this circuit.
     * Actual implementation is in PortTracker, which handles all
     * the bridge tracking.
     */
    public int bridgeVersion()
    {
        return this.ports.bridgeVersion();
    }
    
    /**
     * Retrieves upward routing information for this circuit.
     * If information is not current, will be automatically refreshed.
     */
    public Legs<T> legs()
    {
        if(this.legs == null || !this.legs.isCurrent())
        {
            this.legs = new Legs<T>(this);
        }
        return this.legs;
    }
    
    /**
     * Call when this circuit is discarded due to
     * all ports disconnecting or merge, etc. <p>
     * 
     * Will prevent any more use of the circuit.<p>
     * 
     * See {@link #isValid}
     */
    public void makeInvalid()
    {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        this.isValid = false;
    }

    /**
     * True if circuit is able to transport the given resource.
     * Currently always true.
     */
    public boolean canTransport(IResource<T> forResource)
    {
        return true;
    }

    @Override
    public T storageType()
    {
        return this.storageType;
    }

    public CarrierLevel level()
    {
        return level;
    }
}
