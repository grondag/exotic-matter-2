package grondag.hard_science.simulator.transport.management;

import static grondag.hard_science.simulator.transport.management.ConnectionResult.BRIDGE_CARRIER;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.BRIDGE_DIRECT;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.CARRIER_BRIDGE;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.CARRIER_CARRIER;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.CARRIER_DIRECT;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.DIRECT_BRIDGE;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.DIRECT_CARRIER;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.FAIL_CHANNEL_MISMATCH;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.FAIL_INCOMPATIBLE;
import static grondag.hard_science.simulator.transport.management.ConnectionResult.FAIL_LEVEL_GAP;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.concurrency.PrivilegedExecutor;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ItemStorageListener;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.endpoint.PortMode;
import grondag.hard_science.simulator.transport.endpoint.Port;
import grondag.hard_science.simulator.transport.routing.Leg;
import grondag.hard_science.simulator.transport.routing.Legs;
import grondag.hard_science.simulator.transport.routing.Route;

/**
 * All changes or state-dependent inquiries for storage and transport systems
 * should execute within this service. <p>
 * 
 * Avoids the need for synchronization
 * by ensuring all inquires and changes to transport and storage of a given
 * resource type are executed within a single thread. <p>
 * 
 * Most requests can be prioritized so that the called can block and expect
 * a reasonably quick return. Useful to handle player actions and world events.
 */
public class LogisticsService<T extends StorageType<T>> implements ITypedStorage<T>
{
    public static final LogisticsService<StorageTypeStack> ITEM_SERVICE 
        = new LogisticsService<StorageTypeStack>(StorageType.ITEM);
    
    public static final LogisticsService<StorageTypeFluid> FLUID_SERVICE 
    = new LogisticsService<StorageTypeFluid>(StorageType.FLUID);   
    
    public static final LogisticsService<StorageTypePower> POWER_SERVICE 
    = new LogisticsService<StorageTypePower>(StorageType.POWER);
    
    private final T storageType;

    public final PrivilegedExecutor executor;
    
    private LogisticsService(T storageType)
    {
        this.storageType = storageType;
        this.executor = new PrivilegedExecutor(
                String.format("Hard Science %s Logistics Service", storageType.enumType.toString()));
    }
    
    /**
     * Check in logic that should only run within this service.
     */
    @Override
    public boolean confirmServiceThread()
    {
        return executor.isRunningOn();
    }

    /**
     * Attempts to connect two ports.  Must be called from service thread. <p>
     * 
     * Assumes (does not verify) ports are physically adjacent or within wireless range.<p>
     * 
     * Returns true if ports were connected.
     */
    public boolean connect(Port<T> first, Port<T> second)
    {
        assert confirmServiceThread() : "LogisticsService.connect called outside service thread.";
        
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("LogisticsService.connect: CONNECT STARTED for %s to %s", first.toString(), second.toString());

        ConnectionResult result = connectionResult(first, second);
        
        switch(result)
        {
        case CARRIER_CARRIER:
        case BRIDGE_CARRIER:
        case CARRIER_BRIDGE:
        case BRIDGE_DIRECT:
        case CARRIER_DIRECT:
        case DIRECT_BRIDGE:
        case DIRECT_CARRIER:
            connectPorts(first, second, result);
            return true;
            
        case FAIL_CHANNEL_MISMATCH:
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.connect: attempt abandoned - channel mismatch.");
            return false;
            
        case FAIL_INCOMPATIBLE:
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.connect: attempt abandoned - incompatible port types.");
            return false;

        case FAIL_LEVEL_GAP:
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.connect: attempt abandoned - carrier level mismatch.");
            return false;

        case FAIL_STORAGE_TYPE:
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.connect: attempt abandoned - incompatible storage types.");
            return false;
            
        default:
            assert false : "LogisticsService.connect: Unhandled ConnectionResult enum";
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.connect: attempt abandoned - unhandled result. This is a bug.");
            return false;
        }
    }

