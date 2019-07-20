package grondag.hard_science.simulator.storage;

public interface ISizedContainer
{
    long getCapacity();
    long usedCapacity();
    default long availableCapacity()
    {
        return this.getCapacity() - this.usedCapacity();
    }
    
    default void setCapacity(long capacity)
    {
        throw new UnsupportedOperationException("Container does not support capacity configuration");
    }
}