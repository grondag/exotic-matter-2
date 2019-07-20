package grondag.hard_science.simulator.demand;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.storage.IStorageResourceListener;
import grondag.hard_science.simulator.storage.StorageManager;

public class InventoryProducer<V extends StorageType<V>> 
    implements IProducer<V>, IStorageResourceListener<V>
{
    /**
     * Broker that is tracking demand for our resource.
     * If null, means {@link #tearDown()} has run
     * and we should ignore any incoming requests.
     */
    private SimpleBroker<V> broker;
    
    /**
     * Resource we monitor, derived from broker
     */
    private final IResource<V> resource;
    
    private final StorageManager<V> storageManager;
    
    /**
     * True while we are filling demand - means we 
     * should ignore callbacks to {@link #onAvailabilityChange(IResource, long)} 
     * from storage system because we are causing them.
     * Would get into infinite loop otherwise.
     */
    private boolean itMe = false;
    
    public InventoryProducer(SimpleBroker<V> broker)
    {
        this.broker = broker;
        this.resource = broker.resource();
        this.storageManager = broker.brokerManager.getDomain().getCapability(resource.storageType().domainCapability());
        storageManager.registerResourceListener(broker.resource(), this);
    }
    
    public synchronized void tearDown()
    {
        storageManager.unregisterResourceListener(this.resource, this);
        this.broker = null;
    }
    
    @Override
    public void cancelWIP(NewProcurementTask<V> request)
    {
        // NOOP because inventory producer does not track WIP
    }

    @Override
    public void notifyNewDemand(IBroker<V> broker, NewProcurementTask<V> request)
    {
        assert this.broker != null
                : "Inventory Producer received new demand after tear down.";
                
        if(this.storageManager.getQuantityAvailable(this.resource) > 0)
        {
            this.fillDemand(request);
        }
    }

    /*
     * Synchronization is to prevent being called on a different thread
     * from {@link #fillDemand(IProcurementRequest)} so that we can 
     * reliably ignore callback from storage while we are allocating demand.
     */
    private synchronized void fillDemand(NewProcurementTask<V> request)
    {
        long demand = request.demandFor(this.resource);
        if(demand > 0)
        {
            this.itMe = true;
            long allocated = this.storageManager.changeAllocation(this.resource, demand, request);
            this.itMe = false;
            
            // found some inventory, so give it to the request
            if(allocated > 0)
            {
                long claimed = request.startWIP(this.resource, allocated, this);
                if(claimed > 0)
                {
                    request.completeWIP(this.resource, claimed, this);
                }
                
                if(claimed < allocated)
                {
                    // unwind if somehow all or part of the request got fulfilled some other way
                    this.itMe = true;
                    this.storageManager.changeAllocation(this.resource, claimed - allocated, request);
                    this.itMe = false;
                }
            }
        }
    }
    
    /**
     * Check for unmet demand we can fill if new inventory is added. <p>
     * 
     * Synchronization is to prevent being called on a different thread
     * from {@link #fillDemand(IProcurementRequest)} so that we can 
     * reliably ignore callback from storage while we are allocating demand.
     * 
     * {@inheritDoc}
     */
    @Override
    public synchronized void onAvailabilityChange(IResource<V> resource, long availableQuantity)
    {
        if(!itMe && availableQuantity > 0 && this.broker != null && !this.broker.requests.isEmpty())
        {
            for(NewProcurementTask<V> request : this.broker.requests)
            {
                this.fillDemand(request);
                if(this.storageManager.getQuantityAvailable(this.resource) == 0) break;
            }
        }
    }
}
