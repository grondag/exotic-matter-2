package grondag.hard_science.machines.energy;

import grondag.exotic_matter.varia.TimeUnits;
import grondag.hard_science.matter.VolumeUnits;
import net.minecraft.util.math.MathHelper;

public enum BatteryChemistry
{
    SILICON(MachinePower.JOULES_PER_SILICON_BATTERY_LITER, 20, 10, 950),
    CAPACITOR(MachinePower.JOULES_PER_SILICON_BATTERY_LITER / 5, 1, 1, 1000);
    
    /**
     * How much energy batter can store in a given volume.  This is after any inefficiency cost has been incurred.  
     * Such costs are accounted for during charging.
     */
    public final long energyDensityJoulesPerLiter;
    
    /**
     * How many simulated minutes to an 80% charge.  Will charge at a fixed rate up the this point. 
     * In-game time will only be 1/72 of this.  So 20 minutes would be less than 30 seconds play time. <br><br>
     * 
     * Charging beyond the 80% level will be somewhat slower.
     * This is similar to charging pattern for contemporary Lithium Ion batteries - they will charge
     * at at a fixed voltage with essentially linear effect on capacity until resistance 
     * of cell kicks in and input current drops.
     */
    public final int minutesToEighyPercentCharged;

    /**
     * How long it takes to discharge battery given max output.  
     * Is assumed the battery includes some compensating mechanism to provide a 
     * virtually constant voltage and current to the consumer.
     */
    public final int minutesToDischarge;
    
    
    /**
     * Energy released during discharge vs energy input during charging.
     */
    public final int efficiencyPerMille;
    
    /**
     * Table of max accepted charge input, after overhead costs, given current charge of 0 (no charge) to 63 (fully charged).
     * Results mille of charge (0 to 1000 where 1000 = max charge) per minute;
     * Values will be the same for the start 80% of the table, but don't care about the space - gives us consistent, simple lookup logic.
     */
    private final int[] chargeRateMillePerMinute = new int[64];
    
    private BatteryChemistry(long energyDensity_J_per_L, int minutesToEighyPercentCharged,  int minutesToDischarge, int efficiencyPerMille)
    {
        this.energyDensityJoulesPerLiter = energyDensity_J_per_L;
        this.minutesToEighyPercentCharged = minutesToEighyPercentCharged;
        this.minutesToDischarge = minutesToDischarge;
        this.efficiencyPerMille = efficiencyPerMille;
        
        
        // Initialize lookup table to compute charge rate. 
        // This isn't based on battery math - which I don't understand, I just picked a curve that looked approximately like
        // the charge curve for contemporary Li-Ion batteries.
        // Didn't bother with interpolation here - didn't seem worthwhile given how fuzzy the model is.
        int maxRate = 800 / minutesToEighyPercentCharged;
        chargeRateMillePerMinute[0] = maxRate;
        
        for(int i = 1; i < 64; i++)
        {
            float charge = i / 63f;
            chargeRateMillePerMinute[i] = (int) (1000 * Math.min(maxRate, charge / (Math.E * minutesToEighyPercentCharged * (Math.pow(charge, Math.E)))));
        }
    }
    
    /**
     * Gives max charge rate in milles of charge per simulated minute at current charge - does not include waste due to inefficiency.
     */
    public int chargeRateMillesPerSimulatedMinute(long maxChargeJoules, long currentChargeJoules)
    {
        if(maxChargeJoules == 0) return 0;
        return chargeRateMillePerMinute[MathHelper.clamp((int) (currentChargeJoules * 63 / maxChargeJoules), 0, 63)];
    }
    
    /**
     * Gives net increase in charge for a single tick at current charge - does not include waste due to inefficiency.
     */
    public long chargeRateJoulesPerTick(long maxChargeJoules, long currentChargeJoules)
    {
        // avoid float-point math by converting to real-world days then to simulated days.  
        // Going to ticks directly would require FP because a simulated minute is less than one tick in simulation time.
        return (maxChargeJoules * chargeRateMillesPerSimulatedMinute(maxChargeJoules, currentChargeJoules) * TimeUnits.MINUTES_PER_DAY) / (1000 * TimeUnits.TICKS_PER_SIMULATED_DAY);
    }
    
    /**
     * Upper bound for informational purposes.
     */
    public long maxChargeJoulesPerTick(long maxChargeJoules)
    {
        return chargeRateJoulesPerTick(maxChargeJoules, 0);
    }
    
    /**
     * Should be used to limit power coming from this battery type.
     */
    public long maxDischargeJoulesPerTick(long maxChargeJoules)
    {
        int ticksToDischarge = (int) (this.minutesToDischarge * TimeUnits.TICKS_PER_SIMULATED_MINUTE);
        return ticksToDischarge == 0 ? maxChargeJoules : maxChargeJoules / ticksToDischarge;
    }

    /**
     * Capacity of a battery of this chemistry with the given volume. 
     * In Joules.
     */
    public long capacityForNanoliters(long volumeNanoliters)
    {
        return volumeNanoliters * this.energyDensityJoulesPerLiter / VolumeUnits.LITER.nL;
    }
}
