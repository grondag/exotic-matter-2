package grondag.hard_science.simulator.fobs;

import grondag.exotic_matter.simulator.job.RequestStatus;

public interface INewTaskListener
{
     
    /** Call by procurement tasks after demands have changed */
    public default void notifyDemandChange(NewTask task) {};
    
    /** Call by tasks after task status has changed */
    public default void notifyStatusChange(NewTask task, RequestStatus oldStatus) {};

    /** Call by tasks after priority has changed */
    public default void notifyPriorityChange(NewTask task, TaskPriority oldPriority) {};
}
