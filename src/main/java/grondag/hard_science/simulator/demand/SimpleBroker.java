package grondag.hard_science.simulator.demand;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Default broker for discrete resources that don't have specialized brokers/producers.
 */
public class SimpleBroker<V extends StorageType<V>> extends AbstractBroker<V>
{
    private final InventoryProducer<V> inventoryProducer;
    
    private final IResource<V> resource;
    
    public SimpleBroker(BrokerManager brokerManager, IResource<V> resource)
    {
        super(brokerManager);
        
        // must happen before anything that might reference it
        this.resource = resource;
        
        this.inventoryProducer = new InventoryProducer<V>(this);
        this.registerProducer(this.inventoryProducer);
    }
    
    @Override
    public synchronized void unregisterRequest(NewProcurementTask<V> request)
    {
        super.unregisterRequest(request);
        this.checkForTearDown();
    }

    @Override
    public synchronized void unregisterProducer(IProducer<V> producer)
    {
        super.unregisterProducer(producer);
        this.checkForTearDown();
    }

    /**
     * Removes this broker and the associated inventory producer
     * when there are no longer any active requests or producers
     * (except for the inventory producer)
     */
    public void checkForTearDown()
    {
        if(this.requests.isEmpty() && this.producers.size() == 1)
        {
            this.inventoryProducer.tearDown();
            this.producers.clear();
            this.brokerManager.removeSimpleBroker(this);
        }
       
    }
    
    public IResource<V> resource()
    {
        return this.resource;
    }
}