    /**
     * Handles case when both ports are known to be carrier ports
     */
    private void connectPorts(Port<T> first, Port<T> second, ConnectionResult result)
    {
        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("LogisticsService.connectPorts: start");

        // now have to decide which circuit to use, or create a new circuit
        // if neither port already has one
        // We reference the internal circuit for ports in carrier mode because
        // external must be the same as internal for carrier ports, but external
        // carrier value may not yet be set. For all port modes, the external
        // carrier is separate from internal, and is what the port will connect with.
        Carrier<T> firstCircuit = result.left == PortMode.CARRIER ? first.internalCircuit() : first.externalCircuit();
        Carrier<T> secondCircuit = result.right == PortMode.CARRIER ? second.internalCircuit() : second.externalCircuit();
        if(firstCircuit == null)
        {
            Carrier<T> newCircuit;
            if(secondCircuit == null)
            {
                if(Configurator.logTransportNetwork) 
                    HardScience.INSTANCE.info("LogisticsService.connectPorts: Neither port has circuit - creating new circuit");

                newCircuit = new Carrier<T>(this.storageType, 
                        first.externalLevel(result.left), 
                        first.getChannel());
            }
            else
            {
                if(Configurator.logTransportNetwork) 
                    HardScience.INSTANCE.info("LogisticsService.connectPorts: First port has null circuit - using circuit from second port");

                newCircuit = secondCircuit;
            }

            attachBothPorts(first, second, newCircuit, result);
        }
        else if(secondCircuit == null || secondCircuit == firstCircuit)
        {
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.connectCarrierPorts: Second port has null circuit - using circuit from start port");

            attachBothPorts(first, second, firstCircuit, result);
        }
        else // ports already have two different, non-null circuits
        {
            if(firstCircuit.portCount() > secondCircuit.portCount())
            {
                if(result.allowMerge)
                {
                    if(Configurator.logTransportNetwork) 
                        HardScience.INSTANCE.info("LogisticsService.connectCarrierPorts: Ports have different circuits. Keeping start port circuit & merging second port circuit into start");
    
                    secondCircuit.mergeInto(firstCircuit);
                    attachBothPorts(first, second, firstCircuit, result);
                }
                else
                    HardScience.INSTANCE.warn("Mismatched circuits for port connect but merge not allowed. This is a bug, and strange (probably bad) things may happen now.");

            }
            else
            {
                if(result.allowMerge)
                {
                    if(Configurator.logTransportNetwork) 
                        HardScience.INSTANCE.info("LogisticsService.connectCarrierPorts: Ports have different circuits. Keeping second port circuit & merging start port circuit into second");
                    
                    firstCircuit.mergeInto(secondCircuit);
                    attachBothPorts(first, second, secondCircuit, result);
                }
                else
                    HardScience.INSTANCE.warn("Mismatched circuits for port connect but merge not allowed. This is a bug, and strange (probably bad) things may happen now.");

            }
        }
    }

    private void attachBothPorts(Port<T> first, Port<T> second, Carrier<T> toCircuit, ConnectionResult result)
    {
        first.setMode(result.left);
        second.setMode(result.right);
        first.attach(toCircuit, second);
        second.attach(toCircuit, first);
    }

