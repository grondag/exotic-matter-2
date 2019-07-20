package grondag.hard_science.simulator.fobs;

import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.simulator.demand.IProducer;
import grondag.hard_science.simulator.demand.InventoryProducer;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public class SimpleProcurementTask<T extends StorageType<T>> extends NewProcurementTask<T>
{
    private IResource<T> resource;
    private long demandQuantity = 0;
    private long allocatedQuantity = 0;
    private long deliveredQuantity = 0;
    /**
     * Not serialized - producers are expected to re-declare WIP on reload.
     */
    private long wipQuantity = 0;
    
    public SimpleProcurementTask(ITaskContainer container, IResource<T> resource, long quantity)
    {
        super(container);
        this.resource = resource;
        this.demandQuantity = quantity;
    }
    
    @Override
    public void breakAllocation(IResource<T> resource, long newAllocation)
    {
        assert this.status() == RequestStatus.ACTIVE || this.status() == RequestStatus.COMPLETE
                : "breakAllocation called while task not ACTIVE or COMPLETE.";

        synchronized(this)
        {
            long reduction = this.allocatedQuantity - newAllocation;
            if(reduction > 0) this.allocatedQuantity -= reduction;
        }
        this.notifyDemandChange();
    }

    @Override
    public long demandFor(IResource<T> resource)
    {
        return this.resource.isResourceEqual(resource)
                ? this.demandQuantity - this.allocatedQuantity - this.deliveredQuantity : 0;
    }

    @Override
    public long startWIP(IResource<T> resource, long startedQuantity, InventoryProducer<T> inventoryProducer)
    {
        synchronized(this)
        {
            assert this.status() == RequestStatus.ACTIVE
                    : "AbstractSimpleProcurmentTask.startWIP called when status != ACTIVE";
            
            startedQuantity = Useful.clamp(this.demandQuantity - this.allocatedQuantity - this.wipQuantity, 0, startedQuantity);
            this.wipQuantity += startedQuantity;
        }
        this.notifyDemandChange();
        return startedQuantity;
    }

    @Override
    public void cancelWIP(IResource<T> resource, long cancelledQuantity, IProducer<T> producer)
    {
        if(cancelledQuantity == 0) return;
        
        assert cancelledQuantity >= 0
                : "cancelWIP received negative value.";
        synchronized(this)
        {
            assert cancelledQuantity >= wipQuantity
                    : "cancelWIP received value greather than current WIP value.";
            
            assert this.status() == RequestStatus.ACTIVE
                    : "cancelWIP called when status != ACTIVE";
            
            this.wipQuantity = Math.max(0, this.wipQuantity - cancelledQuantity);
        }
        this.notifyDemandChange();
    }

    @Override
    public void completeWIP(IResource<T> resource, long completedQuantity, InventoryProducer<T> inventoryProducer)
    {
        if(completedQuantity == 0) return;

        assert completedQuantity >= 0
                : "completeWIP receive negative value.";
        
        assert this.status() == RequestStatus.ACTIVE
                : "completeWIP called when status != ACTIVE";
        
        synchronized(this)
        {
            this.allocatedQuantity += completedQuantity;
            this.wipQuantity -= completedQuantity;
        }
    }
}
