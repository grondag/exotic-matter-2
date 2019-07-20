package grondag.hard_science.machines.support;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.varia.structures.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Handles constraints and accounting for bulkResource containers.
 * @author grondag
 *
 */
public abstract class ThroughputRegulator<T extends StorageType<T>>
{
    /**
     * Has no internal state, enforces no limits and does no accounting.
     * Use when you don't want to regulate and don't want to check for null.
     */
    @SuppressWarnings("rawtypes")
    public static final ThroughputRegulator DUMMY = new Dummy();
    /**
     * If this regulator has input constraints, 
     * limits the requested amount to those constraints
     * and updates internal accounting if applicable. <p>
     * 
     * @param requested    desired input amount
     * @param isSimulated  if false, will not update internal accounting
     * @param allowPartial if false will return zero if constraints don't permit full amount
     * @return input amount that fits within constraints
     */
    public long limitInput(long requested, boolean isSimulated, boolean allowPartial)
    {
        return requested;
    }

    /**
     * If this regulator has output constraints, 
     * limits the requested amount to those constraints
     * and updates internal accounting if applicable. <p>
     * 
     * @param requested    desired output amount
     * @param isSimulated  if false, will not update internal accounting
     * @param allowPartial if false will return zero if constraints don't permit full amount
     * @return output amount that fits within constraints
     */
    public long limitOutput(long requested, boolean isSimulated, boolean allowPartial)
    {
        return requested;
    }
    
    public long maxOutputPerTick()
    {
        return Long.MAX_VALUE;
    }
    
    public long maxInputPerTick()
    {
        return Long.MAX_VALUE;
    }
    
    public long inputLastTick()
    {
        return 0;
    }
    
    public long outputLastTick()
    {
        return 0;
    }
    
    /**
     * Has no limits and does no accounting.
     */
    @SuppressWarnings("rawtypes")
    private static class Dummy extends ThroughputRegulator
    {
        
    }
    
    public boolean isFailureCause()
    {
        return false;
    }
    
    public void blame(IResource<T> resource) {}
    
    public ImmutableList<IResource<T>> blames() { return ImmutableList.of(); }
    
    public void forgive(IResource<T> resource) {}
    
    public void forgiveAll() {}

    /**
     * Has no limits but tracks input/output per tick.
     */
    public static class Tracking<T extends StorageType<T>> extends ThroughputRegulator<T>
    {
        protected long inputLastTick;
        
        /** total resources input during the current tick. */
        protected long inputThisTick;
        
        protected long outputLastTick;
        
        /** total resources input during the current tick. */
        protected long outputThisTick;
        
        protected int lastTickSeen = Integer.MIN_VALUE;
        
        protected SimpleUnorderedArrayList<IResource<T>> blames = new SimpleUnorderedArrayList<>();

        protected synchronized void updateTracking()
        {
            int now = Simulator.currentTick();
            if(now == lastTickSeen) return;
            
            if (now == lastTickSeen + 1)
            {
                this.outputLastTick = this.outputThisTick;
                this.inputLastTick = this.inputThisTick;
            }
            else
            {
                this.outputLastTick = 0;
                this.inputLastTick = 0;
            }
            this.inputThisTick = 0;
            this.outputThisTick = 0;
            this.lastTickSeen = now;
        }
        
        @Override
        public synchronized long limitInput(long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            if(!isSimulated)
            {
                this.inputThisTick += requested;
            }
            return requested;
        }

        @Override
        public synchronized long limitOutput(long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            if(!isSimulated)
            {
                this.outputThisTick += requested;
            }
            return requested;
        }

        @Override
        public long inputLastTick()
        {
            this.updateTracking();
            return this.inputLastTick;
        }

        @Override
        public long outputLastTick()
        {
            this.updateTracking();
            return this.outputLastTick;
        }

        @Override
        public boolean isFailureCause()
        {
            return !this.blames.isEmpty();
        }

        @Override
        public void blame(IResource<T> resource)
        {
            this.blames.addIfNotPresent(resource);
        }
        
        @Override
        public ImmutableList<IResource<T>> blames()
        { 
            return ImmutableList.copyOf(this.blames);
        }

        @Override
        public void forgive(IResource<T> resource)
        {
            this.blames.removeIfPresent(resource);
        }
        
        @Override
        public void forgiveAll()
        {
            this.blames.clear();;
        }
    }
    
    /**
     * Has input/output limits and also does accounting
     */
    public static class Limited<T extends StorageType<T>> extends ThroughputRegulator.Tracking<T>
    {
        private final long maxInputPerTick;
        
        private final long maxOutputPerTick;
        
        public Limited(long maxInputPerTick, long maxOutputPerTick)
        {
            this.maxInputPerTick = maxInputPerTick;
            this.maxOutputPerTick = maxOutputPerTick;
        }
        
        @Override
        public synchronized long limitInput(final long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            long result = Math.min(requested, this.maxInputPerTick - this.inputThisTick);
            
            if(result != requested && !allowPartial) return 0;
            
            if(!isSimulated)
            {
                this.inputThisTick += result;
            }
            return result;
        }

        @Override
        public synchronized long limitOutput(final long requested, boolean isSimulated, boolean allowPartial)
        {
            this.updateTracking();
            
            long result = Math.min(requested, this.maxOutputPerTick - this.outputThisTick);
            
            if(result != requested && !allowPartial) return 0;
            
            if(!isSimulated)
            {
                this.outputThisTick += result;
            }
            return result;
        }

        @Override
        public long maxOutputPerTick()
        {
            return this.maxOutputPerTick;
        }

        @Override
        public long maxInputPerTick()
        {
            return this.maxInputPerTick;
        }
    }
}
