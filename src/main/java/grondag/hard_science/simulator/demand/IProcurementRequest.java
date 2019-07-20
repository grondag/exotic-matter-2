package grondag.hard_science.simulator.demand;

import java.util.List;

import grondag.hard_science.simulator.jobs.ITask;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Implement in all resource procurement tasks.  Specifies
 * the methods needed by resource brokers and storage manager
 * to track and fulfill all resource demands.
 */
public interface IProcurementRequest <V extends StorageType<V>> extends ITask
{
    
    /**
     * Resources and amounts that are allocated to this request.
     * These are always actual resources.
     * Order here has no meaning. 
     */
    public List<AbstractResourceWithQuantity<V>> allocatedDemands();
    
    /**
     * Called by a producer when starting to produce a resource for this request.
     * Will cause openDemands to refresh (no longer includes the WIP.)
     * Retains a reference to producer so can notify producer is request is cancelled.
     * Returns a positive, non-zero quantityIn of WIP successfully claimed.
     * Returns zero if no WIP could be claimed.
     */
    public long startWIP(IResource<V> resource, long startedQuantity, IProducer<V> producer);
    
    /**
     * Called by a producer if it can no longer continue with WIP.
     * Will cause openDemands to refresh (add back the cancelled WIP.)
     */
    public void cancelWIP(IResource<V> resource, long cancelledQuantity, IProducer<V> producer);
    
    /**
     * Called by a producer when WIP is completed and available in inventory.
     * The producer will allocate the resource to the request when it places the resource into storage.
     * The amount provided will reduce WIP and increase allocated demand.
     * If WIP is zero, this request will stop tracking WIP for this producer.
     */
    public void completeWIP(IResource<V> resource, long completedQuantity, IProducer<V> producer);
    
    /**
     * Called by storage system if an allocated resource becomes unavailable.
     * Will cause open and allocated demands to refresh.  Should update broker when this occurs.
     */
    public void breakAllocation(IResource<V> resource, long newAllocation);

    /**
     * Called by the owner of this request when it accepts delivery of resources
     * allocated by this request.  Will reduce allocation.<p>
     * 
     * Does NOT notify storage system to reduce allocation - passing this request
     * to all storage operations will cause the storage system to update 
     * allocations automatically. For example, if the owner withdraws an
     * allocated resource from storage and does not put it back, storage manager
     * will automatically reflect the reduced allocation.
     */
    public void reportDelivery(IResource<V> resource, long deliveredQuantity);
    
    /**
     * Returns maximum quantity of the given resource that could be used
     * by this request.  Does not include allocated or delivered amounts.
     * Not guaranteed request will still need it if 
     * allocation is later requested, because of concurrency.
     * Will return zero if the resource cannot be used to satisfy this request.<p>
     * 
     * Value does not decrease as resources are delivered. 
     * For that, see {@link #deliveredQuantity(IResource)}
     */
    public long demandFor(IResource<V> resource);
    
    /**
     * Quantity of the given resource that has been delivered by this request.
     * Not included in open demand or allocations.
     */
    public long deliveredQuantity(IResource<V> resource);
    
}
