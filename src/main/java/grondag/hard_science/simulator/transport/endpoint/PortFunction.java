package grondag.hard_science.simulator.transport.endpoint;

public enum PortFunction
{
    /**
     * Carrier port are attached to an internal carrier within the device.
     * Device might have other ports also attached to the same carrier.
     * Carrier packets can pass through the device through other mated
     * carrier ports of the same level.<p>
     * 
     * This is the base port type for most simple machines.
     * Allows machines to be placed adjacent and form a simple bus with
     * a common carrier circuit. <p>
     * 
     * All rules for carrier ports apply to the ports within the device
     * and to ports attached to the same carrier outside the device: 
     * Must be same carrier type, same channel, limited number of 
     * device blocks per carrier. (Except at top level.)<p>
     * 
     * Can mate with carrier ports and direct ports of same level or
     * with a bridge port of the next highest level. 
     * 
     * When used to cross-connect top-level switches, these ports have no
     * channel (because top-level switches have no channels) and can only
     * connect two end points, directly, with no branching.  This constraint
     * is in place for item transport: items traveling at supersonic speeds
     * in a vacuum tube can only change direction (except for gentle curves)
     * within the switch itself.
     */
    CARRIER,
    
    /**
     * A direct port has no internal carrier and must mate with a carrier port.
     * Direct ports are isolated from other ports on the same
     * device and represents a "direct" attachment to that device.<p>
     * 
     * This port type is only used for machines that need high capacity I/O.<p>
     * 
     * Direct ports can only mate with carrier ports of the same level 
     * (including same-level bridge ports in carrier mode) or with
     * bridge ports of the next level up in active bridge mode. 
     * They always rely on an external carrier, and cannot be used 
     * to form a bus or circuit on their own.<p>
     */
    DIRECT,
    
    /**
     * Bridge ports are carrier ports that can also "bridge" to 
     * carrier or direct ports of the next-lower capacity level. 
     * The most flexible ports, they only occur on intermediate
     * and top-tier connectivity devices.<p>
     * 
     * When connected to Carrier ports of the same level,
     * they behave identically to a Carrier port.
     * 
     * When connected to a Carrier port of the next level down, 
     * bridge ports will join the circuit of the mated carrier
     * port as its external circuit and can pass traffic to/from its
     * device's internal circuit. <p>
     * 
     * When two bridge ports of the same level connect, they both
     * behave like carrier ports.<p>
     * 
     * When two bridge ports with a one-level difference connect,
     * the upper-level port acts as a bridge, and the lower-level
     * port acts as a carrier port.<p>
     * 
     * When connected to a direct port of the same level, acts 
     * like a carrier port.  If connected to a direct port one
     * level lower, then provides a dedicated external circuit
     * (at lower level) for the direct port.  This external 
     * circuit is isolated from all other circuits and
     * cannot have any channel conflict.<p>
     * 
     * Channel rules apply to bridge ports only when they are
     * acting as carrier ports.  When acting as bridge ports, the 
     * internal and external circuits are separate so no channel
     * conflict is possible. 
     */
    BRIDGE;
    
}
