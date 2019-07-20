package grondag.hard_science.machines.energy;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.storage.PowerContainer;

public abstract class AbstractGenerator implements IEnergyComponent
{
    private final IDevice owner;
    
    /**
     * Set by subclasses via {@link #setMaxOutputJoulesPerTick(long)}
     */
    private long maxEnergyOutputPerTick = Long.MAX_VALUE;
    
    /** total of all energy provided during the current tick. */
    private long outputThisTick;
    
    /**
     * total of all energy provided last tick.
     */
    private long outputLastTick;
    
    protected AbstractGenerator(IDevice owner)
    {
        this.owner = owner;
    }
    
    @Override
    public IDevice device()
    {
        return this.owner;
    }
    
    /**
     * Must be called in subclasses during initialization and NBT deserialization.
     */
    protected final void setMaxOutputJoulesPerTick(long maxJoules)
    {
        this.maxEnergyOutputPerTick = maxJoules;
    }
    
    @Override
    public long energyOutputLastTickJoules()
    {
        return this.outputLastTick;
    }
    
    @Override
    public long maxEnergyOutputJoulesPerTick()
    {
        return this.maxEnergyOutputPerTick;
    }

    /** generators only provide energy to output buffer during device tick*/
    @Override
    public boolean canProvideEnergy()
    {
        return false;
    }

    /** generators only provide energy to output buffer during device tick*/
    @Override
    public long provideEnergy(long maxOutput, boolean allowPartial, boolean simulate)
    {
        return 0;
    }

    public final void generate(PowerContainer outputContainer)
    {
        this.outputLastTick = this.outputThisTick;
        
        long needed = outputContainer.acceptEnergy(this.maxEnergyOutputPerTick, true, true);
        
        this.outputThisTick = this.generateImplementation(needed, true, false);
                
        // prevent shenannigans/derpage
        if(outputThisTick <= 0) return;
        
        long stored = outputContainer.acceptEnergy(this.outputThisTick, true, false);
        this.setDirty();
        assert this.outputThisTick == stored : "output buffer did not accept all generated energy";
    }
    
    /**
     * Implementation can assume maxOutput has already been tested/adjusted against per-tick max.
     * Machine is the machine containing the power component - in case it needs access to 
     * other machine components.
     */
    protected abstract long generateImplementation(long maxOutput, boolean allowPartial, boolean simulate);
    
    @Override
    public long energyInputLastTickJoules()
    {
        return 0;
    }

    @Override
    public long maxEnergyInputJoulesPerTick()
    {
        return 0;
    }

    @Override
    public long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate)
    {
        return 0;
    }

    @Override
    public boolean canAcceptEnergy()
    {
        return false;
    }
}
