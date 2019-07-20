package grondag.hard_science.simulator.transport.routing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;

/**
 * Describes upward routes available from a circuit
 */
public class Legs<T extends StorageType<T>>
{
    @SuppressWarnings("rawtypes")
    private static final Legs<?> EMPTY_LEGS = new Legs();
    
    @SuppressWarnings("unchecked")
    public static <E extends StorageType<E>> Legs<E> emptyLegs()
    {
        return (Legs<E>) EMPTY_LEGS;
    }
    
    /**
     * Max circuit bridge version for all included circuits at time of create.
     * If any included circuit has a version higher than this, then
     * this information is no longer valid. See {@link Carrier#bridgeVersion()}.
     */
    private final int maxBridgeVersion;
    
    /**
     * All circuits referenced by this instance.
     */
    public final ImmutableSet<Carrier<T>> circuits;
    
    /**
     * See {@link #legs()}
     */
    private final ImmutableList<ImmutableList<Leg<T>>> legs;
    
    /**
     * Unique Top-level circuits across all legs.
     * For two devices to be connected, they must share at
     * least one island. Allows for connectivity checking
     * without building specific routes.
     */
    public final ImmutableSet<Carrier<T>> islands;
    
    public Legs(Carrier<T> forCircuit)
    {
        int maxVersion = forCircuit.bridgeVersion();
        
        ImmutableSet.Builder<Carrier<T>> circuitBuilder = ImmutableSet.builder();
        circuitBuilder.add(forCircuit);
        
        LegBuilder<T> legBuilder = new LegBuilder<T>();
        
        Leg<T> firstLeg = Leg.create(forCircuit);
        legBuilder.add(firstLeg);
        
        ImmutableSet.Builder<Carrier<T>> islandBuilder = ImmutableSet.builder();
        
        if(forCircuit.parents().isEmpty())
        {
            // no parents, so we are our own island
            islandBuilder.add(forCircuit);
        }
        else
        {
            // have parents, so let's expand legs
            LinkedList<Leg<T>> workList = new LinkedList<Leg<T>>();
            workList.add(firstLeg);
            while(!workList.isEmpty())
            {
                Leg<T> workLeg = workList.poll();
                
                // not checking for empty because won't be in
                // worklist if parents is empty
                for(Carrier<T> c : workLeg.end().parents())
                {
                    // don't extend legs with circuits we can directly access
                    // but can't appy this check to the starting node
                    // because then we wouldn't get the first tier of legs
                    if(workLeg != firstLeg && forCircuit.parents().contains(c)) continue;
                    
                    // add to unique circuit list
                    circuitBuilder.add(c);
                    maxVersion = Math.max(maxVersion, c.bridgeVersion());
                    
                    // extend leg and add to legs
                    Leg<T> newLeg = workLeg.append(c);
                    
                    legBuilder.add(newLeg);
                    
                    // check end of this leg for another level of parents
                    if(newLeg.end().parents().isEmpty())
                    {
                        // no parents, so this circuit is an island
                        islandBuilder.add(newLeg.end());
                    }
                    else
                    {
                        // found more parents, so add to work list and keep going
                        workList.add(newLeg);
                    }
                }
            }
            
        }
        
        this.legs = legBuilder.build();
        this.circuits = circuitBuilder.build();
        this.islands = islandBuilder.build();
        this.maxBridgeVersion = maxVersion;
    }
    
    /** 
     * Combines legs from multiple circuits - used to represent information 
     * for a device attached to more than one circuit.
      */
    public Legs(Iterable<Carrier<T>> circuits)
    {
        int maxVersion = 0;
        
        ImmutableSet.Builder<Carrier<T>> circuitBuilder = ImmutableSet.builder();
        
        LegBuilder<T> legBuilder = new LegBuilder<T>();
        
        ImmutableSet.Builder<Carrier<T>> islandBuilder = ImmutableSet.builder();
        
        for(Carrier<T> c : circuits)
        {
            Legs<T> legs = c.legs();
            
            maxVersion = Math.max(maxVersion, legs.maxBridgeVersion);
            circuitBuilder.addAll(legs.circuits);
            
            for(ImmutableList<Leg<T>> list : legs.legs)
            {
                for(Leg<T> leg : list)
                {
                    legBuilder.add(leg);
                }
            }
            islandBuilder.addAll(legs.islands);
        }
        
        this.legs = legBuilder.build();
        this.circuits = circuitBuilder.build();
        this.islands = islandBuilder.build();
        this.maxBridgeVersion = maxVersion;
    }
    
