//package grondag.hard_science.simulator.jobs.tasks;
//
//import java.util.List;
//
//import com.google.common.collect.ImmutableList;
//
//import grondag.hard_science.simulator.demand.IBroker;
//import grondag.hard_science.simulator.demand.IProcurementRequest;
//import grondag.hard_science.simulator.demand.IProducer;
//import grondag.hard_science.simulator.jobs.AbstractTask;
//import grondag.hard_science.simulator.jobs.RequestStatus;
//import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
//import grondag.hard_science.simulator.resource.ITypedStorage;
//import grondag.hard_science.simulator.resource.StorageType;
//import grondag.hard_science.simulator.storage.StorageManager;
//import it.unimi.dsi.fastutil.objects.AbstractObject2LongMap;
//import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
//
//public abstract class ProcurementTask<V extends StorageType<V>> extends AbstractTask implements IProcurementRequest<V>, ITypedStorage<V>
//{
//    /**
//     * Not serialized - producers are expected to re-declare WIP on reload.
//     */
//    private Object producers = null;
//    
//    protected ProcurementTask(boolean isNew)
//    {
//        super(isNew);
//    }
//    
//    protected abstract IBroker<V> broker();
//   
//    
//    /**
//     * Will automatically register this request with broker when
//     * status changes to ACTIVE for any reason.<p>
//     * 
//     * Will automatically un-register this request with broker 
//     * AND cancel WIP with any producers when status changes from
//     * ACTIVE to any other status for any reason.<p>
//     * 
//     * Also changes any incoming READY status to ACTIVE because
//     * procurement requests self-register with brokers instead of being 
//     * claimed by producers.<p>
//     * 
//     * Changing to an inactive status releases all allocations.<p>
//     * 
//     * {@inheritDoc}
//     */
//    @Override
//    protected synchronized void setStatus(RequestStatus newStatus)
//    {
//        RequestStatus oldStatus = this.status;
//        
//        if(newStatus == RequestStatus.READY) newStatus = RequestStatus.ACTIVE;
//        
//        super.setStatus(newStatus);
//        
//        if(newStatus == RequestStatus.ACTIVE && oldStatus != RequestStatus.ACTIVE)
//        {
//            this.broker().registerRequest(this);
//        }
//        else if(!oldStatus.isTerminated && newStatus.isTerminated)
//        {
//            this.abandonBroker();
//            this.cancelProducerWIP();
//            
//            // release any allocated resources
//            if(!this.allocatedDemands().isEmpty())
//            {
//                this.storageType().service().executor.execute(() -> 
//                {
//                    // snapshot because we may clear it before this runs
//                    final List<AbstractResourceWithQuantity<V>> targets 
//                        = ImmutableList.copyOf(this.allocatedDemands());
//                    
//                    StorageManager<V> storageManager = this.getDomain().getStorageManager(this.storageType());
//                    for(AbstractResourceWithQuantity<V> rwq : targets)
//                    {
//                        storageManager.setAllocation(rwq.resource(), this, 0);
//                    }
//                }, false);
//            }
//            
//            this.clearAllocatedDemand();
//        }
//    }
//    /**
//     * Tracks WIP quantityIn for a producer. 
//     * Does NOT update total WIP count, because not all implementation have that concept.
//     */
//    private synchronized void setProducerWIP(IProducer<V> producer, long wip)
//    {
//        assert wip >= 0
//                : "SimpleProcurementTask.setProducerWIP encountered negative WIP value.";
//        
//        if(this.producers == null)
//        {
//            // start producer, use a single entry object
//            if(wip > 0)
//            {
//                this.producers = new AbstractObject2LongMap.BasicEntry<IProducer<V>>(producer, wip);
//            }
//        }
//        else if(this.producers instanceof AbstractObject2LongMap.BasicEntry<?>)
//        {
//            // using single entry object
//            
//            @SuppressWarnings("unchecked")
//            AbstractObject2LongMap.BasicEntry<IProducer<V>> entry 
//            = (AbstractObject2LongMap.BasicEntry<IProducer<V>>)this.producers;
//
//            if(entry.getKey() == producer)
//            {
//                // already tracking this producer
//                if(wip == 0)
//                {
//                    this.producers = null;
//                }
//                else
//                {
//                    entry.setValue(wip);
//                }
//            }
//            else if(wip > 0)
//            {
//                // new producer
//                // upgrade to map if wip is non-zero
//                Object2LongOpenHashMap<IProducer<V>> map = new Object2LongOpenHashMap<IProducer<V>>();
//                map.put(entry.getKey(), entry.getLongValue());
//                map.put(producer, wip);
//                this.producers = map;
//            }
//        }
//        else
//        {
//            // already using map
//            @SuppressWarnings("unchecked")
//            Object2LongOpenHashMap<IProducer<V>> map = (Object2LongOpenHashMap<IProducer<V>>)this.producers;
//            
//            if(wip == 0)
//            {
//                map.removeLong(producer);
//                if(map.isEmpty()) this.producers = null;
//            }
//            else
//            {
//                map.put(producer, wip);
//            }
//        }
//    }
//    
//    
//    @SuppressWarnings("unchecked")
//    protected synchronized long getProducerWIP(IProducer<V> producer)
//    {
//        if(this.producers == null)
//        {
//            return 0;
//        }
//        else if(this.producers instanceof AbstractObject2LongMap.BasicEntry<?>)
//        {
//            AbstractObject2LongMap.BasicEntry<IProducer<V>> entry 
//            = (AbstractObject2LongMap.BasicEntry<IProducer<V>>)this.producers;
//            return entry.getKey() == producer ? entry.getLongValue() : 0;
//        }
//        else
//        {
//            return ((Object2LongOpenHashMap<IProducer<V>>)this.producers).getLong(producer);
//        }
//    }
//    
//    /**
//     * Convenient alternative to calling {@link #getProducerWIP(IProducer)}, 
//     * adding or subtracting, and then calling {@link #setProducerWIP(IProducer, long)}.
//     * Effects are combination of those two methods.
//     */
//    protected synchronized void changeProducerWIP(IProducer<V> producer, long delta)
//    {
//        long wip = this.getProducerWIP(producer) + delta;
//        
//        assert wip >= 0
//                : "ProcurementTask encountered request to reduce producer WIP below zero.";
//        
//        this.setProducerWIP(producer, wip);
//    }
//    
//    /**
//     * Cancels WIP for all producers, providing notification to the producers. 
//     * Does NOT update total WIP count or make all WIP demand into open demand
//     * because that logic is implementation-specific.
//     */
//    protected synchronized void cancelProducerWIP()
//    {
//        if(this.producers == null) 
//        {
//            return;
//        }
//        else if(this.producers instanceof AbstractObject2LongMap.BasicEntry<?>)
//        {
//            // using single entry object
//            
//            @SuppressWarnings("unchecked")
//            AbstractObject2LongMap.BasicEntry<IProducer<V>> entry 
//            = (AbstractObject2LongMap.BasicEntry<IProducer<V>>)this.producers;
//
//            entry.getKey().cancelWIP(this);
//            
//            this.producers = null;
//        }
//        else
//        {
//            // using map
//            @SuppressWarnings("unchecked")
//            Object2LongOpenHashMap<IProducer<V>> map = (Object2LongOpenHashMap<IProducer<V>>)this.producers;
//            
//            if(!map.isEmpty())
//            {
//                for(IProducer<V> producer : map.keySet())
//                {
//                    producer.cancelWIP(this);
//                }
//                map.clear();
//            }
//            this.producers = null;
//        }
//    }
//    
//    /**
//     * Deregister this request's broker and void any reference to it.
//     * The second part is important because if we re-register there could
//     * be a new broker different than our current reference.
//     */
//    protected abstract void abandonBroker();
//    
//    /**
//     * Called when a task with allocated demands is terminated,
//     * after all allocations are released in storage system.
//     * Simply needs to clear {@link #allocatedDemands()}.
//     */
//    protected abstract void clearAllocatedDemand();
//}
