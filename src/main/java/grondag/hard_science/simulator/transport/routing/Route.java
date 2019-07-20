package grondag.hard_science.simulator.transport.routing;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;

/**
 * Defines a path from one circuit to another circuit.
 */
public class Route<T extends StorageType<T>>
{
    /**
     * List of all circuits in the route. 
     * First circuit will be first in the list
     * and last will be last. (duh)
     */
    private final ImmutableList<Carrier<T>> circuits;
    
    public Route(Leg<T> leg1, Leg<T> leg2)
    {
        if(leg1.end() != leg2.end())
            throw new UnsupportedOperationException("Pathological route construction: leg endpoints do not match.");
            
        if(leg1.start.carrierAddress() > leg2.start.carrierAddress())
        {
            // ensure lower-numbered endpoint is first
            Leg<T> swap = leg1;
            leg1 = leg2;
            leg2 = swap;
        }
        
        ImmutableList.Builder<Carrier<T>> builder = ImmutableList.builder();
        
        builder.add(leg1.start);
        if(leg1.size() == 3)
        {
            builder.add(leg1.inner());
            builder.add(leg1.end());
        }
        else if(leg1.size() == 2)
        {
            builder.add(leg1.end());
        }
        
        if(leg2.size() == 3)
        {
            builder.add(leg2.inner());
            builder.add(leg2.start());
        }
        else if(leg2.size() == 2)
        {
            builder.add(leg2.start());
        }
        
        this.circuits = builder.build();
        
        assert isValid() : "Invalid route construction.";
    }
    
    /** 
     * Checks assertions in dev environment.
     * Basic premise is that circuits should not appear
     * in both legs.  Relies on legs to ensure that circuits
     * in each leg are distinct. (Legs do this by ensuring
     * adjacent circuits are one level apart.)
     */
    private boolean isValid()
    {
        int size = this.circuits.size();
        
        if(size == 1) return true;
        
        if(size == 2)
        {
            return this.circuits.get(0) != this.circuits.get(1);
        }
        else if(size == 3)
        {
            return this.circuits.get(0) != this.circuits.get(2);
        }
        else if(size == 4)
        {
            return this.circuits.get(0) != this.circuits.get(3)
                && this.circuits.get(0) != this.circuits.get(2)
                && this.circuits.get(1) != this.circuits.get(3);
        }
        else
        {
            return size == 5
                && this.circuits.get(0) != this.circuits.get(4)
                && this.circuits.get(1) != this.circuits.get(3);
        }
    }
    
    /**
     * The endpoint circuit in the route with the lower carrier address.
     * Will be same as {@link #last()} in a direct route.
     */
    public Carrier<T> first()
    {
        return this.circuits.get(0);
    }
    
    /**
     * The endpoint circuit in the route with the higher carrier address.
     * Will be same as {@link #first()} in a direct route.
     */
    public Carrier<T> last()
    {
        return this.circuits.get(this.circuits.size()-1);
    }
    
    public List<Carrier<T>> circuits()
    {
        return this.circuits;
    }
    
    public int size()
    {
        return this.circuits.size();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for ( Carrier<T> c : this.circuits)
        {
            if (sb.length() > 0) sb.append(".");
            sb.append(c.carrierAddress());
        }
        return sb.toString();
    }
}
