package grondag.hard_science.simulator.transport.management;

import grondag.hard_science.simulator.transport.endpoint.PortMode;

/**
 * Captures expected result of attempt to mate two ports.
 * Is result type for {@link LogisticsService#connect(grondag.hard_science.simulator.transport.endpoint.Port, grondag.hard_science.simulator.transport.endpoint.Port)}
 */
public enum ConnectionResult
{
    FAIL_STORAGE_TYPE(PortMode.NO_CONNECTION_STORAGE_TYPE, PortMode.NO_CONNECTION_STORAGE_TYPE),
    
    FAIL_CHANNEL_MISMATCH(PortMode.NO_CONNECTION_CHANNEL_MISMATCH, PortMode.NO_CONNECTION_CHANNEL_MISMATCH),

    FAIL_LEVEL_GAP(PortMode.NO_CONNECTION_LEVEL_GAP, PortMode.NO_CONNECTION_LEVEL_GAP),

    FAIL_INCOMPATIBLE(PortMode.NO_CONNECTION_INCOMPATIBLE, PortMode.NO_CONNECTION_INCOMPATIBLE),

    /**
     * must have shared circuit, three scenarios
     * 1) has a circuit, other does not - extend to both
     * 2) neither has a circuit, create new use by both
     * 3) have different circuits, must merge
     */
    CARRIER_CARRIER(PortMode.CARRIER, PortMode.CARRIER, true),

    /**
     * direct will use carrier circuit, two scenarios:
     * 1) carrier already has a circuit, use it
     * 2) carrier doesn't have a circuit yet, create it
     */
    CARRIER_DIRECT(PortMode.CARRIER, PortMode.DIRECT),

    /**
     * Same as {@link #CONNECT_CARRIER_DIRECT} with reverse port order
     */
    DIRECT_CARRIER(PortMode.DIRECT, PortMode.CARRIER),

    /**
     * Bridge external circuit will be shared with carrier port.
     * Bridge will have a separate internal circuit.
     * Merging is not a possibility, but if either side does not
     * yet have an internal circuit, then will need to be created.
     * This give four (trivial) scenarios:
     * 1) both have circuits already
     * 2, 3) One side needs a circuit created.
     * 4) both sides need a circuit created
     */
    CARRIER_BRIDGE(PortMode.CARRIER, PortMode.BRIDGE),

    /**
     * Same as {@link #CONNECT_CARRIER_BRIDGE} but reverse port order.
     */
    BRIDGE_CARRIER(PortMode.BRIDGE, PortMode.CARRIER),

    /**
     * Bridge external circuit will be a separate circuit.
     * Bridge needs to have an internal circuit.
     * Merging is not a possibility.
     * This give two (trivial) scenarios:
     * 1) Bridge has an internal circuit already
     * 2, Bridge needs an internal circuit created.
     * In both cases, a new external circuit is created and 
     * shared by both ports.
     */
    DIRECT_BRIDGE(PortMode.DIRECT, PortMode.BRIDGE),

    /**
     * Same as {@link #CONNECT_DIRECT_BRIDGE} but reverse port order.
     */
    BRIDGE_DIRECT(PortMode.BRIDGE, PortMode.DIRECT);
    
    public final PortMode left;
    public final PortMode right;
    public final boolean isConnected;
    /**
     * If true, external circuits of ports should be merged
     * if both are non-null and not the same.  If false,
     * then at least one side will always be null or not
     * possible for them to be different.
     */
    public final boolean allowMerge;
    
    private ConnectionResult(PortMode left, PortMode right)
    {
        this(left, right, false);
    }
    
    private ConnectionResult(PortMode left, PortMode right, boolean allowMerge)
    {
        this.left = left;
        this.right = right;
        this.isConnected = left.isConnected && right.isConnected;
        this.allowMerge = allowMerge;
    }
}
