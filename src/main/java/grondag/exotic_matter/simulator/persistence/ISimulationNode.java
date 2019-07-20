package grondag.exotic_matter.simulator.persistence;

import grondag.exotic_matter.simulator.Simulator;

/**
 * Lifecycle events for simulator.  Simulator calls top-level nodes
 * directly.  Top-level nodes and sub containers are responsible 
 * for cascading calls to child instances.
 *
 */
public interface ISimulationNode extends IDirtListener
{
    /**
     * Called by simulator at shutdown
     */
    public default void unload() {}
    
    /**
     * Called immediately after object is instantiated and added to parent collection.
     * Will <em>always</em> be called, and will be followed by {@link #afterDeserialization()} or {@link #loadNew()}. <p>
     * 
     * Difference from constructor is that can be used to distinguish instances created by the simulator vs
     * an instance created by a test harness or for some other purpose.
     */
    public default void afterCreated(Simulator sim) {};
    
    /**
     * Called by simulator after all top-level nodes are deserialized (if anything found) 
     * but before first simulation tick.  This is the time to re-create references to other nodes if needed.
     */
    public default void afterDeserialization() {}
    
    /**
     * Called by simulator if starting new world/simulation.
     */
    public default void loadNew() {}
    
    /**
     * If true, won't be serialized and on reload
     * will have a new (empty) instance.
     */
    public default boolean isSerializationDisabled() { return false; }
}
