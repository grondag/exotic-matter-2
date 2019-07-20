/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.hard_science;

import grondag.xm2.block.BlockHarvestTool;
import grondag.xm2.init.SubstanceConfig;

public class HsConfig {

//    @Comment("Enable tracing for machine jobs and processing. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logMachineActivity = false;

//    @Comment("Enable tracing for transport network activity. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logTransportNetwork = false;

//    @Comment("Enable tracing for excavation render tracking. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logExcavationRenderTracking = false;

//    @Comment("Enable tracing for structural device & device block changes. Highly verbose. Intended for dev environment and troubleshooeting.")
    public static boolean logDeviceChanges = false;

    public static void recalcDerived() {
        Machines.recalcDerived();
    }

    ////////////////////////////////////////////////////
    // SUBSTANCES
    ////////////////////////////////////////////////////
//    @LangKey("config.substance")
//    @Comment("Hard Science material properties.")
    public static Substances SUBSTANCES = new Substances();

    public static class Substances {
        public SubstanceConfig flexstone = new SubstanceConfig(2, BlockHarvestTool.PICK, 1, 10, 1.0);

        public SubstanceConfig durastone = new SubstanceConfig(4, BlockHarvestTool.PICK, 2, 50, 1.15);

        public SubstanceConfig hyperstone = new SubstanceConfig(10, BlockHarvestTool.PICK, 3, 200, 1.3);

        public SubstanceConfig flexiglass = new SubstanceConfig(2, BlockHarvestTool.PICK, 1, 10, 1.0);

        public SubstanceConfig duraglass = new SubstanceConfig(4, BlockHarvestTool.PICK, 2, 50, 1.15);

        public SubstanceConfig hyperglass = new SubstanceConfig(10, BlockHarvestTool.PICK, 3, 200, 1.3);

        public SubstanceConfig flexwood = new SubstanceConfig(2, BlockHarvestTool.AXE, 1, 10, 1.0).withFlammability(1);

        public SubstanceConfig durawood = new SubstanceConfig(4, BlockHarvestTool.AXE, 2, 50, 1.15);

        public SubstanceConfig hyperwood = new SubstanceConfig(10, BlockHarvestTool.AXE, 3, 200, 1.3);

        public SubstanceConfig hdpe = new SubstanceConfig(2, BlockHarvestTool.AXE, 1, 10, 1.0);
    }

    ////////////////////////////////////////////////////
    // MACHINES
    ////////////////////////////////////////////////////
//    @LangKey("config.machines")
//    @Comment("Settings for machines.")
    public static Machines MACHINES = new Machines();

    public static class Machines {
//        @Comment({"Machines display four-character random names. ",
//        "What could possibly go wrong?"})
        public boolean filterOffensiveMachineNames = true;

//        @Comment({"Radius for basic builder to find & build virtual blocks - in chunks.",
//        "0 means can only build in chunk where machine is located."})
//        @RequiresMcRestart
//        @RangeInt(min = 0, max = 8)
        public int basicBuilderChunkRadius = 4;

//        @Comment({"Number of milliseconds server waits between sending machine updates to clients.",
//            "Lower values will provide more responsive machine status feedback at the cost of more network traffic",
//        "Some specialized machines may not honor this value consistently."})
//        @RangeInt(min = 0, max = 5000)
        public int machineUpdateIntervalMilliseconds = 200;

//        @Comment({"Number of milliseconds between keepalive packets sent from client to server to notifiy ",
//            "server that machine is being rendered and needs status information for external display.",
//            "Values must match on both client and server for machine updates to work reliably!",
//        "Not recommended to change this unless you are trying to address a specific problem."})
//        @RangeInt(min = 1000, max = 30000)
        public int machineKeepaliveIntervalMilliseconds = 5000;

//        @Comment({"Number of milliseconds grace period gives before timing out listeners when no keepalive packet is received.",
//        "Lower values will sligntly reduce network traffice but are not recommended if any clients have high latency" })
//        @RangeInt(min = 100, max = 2000)
        public int machineLatencyAllowanceMilliseconds = 1000;

//        @Comment({"Track and display exponential average change in machine material & power buffers.",
//        "Disabling may slightly improve client performance. Has no effect on server."})
//        @RequiresMcRestart
        public boolean enableDeltaTracking = true;

//        @Comment({"You have to be this close to machines for external displays to render.",
//            "Visibility starts at this distance and then becomes full at 4 blocks less.",
//        "Lower values may improve performance in worlds with many machines."})
//        @RangeInt(min = 5, max = 16)
        public int machineMaxRenderDistance = 8;

//        @RequiresMcRestart
//        @Comment({"If true, machine simulation will periodically output performance statistics to log.",
//            "Does cause minor additional overhead and log spam so should generally only be enabled for testing.",
//        "Turning this off does NOT disable the minimal performance counting needed to detect simulation overload."})
        public boolean enablePerformanceLogging = false;