    /**
     * Disconnects two ports if they are connected,
     * and splits affected circuits if necessary.
     * Should be called from the port that is being
     * removed if device removal is the cause.<p>
     * 
     * Must be called from service thread.
     */
    public void disconnect(Port<T> leaving)
    {
        assert confirmServiceThread() : "LogisticsService.connect called outside service thread.";

        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("LogisticsService.disconnect: DISCONNECT STARTED for %s", leaving.toString());

        if(!leaving.isAttached())
        {
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.disconnect: Disconnect abandoned - port not attached");
            return;
        }

        Port<T> mate = leaving.mate();

        assert mate != null : "Missing mate on port disconnect.";

        if(Configurator.logTransportNetwork) 
            HardScience.INSTANCE.info("LogisticsService.disconnect: Port mate is %s", mate.toString());


        // split isn't possible unless both ports are carrier ports
        if(leaving.getMode() == PortMode.CARRIER && mate.getMode() == PortMode.CARRIER)
        {
            if(Configurator.logTransportNetwork) 
                HardScience.INSTANCE.info("LogisticsService.disconnect: Checking for possible split");

            // Carrier split will be necessary UNLESS the mate is navigable via
            // an alternate route on the same carrier circuit. 
            // Split also not necessary if no connected ports beyond the leaving port.
            Set<Port<T>> reachableFromLeaving = findNavigableCarrierPorts(leaving, leaving.mate());
            if(reachableFromLeaving.size() > 1 && !reachableFromLeaving.contains(leaving.mate()))
            {
                Carrier<T> existingCircuit = leaving.internalCircuit();

                Carrier<T> newCircuit = 
                        new Carrier<T>(existingCircuit.storageType(), existingCircuit.level(), existingCircuit.channel);

                // no alternate route, so must split
                if(reachableFromLeaving.size() >= existingCircuit.portCount() / 2)
                {
                    if(Configurator.logTransportNetwork) 
                        HardScience.INSTANCE.info("LogisticsService.disconnect: Split needed, moving mate-side ports to new circuit %d", newCircuit.carrierAddress());

                    // Reachable ports are at least half of total. 
                    // Swap mate and unreachable ports to a new circuit
                    existingCircuit.movePorts(newCircuit, new Predicate<Port<T>>()
                    {
                        @Override
                        public boolean test(@Nullable Port<T> t)
                        {
                            return !reachableFromLeaving.contains(t);
                        }
                    });
                }
                else
                {
                    if(Configurator.logTransportNetwork) 
                        HardScience.INSTANCE.info("LogisticsService.disconnect: Split needed, moving leave-side ports to new circuit %d", newCircuit.carrierAddress());

                    // Reachable ports are less than half of total.
                    // Swap leaving port and reachable ports to a new circuit
                    existingCircuit.movePorts(newCircuit, new Predicate<Port<T>>()
                    {
                        @Override
                        public boolean test(@Nullable Port<T> t)
                        {
                            return reachableFromLeaving.contains(t);
                        }
                    });
                }
            }
            else if(Configurator.logTransportNetwork) 
            {
                HardScience.INSTANCE.info("LogisticsService.disconnect: Split not needed for %d ports. (If > 1 ports are still reachable)",
                        reachableFromLeaving.size());
            }
        }
        leaving.detach();
        mate.detach();
    }

