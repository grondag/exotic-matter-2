package grondag.hard_science.simulator.accounting;

//WIP
public interface ITimed
{
    /**
     * Tick when job started or should start.
     * Will be 0 if job has not started and should start as soon as possible. 
     */
    public int getStartTick();
    
    /**
     * Number of ticks job took (including waits) or is expected to take if not complete.
     */
    public int getDurationTicks();
}