        public static int machineKeepAlivePlusLatency;

        private static void recalcDerived() {
            machineKeepAlivePlusLatency = MACHINES.machineKeepaliveIntervalMilliseconds + MACHINES.machineLatencyAllowanceMilliseconds;
        }
    }

    ////////////////////////////////////////////////////
    // PROCESSING
    ////////////////////////////////////////////////////
//    @LangKey("config.processing")
//    @Comment("Settings for automatic resource processing.")
    public static Processing PROCESSING = new Processing();

    public static class Processing {

//        @Comment({"Defines chemical composition of micronizer outputs/digester inputs",
//            "Comma separated parameters are... ",
//            "    item ingredient - ore dictionary accepted, metadata optional",
//            "    fluid name - defines the output", 
//            "    liters output - floating point value, 1 block = 1000L",
//            "    energy consumption factor - floating point value",
//            "Energy consumption factor and output volume determine energy usage.",
//            "Harder materials should have a higher energy consumption factor.",
//            "Smooth stone is suggested as the reference value at 1.0."})
//        @RequiresMcRestart
        public String[] micronizerOutputs = { "# Stone is modeled after approx earth crust composition.",
                "# Ends up being pretty close to a feldspar mineral, because trace components are small", "stone, 2.8, 0x646973", "Si, 0.282000000",
                "O, 0.461000000", "Fe, 0.056300000", "Al, 0.082300000", "Ca, 0.041500000", "K, 0.020900000", "Mg, 0.023300000", "Na, 0.023600000",
                "Ti, 0.005650000", "Mn, 0.000950000", "P, 0.001050000", "Zr, 0.000165000", "S, 0.000350000", "F, 0.000585000", "Nd, 0.000041500",
                "Cr, 0.000102000", "Cl, 0.000145000", "Ni, 0.000084000", "Zn, 0.000070000", "Cu, 0.000060000", "Pb, 0.000014000", "C, 0.000200000",
                "Co, 0.000025000", "H, 0.001400000", "Sn, 0.000002300", "N, 0.000019000", "W, 0.000001300", "Li, 0.000020000", "Mo, 0.000001200",
                "B, 0.000010000", "Ag, 0.000000075", "Au, 0.000000004", "Se, 0.000000050", "Pt, 0.000000005", "", "basalt, 3.7, 0x648090", "SiO2, 0.4",
                "Fe3O4, 0.6" };

//        @Comment({"Recipe configuration for micronizer.",
//            "Comma separated parameters are... ",
//            "    item ingredient - ore dictionary accepted, metadata optional",
//            "    fluid name - defines the output", 
//            "    liters output - floating point value, 1 block = 1000L",
//            "    energy consumption factor - floating point value",
//            "Energy consumption factor and output volume determine energy usage.",
//            "Harder materials should have a higher energy consumption factor.",
//            "Smooth stone is suggested as the reference value at 1.0."})
//        @RequiresMcRestart
        public String[] micronizerRecipes = { "ore:sand, stone, 1000.0, 0.5", "ore:gravel, stone, 1000.0, 0.65", "ore:sandstone, stone, 1000.0, 0.7",
                "ore:cobblestone, stone, 1000.0, 0.8", "ore:stone, stone, 1000.0, 1.0", "minecraft:cobblestone_wall, stone, 1000.0, 0.8",
                "minecraft:stone_slab:0, stone, 500.0, 0.8", "minecraft:stone_slab:1, stone, 500.0, 0.77", "minecraft:stone_slab:3, stone, 500.0, 1.0",
                "minecraft:stone_slab:5, stone, 500.0, 1.0", "minecraft:stone_slab2:0, stone, 500.0, 0.7", "hard_science:basalt_cobble, basalt, 1000.0, 1.0",
                "hard_science:basalt_cut, basalt, 1000.0, 1.2", "hard_science:basalt_rubble, basalt, 111.11111, 1.0" };

//        @Comment({"Recipe configuration for digester.",
//            "Each row lists a bulk resource (which are fluids in game)",
//            "Other inputs, energy usage and outputs are automatically derived",
//            "from the chemical composition of the input resource.",
//            "Air, water and electricityare used to generate nitric acid within the device.",
//            "Some of this is output in the form of nitrates.",
//            "All other reactants/catalysts are recovered/regenerated within the digester."})
//        @RequiresMcRestart
//        public String[] digesterInputs =
//        {
//            "micronized_stone",
//            "micronized_basalt"
//        };

//        @Comment({"Ouput digester analysis debug information to log. Intended for testing."})
        public boolean enableDigesterAnalysisDebug = true;

//        @Comment({"Output warning message if digester analysis violates physical constraints. "})
        public boolean enableDigesterRecipeWarnings = true;

//        @Comment({"Default reserve and target stocking levels",
//            "for new domains. Has no effect after a domain is created.",
//            "Listed resources represent inputs or outputs of automatic production.",
//            "When resource level drops below target, production starts.",
//            "If resource drops below reserve, it is no longer used as an input",
//            "for automated resource processing, but can still be used for player request.",
//            "Fluid resources are given in liters (equivalent to millibucket).",
//            "Order is fluid name, reserveStockLevel, targetStockLevel"})
        public String[] fluidResourceDefaults = { "micronized_basalt, 16000, 64000", "micronized_stone, 16000, 64000", "co2_gas, 16000, 64000",
                "h2_gas, 16000, 64000", "ethene_gas, 16000, 64000", "return_air, 16000, 64000", "flex_resin, 16000, 64000", "h2o_vapor, 16000, 64000",
                "graphite, 16000, 64000", "flex_alloy, 16000, 64000", "lithium, 16000, 64000", "platinum, 16000, 64000", "tin, 16000, 64000",
                "dye_cyan, 16000, 64000", "raw_mineral_dust, 16000, 64000", "zinc, 16000, 64000", "potassium_nitrate, 16000, 64000",
                "magnesium_nitrate, 16000, 64000", "phosphorus, 16000, 64000", "molybdenum, 16000, 64000", "tungsten_powder, 16000, 64000",
                "calcium_nitrate, 16000, 64000", "dye_yellow, 16000, 64000", "ammonia_liquid, 16000, 64000", "magnesium, 16000, 64000", "water, 16000, 64000",
                "sodium, 16000, 64000", "ar_gas, 16000, 64000", "mineral_filler, 16000, 64000", "fresh_air, 16000, 64000", "silver, 16000, 64000",
                "n2_gas, 16000, 64000", "ethanol_liquid, 16000, 64000", "silicon_nitride, 16000, 64000", "hdpe, 16000, 64000", "ammonia_gas, 16000, 64000",
                "potassium, 16000, 64000", "silica, 16000, 64000", "titanium, 16000, 64000", "sodium_chloride, 16000, 64000", "cobalt, 16000, 64000",
                "aluminum, 16000, 64000", "manganese, 16000, 64000", "dura_resin, 16000, 64000", "calcium_fluoride, 16000, 64000", "gold, 16000, 64000",
                "copper, 16000, 64000", "dye_magenta, 16000, 64000", "nickel, 16000, 64000", "magnetite, 16000, 64000", "chromium, 16000, 64000",
                "calcium, 16000, 64000", "perovskite, 16000, 64000", "methane_gas, 16000, 64000", "calcium_carbonate, 16000, 64000", "sulfer, 16000, 64000",
                "flex_fiber, 16000, 64000", "o2_gas, 16000, 64000", "super_fuel, 16000, 64000", "sodium_nitrate, 16000, 64000", "carbon_vapor, 16000, 64000",
                "lead, 16000, 64000", "diamond, 16000, 64000", "silicon, 16000, 64000", "neodymium, 16000, 64000", "h2o_fluid, 16000, 64000",
                "iron, 16000, 64000", "boron, 16000, 64000", "monocalcium_phosphate, 16000, 64000", "lithium_nitrate, 16000, 64000" };

//        @Comment({"Default reserve and target stocking levels",
//            "for new domains. Has no effect after a domain is created.",
//            "Listed resources represent inputs or outputs of automatic production.",
//            "When resource level drops below target, production starts.",
//            "If resource drops below reserve, it is no longer used as an input",
//            "for automated resource processing, but can still be used for player request.",
//            "Target = 0 means resource should not be retained if there is demand.",
//            "If an item satisfies multiple ingredient, the highest reserve/target value applies.",
//            "Item resources are given in plain counts. (1 = 1 item)",
//            "Order is item name, reserveStockLevel, targetStockLevel.",
//            "Use ore prefix for oredictionary ingredients."})
        public String[] itemResourceDefaults = { "ore:sand, 64, 512", "ore:gravel, 64, 256", "ore:sandstone, 64, 256", "ore:cobblestone, 64, 512",
                "ore:stone, 64, 256", "minecraft:cobblestone_wall, 64, 64", "minecraft:stone_slab:0, 64, 64", "minecraft:stone_slab:1, 64, 64",
                "minecraft:stone_slab:3, 64, 64", "minecraft:stone_slab:5, 64, 64", "minecraft:stone_slab2:0, 64, 64", "hard_science:basalt_cobble, 64, 256",
                "hard_science:basalt_cut, 64, 256", "hard_science:basalt_rubble, 0, 0", "hard_science:basalt_cool_static_height, 0, 0",
                "hard_science:basalt_cool_static_filler, 0, 0" };

    }
}
