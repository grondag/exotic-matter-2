package grondag.hard_science.machines.energy;

public class PolyethyleneFuelCellType
{
    public static final PolyethyleneFuelCellType BASIC = new PolyethyleneFuelCellType(false);
    public static final PolyethyleneFuelCellType ADVANCED = new PolyethyleneFuelCellType(true);
    
    /**
     * If true, cell(s) is/are coupled with a thermoelectric generator to convert waste heat
     * and boost efficiency.
     */
    public final boolean hasThermalCapture;
    
    /**
     * How good we are at turning PE into electricity. Includes benefit of thermoelectric if present.
     */
    public final float conversionEfficiency;
    
    /**
     * To compute actual fuel usage.
     */
    public final float fuelNanoLitersPerJoule;

    public final float joulesPerFuelNanoLiters;
    
    private PolyethyleneFuelCellType(boolean hasThermalCapture)
    {
        this.hasThermalCapture = hasThermalCapture;
        this.conversionEfficiency = hasThermalCapture 
                ? MachinePower.POLYETHYLENE_BOOSTED_CONVERSION_EFFICIENCY 
                        : MachinePower.POLYETHYLENE_CONVERSION_EFFICIENCY;
        this.joulesPerFuelNanoLiters = MachinePower.JOULES_PER_POLYETHYLENE_NANOLITER * this.conversionEfficiency;
        this.fuelNanoLitersPerJoule = 1f / this.joulesPerFuelNanoLiters;
        
    }
}
