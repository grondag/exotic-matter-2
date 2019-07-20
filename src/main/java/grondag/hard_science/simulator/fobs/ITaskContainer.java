package grondag.hard_science.simulator.fobs;

import grondag.exotic_matter.simulator.domain.IDomainMember;

/**
 * Interface for classes that are containers for tasks.
 * Task containers own priority, propagation of priority changes,
 * propagation of "job"-level status changes, and serialization
 * (if applicable)
 */
public interface ITaskContainer extends IDomainMember
{
    TaskPriority priority();
}
