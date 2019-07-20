package grondag.hard_science.simulator.fobs;

import grondag.hard_science.simulator.demand.IProducer;
import grondag.hard_science.simulator.demand.InventoryProducer;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public abstract class NewProcurementTask<V extends StorageType<V>> extends NewTask
{
    protected NewProcurementTask(ITaskContainer container)
    {
        super(container);
    }

    protected void notifyDemandChange()
    {
        this.listeners().forEach(l -> l.notifyDemandChange(this));
    }
    
    /**
     * Called by storage system if an allocated resource becomes unavailable.
     * Will cause open and allocated demands to refresh.  Should update broker when this occurs.
     */
    public abstract void breakAllocation(IResource<V> resource, long newAllocation);

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
    public abstract long demandFor(IResource<V> resource);

    /**
     * Called by a producer when starting to produce a resource for this request.
     * Returns a positive, non-zero quantityIn of WIP successfully claimed.
     * Returns zero if no WIP could be claimed.<p>
     * 
     * Producer should listen to this task if it wants to cancel production
     * if the task is cancelled.
     */
    public abstract long startWIP(IResource<V> resource, long startedQuantity, InventoryProducer<V> inventoryProducer);

    /**
     * Called by a producer if it can no longer continue with WIP.
     * Will cause {@link #demandFor(IResource)} to include the canceled WIP.
     */
    public abstract void cancelWIP(IResource<V> resource, long cancelledQuantity, IProducer<V> producer);
    
    /**
     * Called by a producer when WIP is completed and available in inventory.
     * The producer will allocate the resource to the request when it places the resource into storage.
     * The amount provided will reduce WIP and increase allocated demand.
     */
    public abstract void completeWIP(IResource<V> resource, long completedQuantity, InventoryProducer<V> inventoryProducer);
}
