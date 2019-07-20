package grondag.hard_science.simulator.transport;

public enum TransportMode
{
    /**
     * Transport via connected bus without packaging.
     * Requires that from node be able to send raw bulkResource
     * and to node can accept raw bulkResource.
     */
    CONNECTED_DIRECT,
    
    /**
     * Transport via connected bus inside a package.
     * Used when bulkResource was already packaged for some 
     * other reason (previous leg of trip) or because
     * receiver requires it. (Hold in storage for drone pickup, for example.)
     */
    CONNECTED_PACKAGED,
    
    /**
     * Drone will pick up at start node and drop off at target node.
     * Must always be packaged.
     */
    DRONE_PACKAGED;
    
    public boolean isConnected() { return this != DRONE_PACKAGED; }
}