    /** for empty legs */
    private Legs()
    {
        this.circuits = ImmutableSet.of();
        this.legs = ImmutableList.of();
        this.islands = ImmutableSet.of();
        this.maxBridgeVersion = Integer.MAX_VALUE;
    }
    
    
    public boolean isCurrent()
    {
        if(this.circuits.isEmpty()) return true;
        
        for(Carrier<T> c : this.circuits)
        {
            if(c.bridgeVersion() > this.maxBridgeVersion) return false;
        }
        return true;
    }
    
    /**
     * All legs originating from a circuit or device (for compounded Legs).
     * This is a list of lists. The top level list has an entry for 
     * every unique end circuit for which there is a leg, and is sorted by
     * carrier level (lowest first) and then by the address of the end circuit.<p>
     * 
     * The top-level list may be empty, but any lists within the list
     * are guaranteed to have at least one entry.<p>
     * 
     * This structure allows for fast pair-wise iterations through two
     * leg lists during route formation.
     */
    public ImmutableList<ImmutableList<Leg<T>>> legs()
    {
        return this.legs;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(ImmutableList<Leg<T>> list : this.legs)
        {
            for(Leg<T> leg : list)
            {
                if(sb.length() > 0) sb.append(", ");
                sb.append(leg.toString());
            }
        }
        sb.append(" Islands: ");
        {
            int i = 0;
            for(Carrier<T> c : this.islands)
            {
                sb.append(c.carrierAddress());
                if(++i < this.islands.size())
                {
                    sb.append(", ");
                }
            }
        }
        sb.append(" Circuits: ");
        {
            int i = 0;
            for(Carrier<T> c : this.circuits)
            {
                sb.append(c.carrierAddress());
                if(++i < this.circuits.size())
                {
                    sb.append(", ");
                }
            }
        }
        sb.append(" ver: ");
        sb.append(this.maxBridgeVersion);
        return sb.toString();
    }
    
    private static class LegBuilder<T extends StorageType<T>>
    {
        // create map of leg builders keyed by end circuit
        // and add builder for first circuit
        private HashMap<Carrier<T>, ImmutableList.Builder<Leg<T>>> legBuilders
         = new HashMap<Carrier<T>, ImmutableList.Builder<Leg<T>>>();
        
        protected void add(Leg<T> leg)
        {
            ImmutableList.Builder<Leg<T>> builder = legBuilders.get(leg.end());
            if(builder == null)
            {
                builder = new ImmutableList.Builder<>();
                legBuilders.put(leg.end(), builder);
            }
            builder.add(leg);
        }
        
        protected ImmutableList<ImmutableList<Leg<T>>> build()
        {
            ImmutableList.Builder<ImmutableList<Leg<T>>> lastLegBuilder = ImmutableList.builder();
            
            // here's the reason for this whole mess: sort the lists by level, end carrier
            legBuilders
            .entrySet()
            .stream()
            .sorted(new Comparator<Map.Entry<Carrier<T>, ImmutableList.Builder<Leg<T>>>>()
            {
                @Override
                public int compare(@Nullable Entry<Carrier<T>, Builder<Leg<T>>> o1, @Nullable Entry<Carrier<T>, Builder<Leg<T>>> o2)
                {
                    Carrier<T> c1 = o1.getKey();
                    Carrier<T> c2 = o2.getKey();
                    return ComparisonChain
                            .start()
                            .compare(c1.level().ordinal(),
                                    c2.level().ordinal())
                            .compare(c1.carrierAddress(), c2.carrierAddress())
                            .result();
                }
                
            })
            .forEach(e -> lastLegBuilder.add(e.getValue().build()));
            
            return lastLegBuilder.build();
        }
    }
}
