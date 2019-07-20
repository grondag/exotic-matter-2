//package grondag.hard_science.simulator.jobs.tasks;
//
//import java.util.Collections;
//import java.util.List;
//
//import com.google.common.collect.ImmutableList;
//
//import grondag.hard_science.library.serialization.ModNBTTag;
//import grondag.hard_science.library.varia.Useful;
//import grondag.hard_science.simulator.demand.IBroker;
//import grondag.hard_science.simulator.demand.IProducer;
//import grondag.hard_science.simulator.domain.DomainManager;
//import grondag.hard_science.simulator.jobs.Job;
//import grondag.hard_science.simulator.jobs.RequestStatus;
//import grondag.hard_science.simulator.jobs.TaskType;
//import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
//import grondag.hard_science.simulator.resource.IResource;
//import grondag.hard_science.simulator.resource.StorageType;
//import grondag.hard_science.simulator.storage.StorageManager;
//import net.minecraft.nbt.NBTTagCompound;
//
//public class SimpleProcurementTask<V extends StorageType<V>> extends ProcurementTask<V>
//{
//    private IResource<V> resource;
//    private long quantity;
//    private long allocatedQuantity = 0;
//    private long deliveredQuantity = 0;
//    
//    /**
//     * Lazily obtained - DO NOT USE DIRECTLY.  Use {@link #broker()} instead.
//     */
//    protected IBroker<V> broker;
//    
//    /**
//     * Not serialized - producers are expected to re-declare WIP on reload.
//     */
//    private long wipQuantity = 0;
//    
//    public SimpleProcurementTask(IResource<V> resource, long quantity)
//    {
//        super(true);
//        this.resource = resource;
//        this.quantity = quantity;
//    }
//    
//    public SimpleProcurementTask()
//    {
//        super(false);
//    }
//
////    @Override
////    public List<IResourcePredicateWithQuantity<V>> allDemands()
////    {
////        return Collections.singletonList(this.resource.withQuantity(this.quantity));
////    }
//
////    @Override
////    public List<IResourcePredicateWithQuantity<V>> openDemands()
////    {
////        return Collections.singletonList(this.resource.withQuantity(this.quantity - this.allocatedQuantity - this.wipQuantity));
////    }
//
//    @Override
//    public List<AbstractResourceWithQuantity<V>> allocatedDemands()
//    {
//        return this.allocatedQuantity == 0 
//                ? ImmutableList.of()
//                : Collections.singletonList(this.resource.withQuantity(this.allocatedQuantity));
//    }
//
//    @Override
//    public synchronized long startWIP(IResource<V> resource, long startedQuantity, IProducer<V> producer)
//    {
//        assert this.status == RequestStatus.ACTIVE
//                : "AbstractSimpleProcurmentTask.startWIP called when status != ACTIVE";
//        
//        startedQuantity = Useful.clamp(this.quantity - this.allocatedQuantity - this.wipQuantity, 0, startedQuantity);
//        
//        if(startedQuantity > 0)
//        {
//            this.changeProducerWIP(producer, startedQuantity);
//            
//            // if all demands are met or WIP, broker can stop tracking us
//            if(this.quantity - this.allocatedQuantity - this.wipQuantity == 0)
//            {
//                this.abandonBroker();
//            }
//        }
//        return startedQuantity;
//    }
//
//    @Override
//    public synchronized void cancelWIP(IResource<V> resource, long cancelledQuantity, IProducer<V> producer)
//    {
//        if(cancelledQuantity == 0) return;
//       
//        assert cancelledQuantity >= 0
//                : "ProcurmentTask.cancelWIP receive negative values.";
//        
//        assert this.status == RequestStatus.ACTIVE
//                : "AbstractSimpleProcurmentTask.cancelWIP called when status != ACTIVE";
//        
//        boolean hadOpenDemand = this.quantity - this.allocatedQuantity - this.wipQuantity > 0;
//        
//        this.changeProducerWIP(producer, cancelledQuantity);
//        
//        if(hadOpenDemand)
//        {
//            // should already be registered - let broker know we have new
//            // demand because a producer gave up
//            this.broker().notifyNewDemand(this);
//        }
//        else
//        {
//            // would not be registered because all demands were allocated
//            // or WIP, so need to re-register with broker
//            this.broker().registerRequest(this);
//        }
//    }
//
//    @Override
//    public synchronized void completeWIP(IResource<V> resource, long completedQuantity, IProducer<V> producer)
//    {
//        if(completedQuantity == 0) return;
//
//        assert completedQuantity >= 0
//                : "ProcurmentTask.cancelWIP receive negative values.";
//        
//        assert this.status == RequestStatus.ACTIVE
//                : "AbstractSimpleProcurmentTask.completeWIP called when status != ACTIVE";
//        
//        this.changeProducerWIP(producer, -completedQuantity);
//        this.allocatedQuantity += completedQuantity;
//        
//        if(this.allocatedQuantity == this.quantity)
//        {
//            assert this.wipQuantity == 0 
//                    : "SimpleProcurementTask.completeWIP found non-zero WIP with full allocation.";
//            
//            // will un-register with broker due to status change
//            this.complete();
//        }
//    }
//
//    
//    /**
//     * Contrary to base class, in this implementation, DOES update overall WIP count.<p>
//     * 
//     * {@inheritDoc}
//     */
//    @Override
//    protected synchronized void changeProducerWIP(IProducer<V> producer, long delta)
//    {
//        super.changeProducerWIP(producer, delta);
//        this.wipQuantity += delta;
//        
//        assert this.wipQuantity >= 0 
//                : "SimpleProcurementTask.changeProducerWIP found negative total WIP.";
//    }
//
//    /**
//     * Contrary to base class, in this implementation, DOES update overall WIP count.<p>
//     * 
//     * {@inheritDoc}
//     */
//    @Override
//    protected synchronized void cancelProducerWIP()
//    {
//        super.cancelProducerWIP();
//        this.wipQuantity = 0;
//    }
//
//    @Override
//    public synchronized boolean initialize(Job job)
//    {
//        // if we are ready at start, will move
//        // immediately to active status and register
//        // with appropriate broker.  So will never
//        // return true from this method because will
//        // either be WAITING or ACTIVE.
//        if(super.initialize(job)) 
//        {
//            // as with super method, setting status directly
//            // to avoid callback to job
//            this.status = RequestStatus.ACTIVE;
//            
//            // Don't register with broker if deserializing because it
//            // may try to re-allocate inventory to us that we already
//            // have allocated.  That case is handled in afterDeserialization();
//            if(!DomainManager.instance().isDeserializationInProgress())
//            {
//                // have to call directly here because not using setStatus()
//                this.broker().registerRequest(this);
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public synchronized void breakAllocation(IResource<V> resource, long newAllocation)
//    {
//        assert this.status == RequestStatus.ACTIVE || this.status == RequestStatus.COMPLETE
//                : "SimpleProcurementTask.breakAllocation called while task not ACTIVE or COMPLETE.";
//
//        
//        long reduction = this.allocatedQuantity - newAllocation;
//        if(reduction > 0)
//        {
//            if(this.allocatedQuantity == this.quantity)
//            {
//                assert this.status == RequestStatus.COMPLETE
//                        : "SimpleProcurementTask.breakAllocation encountered full allocation while status not COMPLETE.";
//                
//                // need to change our status, because no longer complete
//                if(this.antecedents().isEmpty())
//                {
//                    this.setStatus(RequestStatus.ACTIVE);
//                }
//                else
//                {
//                    this.setStatus(RequestStatus.WAITING);
//                }
//                this.backTrackConsequents();
//            }
//            else
//            {
//                // No status change because we haven't declared this task complete
//                // but should be in active status if existing allocation isn't full.
//                
//                assert this.status == RequestStatus.ACTIVE
//                        : "SimpleProcurementTask.breakAllocation encountered partial allocation while status not ACTIVE.";
//            
//                // Notify broker we have new demand
//                this.broker().notifyNewDemand(this);
//            }
//            this.allocatedQuantity -= reduction;
//        }
//    }
//    
//    @Override
//    public void afterDeserialization()
//    {
//        this.reallocateAfterDeserialization();
//        
//        if(this.status == RequestStatus.ACTIVE) this.broker().registerRequest(this);
//    }
//    
//    /**
//     * Reclaim any allocated amounts from storage before simulation starts
//     * after deserialization.  All domain objects will be available at this point.
//     */
//    protected void reallocateAfterDeserialization()
//    {
//        StorageManager<V> storageManager = this.getDomain().getStorageManager(this.storageType());
//        this.allocatedQuantity = storageManager.setAllocation(this.resource, this, this.allocatedQuantity);
//    }
//
//    @Override
//    public void deserializeNBT(NBTTagCompound tag)
//    {
//        super.deserializeNBT(tag);
//        this.allocatedQuantity = tag.getLong(ModNBTTag.TASK_ALLOCATED_QTY);
//        this.quantity = tag.getLong(ModNBTTag.TASK_REQUESTED_QTY);
//        this.deliveredQuantity = tag.getLong(ModNBTTag.TASK_DELIVERED_QTY);
//        this.resource = StorageType.fromNBTWithType(tag.getCompoundTag(ModNBTTag.RESOURCE));
//    }
//
//    @Override
//    public synchronized void serializeNBT(NBTTagCompound tag)
//    {
//        super.serializeNBT(tag);
//        tag.setLong(ModNBTTag.TASK_ALLOCATED_QTY, this.allocatedQuantity);
//        tag.setLong(ModNBTTag.TASK_REQUESTED_QTY, this.quantity);
//        tag.setLong(ModNBTTag.TASK_DELIVERED_QTY, this.deliveredQuantity);
//        tag.setTag(ModNBTTag.RESOURCE, StorageType.toNBTWithType(resource));
//    }
//
//    @Override
//    public V storageType()
//    {
//        return this.resource.storageType();
//    }
//
//    @Override
//    public TaskType requestType()
//    {
//        return TaskType.SIMPLE_PROCUREMENT;
//    }
//
//    @Override
//    public long demandFor(IResource<V> resource)
//    {
//        return this.resource.isResourceEqual(resource)
//                ? this.quantity - this.allocatedQuantity - this.deliveredQuantity : 0;
//    }
//
//    @Override
//    protected synchronized IBroker<V> broker()
//    {
//        if(this.broker == null)
//        {
//            this.broker = this.getDomain().brokerManager
//                    .brokerForResource(this.resource);
//        }
//        return this.broker;
//    }
//    
//    @Override
//    protected synchronized void abandonBroker()
//    {
//        if(this.broker != null)
//        {
//            this.broker.unregisterRequest(this);
//            this.broker = null;
//        }
//    }
//
//    @Override
//    protected void clearAllocatedDemand()
//    {
//        this.allocatedQuantity = 0;
//    }
//
//    @Override
//    public void reportDelivery(IResource<V> resource, long deliveredQuantity)
//    {
//        assert deliveredQuantity <= this.allocatedQuantity
//                : "Reported delivery greater than allocation.";
//        
//        this.allocatedQuantity -= deliveredQuantity;
//        this.deliveredQuantity += deliveredQuantity;
//    }
//
//    @Override
//    public long deliveredQuantity(IResource<V> resource)
//    {
//        return this.deliveredQuantity;
//    }
//    
//}
