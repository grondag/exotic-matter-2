//package grondag.hard_science.simulator.jobs.tasks;
//
//import javax.annotation.Nonnull;
//
//import grondag.hard_science.simulator.domain.DomainManager;
//import grondag.hard_science.simulator.jobs.AbstractTask;
//import grondag.hard_science.simulator.jobs.TaskType;
//import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
//import grondag.hard_science.simulator.resource.StorageType;
//import grondag.hard_science.simulator.storage.IResourceContainer;
//import grondag.hard_science.simulator.storage.StorageManager;
//
///**
// * Transports the result of a procurement request to the player
// * TODO: current implementation is a stub.
// *
// */
//public class DeliveryTask<V extends StorageType<V>> extends AbstractTask
//{
//   private int procurementTaskID;
//    
//    /** 
//     * Don't use directly - lazily deserialized.
//     */
//    private ProcurementTask<V> procurementTask;
//    
//    /**
//     * Use for new instances. Automatically
//     * make procurement task dependent on this task.
//     */
//    public DeliveryTask(@Nonnull ProcurementTask<V> procurementTask)
//    {
//        super(true);
//        this.procurementTaskID = procurementTask.getId();
//        this.procurementTask = procurementTask;
//        AbstractTask.link(procurementTask, this);
//    }
//    
//    /** Use for deserialization */
//    public DeliveryTask()
//    {
//        super(false);
//    }
//
//    @Override
//    public TaskType requestType()
//    {
//        return TaskType.DELIVERY;
//    }
//
//    @SuppressWarnings("unchecked")
//    public ProcurementTask<V> procurementTask()
//    {
//        if(this.procurementTask == null)
//        {
//            this.procurementTask = (ProcurementTask<V>) DomainManager.taskFromId(procurementTaskID);
//        }
//        return this.procurementTask;
//    }
//
//    @Override
//    public void complete()
//    {
//        // TODO this is a stub / mock up
//        /**
//         * Delivery can be direct or packaged.
//         * Direct delivery requires three things:
//         * 1) allocated bulkResource is in a location accessible via fast transport
//         * 2) the transport network is not backlogged - transport is effectively instant
//         * 3) the receiver can take immediate ownership at the destination
//         * 
//         * Direct won't be possible for anything that has to be picked up
//         * from storage.  Direct won't be possible if drone transport is required.
//         * 
//         * Packaged delivery means a new bulkResource is created that is uniquely tagged
//         * with this delivery request so that it is not accidentally consumed by 
//         * another request and easily identified for pick up.  
//         * 
//         * Direct receivers must also be prepared to accept packaged delivery.
//         */
//
//        // claim self and make active prevent griping by complete
//        this.claim();
//        
//        super.complete();
//        StorageManager<V> sm = this.getDomain().getStorageManager(this.procurementTask.storageType());
//        
//        for(AbstractResourceWithQuantity<V> rwq : this.procurementTask.allocatedDemands())
//        {
//            long allocation = rwq.getQuantity();
//            
//            for(IResourceContainer<V> store : sm.getLocations(rwq.resource()))
//            {
//                allocation -= store.takeUpTo(rwq.resource(), allocation, false, this.procurementTask);
//                if(allocation == 0) break;
//            }
//        }
//    }
//    
//    
//}
