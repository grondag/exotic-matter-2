package grondag.hard_science.simulator.jobs;


/**
 * Callback interface for task status changes.
 */
public interface ITaskListener
{
    public void onTaskComplete(ITask task);
    
    /** 
     * Called when active task is cancelled. 
     * Default implementation calls {@link #onTaskComplete(ITask)}
     */
    public default void onTaskCancelled(ITask task) { this.onTaskComplete(task); }
    
    /** 
     * Called when task that is started or complete has to 
     * backtrack due to antecedent change. Default implementation
     * calls {@link #onTaskCancelled(ITask)}
     */
    public default void onTaskBackTracked(ITask task) { this.onTaskCancelled(task); }
}
