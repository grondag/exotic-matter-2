package grondag.hard_science.simulator.transport.routing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;

/**
 * Describes one half of a route - path from a starting
 * circuit to an ending circuit.  End circuit is always
 * the highest level circuit if more than one circuit.
 * To form a route, end circuits of two legs must be the same.
 */
public abstract class Leg<T extends StorageType<T>>
{
    /**
     * Lowest-level circuit in the leg. 
     * Will be same as {@link #end()} for direct routes.
     */
    public abstract Carrier<T> start();
    
    /**
     * Highest-level circuit in the leg. 
     * Will be same as {@link #start()} for direct routes.
     */
    public abstract Carrier<T> end();
    
    /**
     * If leg has three circuits, the circuit between
     * {@link #start()} and {@link #end()}. Null otherwise.
     */
    @Nullable
    public abstract Carrier<T> inner();
    
    /**
     * Number of circuits in the leg. 
     * Possible values are 1, 2 or 3.
     */
    public abstract int size();
    
    /**
     * Returns a new leg with end circuit as the given
     * circuit.  Only valid if size < 3 and provided circuit
     * is one level higher than current end.
     */
    public abstract Leg<T> append(Carrier<T> newEnd);
    
    protected final Carrier<T> start;
    
    private Leg(Carrier<T> first)
    {
        this.start = first;
    }
    
    public static <V extends StorageType<V>> Leg<V> create(Carrier<V> single)
    {
        return new SingleLeg<V>(single);
    }
    
    public static <V extends StorageType<V>> Leg<V> create(Carrier<V> start, Carrier<V> end)
    {
        return new DoubleLeg<V>(start, end);
    }
    
    public static <V extends StorageType<V>> Leg<V> create(Carrier<V> start, Carrier<V> inner, Carrier<V> end)
    {
        return new TripleLeg<V>(start, inner, end);
    }
    
    private static class SingleLeg<T extends StorageType<T>> extends Leg<T>
    {
        private SingleLeg(Carrier<T> single)
        {
            super(single);
        }

        @Override
        public Carrier<T> start()
        {
            return this.start;
        }

        @Override
        @Nonnull 
        public Carrier<T> end()
        {
            return this.start;
        }

        @Override
        public @Nullable Carrier<T> inner()
        {
            return null;
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public Leg<T> append(@Nonnull Carrier<T> newEnd)
        {
            return new DoubleLeg<T>(this.start, newEnd) ;
        }
        
        @Override
        public String toString()
        {
            return Integer.toString(this.start.carrierAddress());
        }
    }
    
    private static class DoubleLeg<T extends StorageType<T>> extends Leg<T>
    {
        protected final Carrier<T> end;
        
        private DoubleLeg(Carrier<T> start, Carrier<T> end)
        {
            super(start);
            this.end = end;
            
            // allow for gap of bottom/top because will see that in triple subclass
            assert start.level().ordinal() < end.level().ordinal()
                    : "Circuit level mismatch in leg constructor.";
            
            assert start.storageType() == end.storageType()
                    : "Circuit type mismatch in leg constructor.";
        }

        @Override
        @Nonnull 
        public Carrier<T> start()
        {
            return this.start;
        }

        @Override
        @Nonnull 
        public Carrier<T> end()
        {
            return this.end;
        }

        @Override
        public @Nullable Carrier<T> inner()
        {
            return null;
        }

        @Override
        public int size()
        {
            return 2;
        }

        @Override
        @Nonnull 
        public Leg<T> append(@Nonnull Carrier<T> newEnd)
        {
            return new TripleLeg<T>(this.start, this.end, newEnd);
        }
        
        @Override
        public String toString()
        {
            return String.format("%d.%d", this.start.carrierAddress(), this.end.carrierAddress());
        }
    }
    
    private static class TripleLeg<T extends StorageType<T>> extends DoubleLeg<T>
    {
        protected final Carrier<T> inner;
        
        private TripleLeg(Carrier<T> start, Carrier<T> inner, Carrier<T> end)
        {
            super(start, end);
            this.inner = inner;
            
            assert start.level() == inner.level().below()
                    && inner.level() == end.level().below()
                    : "Circuit level mismatch in leg constructor.";
            
            
            assert start.storageType() == inner.storageType()
                    && inner.storageType()  == end.storageType()
                    : "Circuit type mismatch in leg constructor.";
        }

        @Override
        @Nonnull 
        public Carrier<T> start()
        {
            return this.start;
        }

        @Override
        @Nonnull 
        public Carrier<T> end()
        {
            return this.end;
        }

        @Override
        @Nonnull 
        public @Nullable Carrier<T> inner()
        {
            return this.inner;
        }

        @Override
        public int size()
        {
            return 3;
        }
        
        @Override
        public Leg<T> append(@Nonnull Carrier<T> newEnd)
        {
            throw new UnsupportedOperationException("Cannot append circuit to a leg with three circuits.");
        }
        
        @Override
        public String toString()
        {
            return String.format("%d.%d.%d",
                    this.start.carrierAddress(), 
                    this.inner.carrierAddress(), 
                    this.end.carrierAddress());
        }
    }
}
