package grondag.hard_science.machines.energy;

import java.util.function.Function;

import grondag.exotic_matter.varia.TimeUnits;
import grondag.exotic_matter.world.Location;
import grondag.hard_science.matter.VolumeUnits;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class MachinePower
{
    public static final long JOULES_PER_KWH =                   3600000;
    public static final long JOULES_PER_POLYETHYLENE_LITER =   42600000;
    public static final long JOULES_PER_SILICON_BATTERY_LITER = 4300000;
    public static final long JOULES_PER_GRAPHENE_CAPACITOR_LITER = 1000000;
    
    public static final float JOULES_PER_POLYETHYLENE_NANOLITER = (float) JOULES_PER_POLYETHYLENE_LITER / VolumeUnits.LITER.nL;
    
    /** s
     * What portion of battery component volume is actually dedicated to storage
     * vs support circuitry, cooling, etc.
     */
    public static final float BATTERY_SPACE_FACTOR = .775f;
    
    /**
     * Efficiency of catalytic conversion plates for turning PE into electricity.
     */
    public static final float POLYETHYLENE_CONVERSION_EFFICIENCY = 0.7257f;
    
    /**
     * Efficiency of thermoelectric generation plate in typical usage.
     */
    public static final float THERMOELECTRIC_CONVERSION_EFFICIENCY = 0.60f;
    
    /**
     * Efficiency of PE conversion plates when paired with thermoelectic generation plates to capture waste heat.
     */
    public static final float POLYETHYLENE_BOOSTED_CONVERSION_EFFICIENCY 
        = POLYETHYLENE_CONVERSION_EFFICIENCY + (1 - POLYETHYLENE_CONVERSION_EFFICIENCY) * THERMOELECTRIC_CONVERSION_EFFICIENCY;
    
    /**
     * Efficiency of catalytic formation process turning atmospheric CO2 and Water into PE and O2.
     */
    public static final float POLYETHYLENE_FORMATION_EFFICIENCY = 0.90f;
    
    /**
     * Max rate of PE consumption and conversion to electricity by catalytic conversion plate in linear microometers per simulated second.
     * Volume will be surface area of plate x this number.
     * This number is pure hand-waving because the technology doesn't exist yet.
     */
    public static final int POLYETHYLENE_MAX_CONVERSION_RATE_MICROMETERS = 27;
    
    /**
     * Max rate of power transfer for standard local power bus
     */
    public static final long POWER_BUS_JOULES_PER_TICK = (long) wattsToJoulesPerTick(1000000);
    
    /**
     * Max possible sum of World.getSunBrightnessFactor() for a single minecraft day.
     * Used to allocate daily insolation per-tick based on current brightness
     */
    public static final float TOTAL_DAILY_BRIGHTNESS_FACTOR = 13559;
   
    /**
     * Mean daily insolation in an icy biome
     */
    public static final long DAILY_INSOLATION_MIN_JOULES =  7200000;

    /**
     * Mean daily insolation in desert hills
     */
    public static final long DAILY_INSOLATION_MAX_JOULES = 27000000;

    /**
     * Minimum insolation (0-1) that applies at sea level 
     * on a sunny day in icy biome.
     */
    public static final float DAILY_INSOLATION_MIN_FACTOR = (float) 7200000 / DAILY_INSOLATION_MAX_JOULES;

    /**
     * How much daily insolation can normally vary.  Is
     * 1 - {@link #DAILY_INSOLATION_MIN_FACTOR}.
     */
    public static final float DAILY_INSOLATION_VARIABILITY = 1 - DAILY_INSOLATION_MIN_FACTOR;

    /**
     * Returns factor of {@link #DAILY_INSOLATION_MAX_JOULES} that will
     * be received during maximum world brightness for the given location.<p>
     * 
     * Factors influencing include:
     * sky light - if none always zero
     * rainfall - can it happen and how much?
     * temp - higher = better
     * height - higher is slightly better
     */
    public static float insolationFactor(Location location)
    {
        World world = location.world();
        
        if(!world.provider.hasSkyLight()) return 0;
        
        Biome b = world.getBiome(location);
        
        float tempFactor = MathHelper.clamp(b.getDefaultTemperature(), 0, 2f) / 2f;
        
        float rainFactor = b.canRain() 
                ? (1 - MathHelper.clamp(b.getRainfall(), 0 , 1)) * 0.8f
                        : 1;

        float variation = tempFactor * 0.6f + rainFactor * 0.3f
                + location.getY() / 255f * 0.1f;
        
            
        return DAILY_INSOLATION_MIN_FACTOR +
                DAILY_INSOLATION_VARIABILITY * variation;
    }
    
    /**
     * How much of insolation is captured by photo-electric cells as
     * electrical energy. 
     */
    public static final float PHOTO_ELECTRIC_EFFICIENCY = 0.65f;
    
    /**
     * How much of insolation is captured by photo-chemical cells as
     * chemical energy. 
     */
    public static final float PHOTO_CHEMICAL_EFFICIENCY = PHOTO_ELECTRIC_EFFICIENCY;
    
    /**
     * For machines that capture atmospheric carbon by splitting CO2, energy premium
     * to reflect fact that CO2 capture may use active concentration mechanism.
     * Cost should not be charged if device has access to concentrated CO2.
     */
    public static final float CARBON_CAPTURE_EFFICIENCY = 0.80f;
    
//    public static enum FuelCellSpec
//    {
//        STANDARD_INTEGRATED(JOULES_PER_KWH, 1000, 2000, POLYETHYLENE_CONVERSION_EFFICIENCY_PER_MILLE);
//        
//        public static final FuelCellSpec[] VALUES = FuelCellSpec.values();
//        
//        /**
//         * Corresponds to {@link IMachinePowerProvider#maxEnergyJoules}
//         */
//        public final long maxEnergyJoules;
//        
//        /**
//         * Corresponds to {@link IMachinePowerProvider#maxPowerInputWatts().}
//         */
//        public final long maxPowerInputWatts;
//        
//        /**
//         * Corresponds to {@link IMachinePowerProvider#maxEnergyInputPerTick()()}
//         */
//        public final long maxEnergyInputPerTick;
//        
//        /**
//         * Corresponds to {@link IMachinePowerProvider#maxPowerOutputWatts()}
//         */
//        public final long maxPowerOutputWatts;
//        
//        /**
//         * Corresponds to {@link IMachinePowerProvider#maxEnergyOutputPerTick()()}
//         */
//        public final long maxEnergyOutputPerTick;
//        
//        /**
//         * Corresponds to {@link IMachinePowerProvider#maxPowerInOrOutWatts()}
//         */
//        public final long maxPowerInOrOutWatts;
//
//        /**
//         * Natural log of {@link #maxPowerInOrOutWatts}
//         */
//        public final double logMaxInOrOut;
//        
//        /**
//         * For fuel cells and generators, efficiency of power conversion for fuel consumed. As value 0 to 1000.
//         */
//        public final int conversionEfficiencyPerMille;
//        
//        
//        private FuelCellSpec(long maxEnergyJoules, long maxPowerInputWatts, long maxPowerOutputWatts, int conversionEfficiencyPerKilo)
//        {
//            this.maxEnergyJoules = maxEnergyJoules;
//            this.maxPowerInputWatts = maxPowerInputWatts;
//            this.maxEnergyInputPerTick = maxPowerInputWatts / 20;
//            this.maxPowerOutputWatts = maxPowerOutputWatts;
//            this.maxEnergyOutputPerTick = maxPowerOutputWatts / 20;
//            this.maxPowerInOrOutWatts = Math.max(maxPowerInputWatts, maxPowerOutputWatts);
//            this.conversionEfficiencyPerMille = conversionEfficiencyPerKilo;
//            this.logMaxInOrOut = Math.log(this.maxPowerInOrOutWatts);
//        }
//    }
    
    /**
     * Here to remind me that I can't just multiply joules (watt seconds) by 20 to watts,
     * because 20 ticks is a real-world second, not a simulated second. 
     * Doing so will understate power consumption.
     */
    public static float joulesPerTickToWatts(long joules)
    {
        return joules * TimeUnits.TICKS_PER_SIMULATED_SECOND;
    }
    
    public static float wattsToJoulesPerTick(long watts)
    {
        return watts * TimeUnits.SIMULATED_SECONDS_PER_TICK;
    }
    
    @SuppressWarnings("null") 
    private static final Function<?, ?> FORMAT_JOULES[] = 
    {
            // 0 digits- should never get
            new Function<String, String>() { @Override
            public String apply(String t) { return t + "J"; }},
            
            // 1 digit
            new Function<String, String>() { @Override
            public String apply(String t) { return t + "J"; }},
            
            // 2  digits
            new Function<String, String>() { @Override
            public String apply(String t) { return t + "J"; }},
            
            // 3  digits
            new Function<String, String>() { @Override
            public String apply(String t) { return t + "J"; }},
            
            // 4  digits - kJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal1_2Suffix(t, "kJ"); }},
            
            // 5  digits - kJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal2_1Suffix(t, "kJ"); }},
            
            // 6  digits - kJ
            new Function<String, String>() { @Override
            public String apply(String t) { return t.substring(0, 3) + "kJ"; }},
            
            // 7  digits - MJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal1_2Suffix(t, "MJ"); }},
            
            // 8  digits - MJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal2_1Suffix(t, "MJ"); }},
            
            // 9  digits - MJ
            new Function<String, String>() { @Override
            public String apply(String t) { return t.substring(0, 3) + "MJ"; }},
            
            // 10  digits - GJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal1_2Suffix(t, "GJ"); }},
            
            // 11  digits - GJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal2_1Suffix(t, "GJ"); }},
            
            // 12  digits - GJ
            new Function<String, String>() { @Override
            public String apply(String t) { return t.substring(0, 3) + "GJ"; }},
            
            // 13  digits - TJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal1_2Suffix(t, "TJ"); }},
            
            // 14  digits - TJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal2_1Suffix(t, "TJ"); }},
            
            // 15  digits - TJ
            new Function<String, String>() { @Override
            public String apply(String t) { return t.substring(0, 3) + "TJ"; }},

            // 16  digits - PJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal1_2Suffix(t, "PJ"); }},
            
            // 17  digits - PJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal2_1Suffix(t, "PJ"); }},
            
            // 18  digits - PJ
            new Function<String, String>() { @Override
            public String apply(String t) { return t.substring(0, 3) + "PJ"; }},

            // 19  digits - EJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal1_2Suffix(t, "EJ"); }},
            
            // 20  digits - EJ
            new Function<String, String>() { @Override
            public String apply(String t) { return insertDecimal2_1Suffix(t, "EJ"); }},
            
            // 21  digits - EJ
            new Function<String, String>() { @Override
            public String apply(String t) { return t.substring(0, 3) + "EJ"; }},
            
            new Function<String, String>() { @Override
            public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { @Override
            public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { @Override
            public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { @Override
            public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { @Override
            public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { @Override
            public String apply(String t) { return "WOW"; }},
            new Function<String, String>() { @Override
            public String apply(String t) { return "WOW"; }}
    };
    
    @SuppressWarnings("null") 
    private static final Function<?, ?> FORMAT_WATTS[] = 
        {
                // 0 digits- should never get
                new Function<String, String>() { @Override
                public String apply(String t) { return t + "W"; }},
                
                // 1 digit
                new Function<String, String>() { @Override
                public String apply(String t) { return t + "W"; }},
                
                // 2  digits
                new Function<String, String>() { @Override
                public String apply(String t) { return t + "W"; }},
                
                // 3  digits
                new Function<String, String>() { @Override
                public String apply(String t) { return t + "W"; }},
                
                // 4  digits - kJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal1_2Suffix(t, "kW"); }},
                
                // 5  digits - kJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal2_1Suffix(t, "kW"); }},
                
                // 6  digits - kJ
                new Function<String, String>() { @Override
                public String apply(String t) { return t.substring(0, 3) + "kW"; }},
                
                // 7  digits - MJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal1_2Suffix(t, "MW"); }},
                
                // 8  digits - MJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal2_1Suffix(t, "MW"); }},
                
                // 9  digits - MJ
                new Function<String, String>() { @Override
                public String apply(String t) { return t.substring(0, 3) + "MW"; }},
                
                // 10  digits - GJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal1_2Suffix(t, "GW"); }},
                
                // 11  digits - GJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal2_1Suffix(t, "GW"); }},
                
                // 12  digits - GJ
                new Function<String, String>() { @Override
                public String apply(String t) { return t.substring(0, 3) + "GW"; }},
                
                // 13  digits - TJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal1_2Suffix(t, "TW"); }},
                
                // 14  digits - TJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal2_1Suffix(t, "TW"); }},
                
                // 15  digits - TJ
                new Function<String, String>() { @Override
                public String apply(String t) { return t.substring(0, 3) + "TW"; }},

                // 16  digits - PJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal1_2Suffix(t, "PW"); }},
                
                // 17  digits - PJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal2_1Suffix(t, "PW"); }},
                
                // 18  digits - PJ
                new Function<String, String>() { @Override
                public String apply(String t) { return t.substring(0, 3) + "PW"; }},

                // 19  digits - EJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal1_2Suffix(t, "EW"); }},
                
                // 20  digits - EJ
                new Function<String, String>() { @Override
                public String apply(String t) { return insertDecimal2_1Suffix(t, "EW"); }},
                
                // 21  digits - EJ
                new Function<String, String>() { @Override
                public String apply(String t) { return t.substring(0, 3) + "EW"; }},
                
                new Function<String, String>() { @Override
                public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { @Override
                public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { @Override
                public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { @Override
                public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { @Override
                public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { @Override
                public String apply(String t) { return "WOW"; }},
                new Function<String, String>() { @Override
                public String apply(String t) { return "WOW"; }}
        };
    
    public static String insertDecimal1_2Suffix(String inString, String withSuffix)
    {
        return inString.substring(0, 1) + "." + inString.substring(1, 3) + withSuffix;
    }
    
    public static String insertDecimal2_1Suffix(String inString, String withSuffix)
    {
        return inString.substring(0, 2) + "." + inString.substring(2, 3) + withSuffix;
    }
    
    @SuppressWarnings({ "unchecked"})
    public static String formatEnergy(long joules, boolean includePositiveSign)
    {
        if(joules == 0) return "0J";

        String raw = Long.toString(joules);
        int len = raw.length();
        
        if(joules > 0)
        {
            String result = ((Function<String, String>)FORMAT_JOULES[len]).apply(raw);
            return includePositiveSign ? "+" + result : result;
        }
        else
        {
            return "-" + ((Function<String, String>)FORMAT_JOULES[len - 1]).apply(raw.substring(1));
        }
    }
    
    @SuppressWarnings({ "unchecked" })
    public static String formatPower(long watts, boolean includePositiveSign)
    {
        if(watts == 0) return "0W";

        String raw = Long.toString(watts);
        int len = raw.length();
        
        if(watts > 0)
        {
            String result = ((Function<String, String>)FORMAT_WATTS[len]).apply(raw);
            return includePositiveSign ? "+" + result : result;
        }
        else
        {
            return "-" + ((Function<String, String>)FORMAT_WATTS[len - 1]).apply(raw.substring(1));
        }
    }

}
