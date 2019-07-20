package grondag.exotic_matter.simulator;

//TODO: move to Timeshare
public interface ISimulationTickable
{
    /**
     * If true, then {@link #doOnTick(int)} will be called during 
     * world tick from server thread. Is generally only checked 
     * at setup so result should not be dynamic.
     */
    public default boolean doesUpdateOnTick() { return false; }
    
    /**
     * See {@link #doesUpdateOnTick()}
     */
    public default void doOnTick() {}
    
    /**
     * If true, then {@link #doOffTick(int)} will be called once per server tick 
     * from simulation thread pool. Is generally only checked 
     * at setup so result should not be dynamic.
     */
    public default boolean doesUpdateOffTick() { return false; }
    
    /**
     * See {@link #doesUpdateOffTick()}
     */
    public default void doOffTick() {}
 }
