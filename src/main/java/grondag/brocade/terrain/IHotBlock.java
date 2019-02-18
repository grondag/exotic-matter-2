package grondag.brocade.terrain;

public interface IHotBlock
{
    /**
     * Highest heat value that can be returned from {@link #heatLevel()}.
     * Corresponds to molten lava.
     */
    public static final int MAX_HEAT = 5;
    
    /**
     * Count of allowed values returned from {@link #heatLevel()}, including zero.
     * Equivalently, {@link #MAX_HEAT} + 1;
     */
    public static final int HEAT_LEVEL_COUNT = MAX_HEAT + 1;
    
    public default int heatLevel() { return 0; }
    
    public default boolean isHot() { return this.heatLevel() != 0; }
    
   
}