    /**
     * Returns all ports that are reachable via peers of the given port
     * and which reference the given ports's internal carrier circuit,
     * either internally or externally.<p>
     * 
     * Will include those peers, mates of those peers, peers of those mates, etc.
     * WILL also include the starting port that is provided.<p>
     * 
     * If stop port is provided, will return as soon as stop node is found.
     * In that case, stop node will be in the result set, but other nodes
     * that are navigable may be left out. Useful as a performance optimization
     * when the results will not be needed if the stop node is found.
     */
    private HashSet<Port<T>> findNavigableCarrierPorts(Port<T> startingFrom, @Nullable Port<T> stopAt)
    {
        assert startingFrom.getMode() == PortMode.CARRIER
                : "transport topology search with non-carrier starting port";

        Carrier<T> onCircuit = startingFrom.internalCircuit();

        /**
         * Ports known to reference the internal carrier of the starting port.
         * These are the ports that may need to be moved to a new circuit
         * if the current circuit must be split
         */
        HashSet<Port<T>> results = new HashSet<Port<T>>();
        results.add(startingFrom);

        /**
         * Ports that should be checked for carrier mates.
         * Any port in this list should already have been added
         * to results.
         */
        ArrayDeque<Port<T>> workList = new ArrayDeque<Port<T>>();
        workList.add(startingFrom);

        while(!workList.isEmpty())
        {
            Port<T> port = workList.poll();
            for(Port<T> peer : port.carrierMates())
            {
                if(peer.internalCircuit() == onCircuit && peer.isAttached())
                {
                    if(results.add(peer))
                    {
                        if(peer == stopAt) return results;

                        // note that we check external circuit here. Will pick up
                        // bridge or direct port using the circuit that will need to be
                        // swapped if there is a split and it is attached
                        if(peer.isAttached())
                        {
                            Port<T> mate = peer.mate();
                            if(mate.externalCircuit() == onCircuit && results.add(mate))
                            {
                                if(mate == stopAt) return results;

                                // Don't need to check peers of mate unless the
                                // carrier passes through into the device. This
                                // will only be true for Carrier ports, so could
                                // have instead checked port type instead of internal carrier.
                                if(mate.internalCircuit() == onCircuit)
                                {
                                    workList.add(mate);
                                }
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * Convenient version of {@link #connectionResult(Port, int, Port, int)}
     * to use if both ports already have channels fully configured.
     */
    public ConnectionResult connectionResult(
            Port<T> port1,
            Port<T> port2)
    {
        return connectionResult(
                port1, 
                port1.getChannel(), 
                port2, 
                port2.getChannel());
    }

    /**
     * Returns the effective port types for the two ports
     * to be mated with the given given channels.
     * Implements all the rules described in PortFunction.<p>
     */
    public ConnectionResult connectionResult(
            Port<T> port1,
            int channel1,
            Port<T> port2,
            int channel2)
    {
        if(port1.connector() != port2.connector()) return FAIL_INCOMPATIBLE;
        
        boolean channelMatch = channel1 == channel2;

        // logic relies on enum ordering: CARRIER / DIRECT / BRIDGE
        // and that ports 1 and 2 are always sorted by that order
        boolean swapOrder = port1.function().ordinal() > port2.function().ordinal();
        if(swapOrder)
        {
            Port<T> swapPort = port1;
            port1 = port2;
            port2 = swapPort;
        }

        switch(port1.function())
        {
        case CARRIER:
        {
            switch(port2.function())
            {
            case CARRIER:
            {
                if(port1.internalLevel() == port2.internalLevel())
                {
                    // Two parents of same level, so just 
                    // need to check for channel match.  
                    return  channelMatch
                            ? CARRIER_CARRIER
                            : FAIL_CHANNEL_MISMATCH;
                }
                // non-top carrier-to-carrier must be same level
                else return FAIL_LEVEL_GAP;
            }

            case DIRECT:
            {
                // direct ports can only join with parents at same level
                if(port1.externalLevel() == port2.externalLevel())
                {
                    return  channelMatch
                            ? (swapOrder ? DIRECT_CARRIER : CARRIER_DIRECT)
                            : FAIL_CHANNEL_MISMATCH;
                }
                else
                    return FAIL_LEVEL_GAP;
            }

            case BRIDGE:
            {
                if(port1.internalLevel() == port2.internalLevel())
                {
                    // if bridge at same level as carrier
                    // then acts exactly like another carrier port
                    return channelMatch
                            ? CARRIER_CARRIER
                            : FAIL_CHANNEL_MISMATCH;
                }
                else if(port1.internalLevel().above() == port2.internalLevel())
                {
                    // if bridge is one level above carrier then it acts
                    // as a proper bridge port
                    return swapOrder ? BRIDGE_CARRIER : CARRIER_BRIDGE;
                }
                else return FAIL_LEVEL_GAP;
            }

            default:
                assert false : "Port.connectionResult(): Unhandled PortFunction enum";
            return FAIL_INCOMPATIBLE;
            }
        }

        case DIRECT:
        {
            switch(port2.function())
            {
            case CARRIER:
            {
                // port type ordering should make this case impossible
                assert false : "Port.connectionResult(): Incorrect port ordering";
            return FAIL_INCOMPATIBLE;
            }

            case DIRECT:
            {
                // direct ports can't form circuits on their own
                
                // TODO: Let direct ports mate with direct ports to form device/device private links
                return FAIL_INCOMPATIBLE;
            }

            case BRIDGE:
            {
                // direct ports always act as direct ports
                // so bridge port mode really only a question of level
                if(port1.externalLevel() == port2.internalLevel())
                {
                    return channelMatch
                            ? (swapOrder ? CARRIER_DIRECT : DIRECT_CARRIER)
                            : FAIL_CHANNEL_MISMATCH;
                }
                else if(port1.externalLevel().above() == port2.internalLevel())
                {
                    return swapOrder ? BRIDGE_DIRECT : DIRECT_BRIDGE;
                }
                else return FAIL_LEVEL_GAP;
            }

            default:
                assert false : "Port.connectionResult(): Unhandled PortFunction enum";
            return FAIL_INCOMPATIBLE;
            }
        }

        case BRIDGE:
        {
            switch(port2.function())
            {

            case CARRIER:
            case DIRECT:
            {
                // port type ordering should make this case impossible
                assert false : "Port.connectionResult(): Incorrect port ordering";
            return FAIL_INCOMPATIBLE;
            }

            case BRIDGE:
            {
                if(port1.internalLevel() == port2.internalLevel())
                    // two bridge ports of same level behave like carrier ports
                    return channelMatch
                            ? CARRIER_CARRIER
                            : FAIL_CHANNEL_MISMATCH;
                
                else if(port1.internalLevel() == port2.internalLevel().below())
                    // if this port is 1 lower, then it will act as carrier port
                    // and other as passive bridge
                    return swapOrder ? BRIDGE_CARRIER : CARRIER_BRIDGE;

                else if(port1.internalLevel() == port2.internalLevel().above())
                    // if this port is 1 higher, then it will act as bridge port
                    // and other as carrier
                    return swapOrder ? CARRIER_BRIDGE : BRIDGE_CARRIER;

                else 
                    return FAIL_LEVEL_GAP;
            }

            default:
                assert false : "Port.connectionResult(): Unhandled PortFunction enum";
            return FAIL_INCOMPATIBLE;
            }
        }

        default:
            assert false : "Port.connectionResult(): Unhandled PortFunction enum";
        return FAIL_INCOMPATIBLE;
        }
    }
    
    /**
        Generates list of possible routes between two devices.  
        Relies on sort order and structure guaranteed by 
        {@link Carrier#legs()}.<p>
        
        Resource is currently ignored.
       
        Approach: compare first leg of each list, with these possibilities...<p>
       
        1) legs are the same level and same end carrier.
        add combinations to results and advance both!<p>
       
        2) one leg end circuit is at the same level
        but of a lower address than the other. ,
        In that case, advance the lower and retry for match.<p>
        
        3) one leg end circuit is of a lower level than
        the other. Behavior now depends on if we have found
        any routes yet. If so, we are done, because we 
        would be generating redundant routes if we go up to 
        higher level. (Future optimization my try to find
        high-speed upper routes that aren't redundant/pathological.)
        If we haven't found any routes yet, we can advance.<p>
       
        If in any case we aren't able to advance either iterator,
        we are out of possibilities and exit loop.
        or the same level but with a lower circuit number.
        
        If this version is run from the service thread, it simply runs
        and returns a result.  If it is run from a different thread
        (typically the server thread) it is submitted as a privileged 
        task to the service thread and blocks until the task is complete.
        This ensures results are always consistent with transport state, but
        allows it to be safely called from any thread.
    */
    public ImmutableList<Route<T>> findRoutesNow(IDevice fromDevice, IDevice toDevice, IResource<T> forResource)
    {
        if(this.confirmServiceThread()) return findRoutesImpl(fromDevice, toDevice, forResource);
            
        try
        {
            return executor.submit(() ->
            {
                return findRoutesImpl(fromDevice, toDevice, forResource);
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Unable to find transport routes due to error", e);
            return ImmutableList.of();
        }
    }
    
    /**
     * Version of {@link #findRoutesNow(IDevice, IDevice)} but
     * returns a future and does not run in highest priority.  Will run after 
     * transport tasks submitted earlier.
     */
    public Future<ImmutableList<Route<T>>> findRoutes(IDevice fromDevice, IDevice toDevice, IResource<T> forResource)
    {
        return executor.submit( () ->
        {
            return findRoutesImpl(fromDevice, toDevice, forResource);
        }, false);
    }
    
    /**
     * Implementation of {@link #findRoutesNow(IDevice, IDevice)}
     */
    private ImmutableList<Route<T>> findRoutesImpl(IDevice fromDevice, IDevice toDevice, IResource<T> forResource)
    {
        @SuppressWarnings("unchecked")
        ITransportManager<T> tm1 = (ITransportManager<T>) fromDevice.tranportManager(storageType);
        if(tm1 == null) return ImmutableList.of();
        
        @SuppressWarnings("unchecked")
        ITransportManager<T> tm2 = (ITransportManager<T>) toDevice.tranportManager(storageType);
        if(tm2 == null) return ImmutableList.of();

        Legs<T> legs1 = tm1.legs(forResource);
        Legs<T> legs2 = tm2.legs(forResource);
        
        boolean canConnect = false;
        for(Carrier<T> c : legs1.islands)
        {
            if(c.canTransport(forResource) && legs2.islands.contains(c))
            {
                canConnect = true;
                break;
            }
        }
        
        if(!canConnect) return ImmutableList.of();
        
        Iterator<ImmutableList<Leg<T>>> itr1 = legs1.legs().iterator();
        if(!itr1.hasNext()) return ImmutableList.of();
        
        Iterator<ImmutableList<Leg<T>>> itr2 = legs2.legs().iterator();
        if(!itr2.hasNext()) return ImmutableList.of();
        
        ImmutableList<Leg<T>> list1 = itr1.next();
        ImmutableList<Leg<T>> list2 = itr2.next();
        
        ImmutableList.Builder<Route<T>> routeBuilder = ImmutableList.builder();
        
        /**
         * prevent unterminated loop due to logic error.
         */
        int safetyCheck = 0;
        
        // see header for explanation
        do
        {
         
            // relying on circuits not to output legs with empty lists!
            Leg<T> firstLeg1 = list1.get(0);
            Leg<T> firstLeg2 = list2.get(0);
            
            if(firstLeg1.end() == firstLeg2.end())
            {
                addCombinedRoutesToBuilder(list1, list2, routeBuilder);
                if(!itr1.hasNext() || !itr2.hasNext()) break;
                list1 = itr1.next();
                list2 = itr2.next();
            }
            else
            {
                // check for same level
                int level1 = firstLeg1.end().level().ordinal();
                int level2 = firstLeg2.end().level().ordinal();
                
                if(level1 < level2)
                {
                    if(itr1.hasNext()) list1 = itr1.next();
                    else break;
                }
                else if(level1 > level2)
                {
                    if(itr2.hasNext()) list2 = itr2.next();
                    else break;
                }
                else
                {
                    // levels same, advance lower address
                    int addr1 = firstLeg1.end().carrierAddress();
                    int addr2 = firstLeg2.end().carrierAddress();
                    
                    if(addr1 < addr2)
                    {
                        if(itr1.hasNext()) list1 = itr1.next();
                        else break;
                    }
                    else if(addr1 > addr2)
                    {
                        if(itr2.hasNext()) list2 = itr2.next();
                        else break;
                    }
                    else
                    {
                        // to get here implies that end circuits for both lists
                        // have same level and carrier address - which means they
                        // are the same carrier.  So they should have been handled
                        // by the first case...
                        assert false : "Logically impossible result in transport routing.  Something is seriously borked.";
                        break;
                    }
                }
            }
            
            
        } while(++safetyCheck < 1000);
        
        assert safetyCheck < 1000 : 
            "probable loop logic error in route building";
            
        return routeBuilder.build();
    }
    
    private void addCombinedRoutesToBuilder(List<Leg<T>> list1, List<Leg<T>> list2, ImmutableList.Builder<Route<T>> builder)
    {
        for(Leg<T> l1 : list1)
        {
            for(Leg<T> l2 : list2)
            {
                builder.add(new Route<T>(l1, l2));
            }
        }
    }

    @Override
    public T storageType()
    {
        return this.storageType;
    }
    
    /**
     * Sends resource from one device to another
     * by any available route(s) and return
     * the quantity actually sent.  Will send via
     * more than one route if necessary / possible.
     * 
     * @param resource  stuff to send
     * @param quantity  how much
     * @param from      producing device
     * @param to        consuming device
     * @param force     send even if transport circuits saturated - for user/world events
     * @param simulate  pretend and return what would have happened
     * @param request   job task if applies
     * @return          Future with quantity produced and consumed
     */
    public Future<Long> sendResource(
            IResource<T> resource, 
            final long quantity, 
            IDevice from, 
            IDevice to, 
            boolean force, 
            boolean simulate,
            NewProcurementTask<T> request)
    {
        return executor.submit( () ->
        {
            return sendResourceImpl(resource, quantity, from, to, force, simulate, request);
        }, false);
    }
    
    /**
     * Blocking version of {@link #sendResource(IResource, long, IDevice, IDevice, boolean, boolean, NewProcurementTask)}
     * 
     * If this version is run from the service thread, it simply runs
     * and returns a result.  If it is run from a different thread
     * (typically the server thread) it is submitted as a privileged 
     * task to the service thread and blocks until the task is complete.
     * This ensures results are always consistent with transport state, but
     * allows it to be safely called from any thread.
     */
    public long sendResourceNow(
            IResource<T> resource, 
            final long quantity, 
            IDevice from, 
            IDevice to, 
            boolean force, 
            boolean simulate,
            NewProcurementTask<T> request)
    {
        if(this.confirmServiceThread()) return sendResourceImpl(resource, quantity, from, to, force, simulate, request);
        
        try
        {
            return executor.submit(() ->
            {
                return sendResourceImpl(resource, quantity, from, to, force, simulate, request);
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Unable to send resources due to error", e);
            return 0;
        }
    }
    
    private long sendResourceImpl(
            IResource<T> resource, 
            final long quantity, 
            IDevice from, 
            IDevice to, 
            boolean force, 
            boolean simulate,
            NewProcurementTask<T> request)
    {
        ImmutableList<Route<T>> routes = 
                this.findRoutesImpl(from, to, resource);
        
        if(routes.isEmpty()) return 0L;
        
        long remaining = quantity;
        
        for(Route<T> r : routes)
        {
            remaining -= 
                    this.sendResourceOnRouteImpl(r, resource, remaining, from, to, force, simulate, request);
            
            if(remaining <= 0) break;
        }
        return quantity - remaining;
    }
    
    /**
     * Like {@link #sendResource(IResource, long, IDevice, IDevice, boolean, boolean, NewProcurementTask)}
     * but specifies a single route to use.
     */
    public Future<Long> sendResourceOnRoute(
            Route<T> route, 
            IResource<?> resource, 
            final long quantity, 
            IDevice from, 
            IDevice to, 
            boolean force, 
            boolean simulate,
            NewProcurementTask<?> request)
    {
        return executor.submit( () ->
        {
            return sendResourceOnRouteImpl(route, resource, quantity, from, to, force, simulate, request);
        }, false);
    }
    
    /**
     * Blocking version of {@link #sendResourceOnRoute(Route, IResource, long, IDevice, IDevice, boolean, boolean, NewProcurementTask)}
     * 
     * If this version is run from the service thread, it simply runs
     * and returns a result.  If it is run from a different thread
     * (typically the server thread) it is submitted as a privileged 
     * task to the service thread and blocks until the task is complete.
     * This ensures results are always consistent with transport state, but
     * allows it to be safely called from any thread.
     */
    public long sendResourceOnRouteNow(
            Route<T> route, 
            IResource<?> resource, 
            final long quantity, 
            IDevice from, 
            IDevice to, 
            boolean force, 
            boolean simulate,
            NewProcurementTask<?> request)
    {
        if(this.confirmServiceThread()) return sendResourceOnRouteImpl(route, resource, quantity, from, to, force, simulate, request);
                
        try
        {
            return executor.submit(() ->
            {
                return sendResourceOnRouteImpl(route, resource, quantity, from, to, force, simulate, request);
            }, true).get();
        }
        catch (Exception e)
        {
            HardScience.INSTANCE.error("Unable to send resources due to error", e);
            return 0;
        }
    }
    
    /**
     * Relies on fact that all transport and storage operations for a
     * given storage type all run within a single thread. This means
     * we should be able to simulate a result, determine max throughput,
     * and then execute that result with confidence it will stick.
     */
    private long sendResourceOnRouteImpl(
            Route<T> route, 
            IResource<?> resource, 
            final long quantity, 
            IDevice from, 
            IDevice to, 
            boolean force, 
            boolean simulate,
            NewProcurementTask<?> request)
    {
        StorageType<?> storageType = resource.storageType();
        
        @SuppressWarnings("unchecked")
        ITransportManager<T> tmFrom = (ITransportManager<T>) from.tranportManager(storageType);
        @SuppressWarnings("unchecked")
        ITransportManager<T> tmTo = (ITransportManager<T>) to.tranportManager(storageType);
        
        if(tmFrom == null || tmTo == null)
        {
            assert false : "Missing transport managers in route send request";
            return 0;
        }
        
        if(!((tmFrom.isConnectedTo(route.first()) && tmTo.isConnectedTo(route.last()))
          || (tmFrom.isConnectedTo(route.last()) && tmTo.isConnectedTo(route.first()))))
        {
            assert false : "Invalid route request - device(s) not attached to route endpoints.";
            return 0;
        }
        
        long limit = quantity;
        
        // always simulate first time

        limit = from.onProduce(resource, limit, true, request);
        limit = to.onConsume(resource, limit, true, request);
        
        for(Carrier<T> c : route.circuits())
        {
            long q = c.transmit(quantity, force, true);
            if(force)
            {
                assert q == quantity : "Circuit did not honor forced transmit request";
            }
            else
            {
                // don't ask subsequent circuits for more than what previous can handle
                limit = Math.min(q, limit);
            }
        }
        
        if(!simulate)
        {
            // actually send stuff!
            for(Carrier<T> c : route.circuits())
            {
                assert limit == c.transmit(limit, force, false)
                      : "Circuit did not honor simulated result";
            }
            assert limit == from.onProduce(resource, limit, false, request)
                : "Device did not honor simulated result";
            
            assert limit == to.onConsume(resource, limit, false, request)
                : "Device did not honor simulated result";
        }
        
        return limit;
    }

    /**
     * Schedules listener for initialization on service thread so that
     * event subscriptions can all be time-consistent.
     */
    public void initializeListener(ItemStorageListener itemStorageListener)
    {
        executor.execute( () ->
        {
            itemStorageListener.initialize();;
        }, false);
    }
    
    /**
     * Returns true if any transport route exists between the two devices
     * that could be used to transport the given resource. Resource currently
     * only matters for fluid circuits, which are always locked to a specific
     * fluid.<p>
     * 
     * By convention, returns true if both devices are the same device.<p>
     * 
     * Must be called from service thread.
     */
    public boolean areDevicesConnected(IDevice fromDevice, IDevice toDevice, IResource<T> forResource)
    {
        assert confirmServiceThread() : "Transport logic running outside logistics service";
        
        if(fromDevice == toDevice) return true;
                
        @SuppressWarnings("unchecked")
        ITransportManager<T> tm1 = (ITransportManager<T>) fromDevice.tranportManager(storageType);
        if(tm1 == null) return false;
        
        @SuppressWarnings("unchecked")
        ITransportManager<T> tm2 = (ITransportManager<T>) toDevice.tranportManager(storageType);
        if(tm2 == null) return false;

        Legs<T> legs1 = tm1.legs(forResource);
        Legs<T> legs2 = tm2.legs(forResource);
        
        for(Carrier<T> c : legs1.islands)
        {
            if(c.canTransport(forResource) && legs2.islands.contains(c))
            {
                return true;
            }
        }
        return false;
    }
}
