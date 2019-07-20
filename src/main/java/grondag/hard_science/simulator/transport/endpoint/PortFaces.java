package grondag.hard_science.simulator.transport.endpoint;

import static grondag.hard_science.simulator.transport.endpoint.PortDescriptions.*;
public class PortFaces
{
    public static final PortFace EMPTY_FACE = PortFace.find();

    /**
     * Used for most single block machines.
     * Includes compact power port to allow easy
     * connection to solar panels.
     */
    public static final PortFace UTB_LOW_CARRIER = PortFace.find(
            ITEM_LOW_CARRIER,
            POWER_LOW_CARRIER,
            FLUID_LOW_CARRIER);
    
    /**
     * For sides of full-block machines that could connect with PE cells.
     */
    public static final PortFace UTB_LOW_CARRIER_WITH_COMPACT_POWER = PortFace.find(
            ITEM_LOW_CARRIER,
            POWER_LOW_CARRIER,
            POWER_LOW_CARRIER_COMPACT,
            FLUID_LOW_CARRIER);
 
    /**
     * For machines that only need item and power support.
     */
    public static final PortFace NON_FLUID_LOW_CARRIER = PortFace.find(
            ITEM_LOW_CARRIER,
            POWER_LOW_CARRIER);
    
    public static final PortFace UTB_MID_CARRIER = PortFace.find(
            ITEM_MID_CARRIER,
            POWER_MID_CARRIER,
            FLUID_MID_CARRIER);
    
    public static final PortFace UTB_MID_BRIDGE = PortFace.find(
            ITEM_MID_BRIDGE,
            POWER_MID_BRIDGE,
            FLUID_MID_BRIDGE);
    
    public static final PortFace UTB_TOP_BRIDGE = PortFace.find(
            ITEM_TOP_BRIDGE,
            POWER_TOP_BRIDGE,
            FLUID_TOP_BRIDGE);
    
    public static final PortFace UTB_TOP_CARRIER = PortFace.find(
            ITEM_TOP_CARRIER,
            POWER_TOP_CARRIER,
            FLUID_TOP_CARRIER);
    
    public static final PortFace STD_POWER_LOW_CARRIER = PortFace.find(
            POWER_LOW_CARRIER);
    
    public static final PortFace COMPACT_POWER_LOW_CARRIER = PortFace.find(
            POWER_LOW_CARRIER_COMPACT);
    
    public static final PortFace FLEX_POWER_LOW_CARRIER = PortFace.find(
            POWER_LOW_CARRIER,
            POWER_LOW_CARRIER_COMPACT);
}
