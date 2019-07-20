package grondag.hard_science.machines.energy;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.varia.TimeUnits;
import grondag.hard_science.simulator.device.IDeviceComponent;

public interface IEnergyComponent extends IReadWriteNBT, IDeviceComponent
{
    /**
     * If this component can store energy, the current stored energy.
     * Always zero if component cannot store energy.
     */
    public default long storedEnergyJoules() { return 0; }
    
    /**
     * If this component can store energy, the maximum stored energy.
     * Always zero if component cannot store energy.
     */
    public default long maxStoredEnergyJoules() { return 0; };
    
    /**
     * Approximate recent level of power coming into the component. 
     * May be an average, only occasionally updated and/or smoothed.
     * Always zero or positive. <br><br>
     * 
     * If the component stores power, this represents charging.
     * A non-zero value implies {@link #powerOutputWatts()} will be zero.<br><br>
     * 
     * If the component is an external power source, this is the power
     * available in the external grid, if known, or will be the same as {@link #powerOutputWatts()}<br><br>
     * 
     * If the component is a generator, will always be zero.
     */
    public default float powerInputWatts()
    {
        return this.energyInputLastTickJoules() * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }
   
    /**
     * Total energy input in the last complete tick.
     */
    public long energyInputLastTickJoules();

    /**
     * Net result of {@link #powerInputWatts()} and {@link #powerOutputWatts()}.
     * Will be negative if output was higher than input.
     */
    public default float netWattsLastTick()
    {
        return (this.energyInputLastTickJoules() - this.energyOutputLastTickJoules())
                * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }
    /**
     * Maximum possible value of {@link #powerInputWatts()}. 
     * Derived from {@link #maxEnergyInputJoulesPerTick()}.
     */
    public default float maxPowerInputWatts()
    {
        return MachinePower.joulesPerTickToWatts(this.maxEnergyInputJoulesPerTick());
    }
    
    /**
     * Max single-tick input to this component, if it accepts input. 
     * For use by machine internal logic and to derive
     * {@link #maxPowerInputWatts()} for display on client.
     */
    public long maxEnergyInputJoulesPerTick();
    
    /**
     * Represents energy export or consumption. 
     * Always zero or positive. <p>
     * 
     * For batteries and output buffers, this represents discharge. 
     * A non-zero value implies {@link #powerInputWatts()} will be zero.<p>
     * 
     * If the component is an input buffer this is the energy consumed 
     * by the device during the last tick.<p>
     * 
     * If the component is a generator, represents generator output.
     */
    public default float powerOutputWatts()
    {
        return this.energyOutputLastTickJoules() * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }

    /**
     * Total energy output in the last complete tick.
     */
    public long energyOutputLastTickJoules();
    
    /**
     * Maximum possible value of {@link #powerOutputWatts()}. 
     * Derived from {@link #maxEnergyInputJoulesPerTick()}.
     */
    public default float maxPowerOutputWatts()
    {
        return MachinePower.joulesPerTickToWatts(this.maxEnergyOutputJoulesPerTick());
    }
    
    /**
     * Max single-tick output from this component, if it can output. 
     * For use by machine internal logic and to derive
     * {@link #maxPowerOutputWatts()} for display on client.
     */
    public long maxEnergyOutputJoulesPerTick();
    
    /**
     * True if component is able to provide energy right now.
     * If false, any attempt to extract energy will receive a zero result.
     */
    public boolean canProvideEnergy();
    
    /**
     * Consumes energy from this component. 
     * 
     * While conceptually this is power, is handled as energy due to the
     * quantized nature of time in Minecraft. Intended to be called each tick.<br><br>
     * @param machine 
                  The machine to which this component belongs. Components
                  don't save a reference, and some may need to access buffers or
                  other machine components.
     * @param maxOutput
     *            Maximum amount of energy to be extracted, in joules.<br>
    *            Limited by {@link #maxDeviceDrawPerTick()}.
     * @param allowPartial
     *            If false, no energy will be extracted unless the entire requested amount can be provided.
     * @param simulate
     *            If true, result will be simulated and no state change occurs.
     *
     * @return Energy extracted (or that would have been have been extracted, if simulated) in joules.
     */
    public long provideEnergy(long maxOutput, boolean allowPartial, boolean simulate);
    
    
    /**
     * True if provider is able to receive energy right now.
     * Will always be false for external power sources and internal generators.
     * If false, any attempt to input energy will receive a zero result.
     */
    public boolean canAcceptEnergy();
    
    /**
     * Adds energy to this component. Will only do something and return
     * a non-zero value for storage-type components.
     * 
     * While conceptually this is power, is handled as energy due to the
     * quantized nature of time in Minecraft. Intended to be called each tick.<br><br>
     *
     * @param maxInput
     *            Maximum amount of energy to be inserted, in joules.<br>
     *            Limited by {@link #maxEnergyInputPerTick()}.
     *            
     * @param allowPartial
     *            If false, no energy will be input unless the entire requested amount can be accepted.
     *            
     * @param simulate
     *            If true, result will be simulated and no state change occurs.
     *            
     * @return Energy accepted (or that would have been have been accepted, if simulated) in joules.
     */
     public long acceptEnergy(long maxInput, boolean allowPartial, boolean simulate);
     
}
