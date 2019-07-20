package grondag.hard_science.simulator.storage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.job.RequestStatus;
import grondag.exotic_matter.varia.Useful;
import grondag.exotic_matter.varia.structures.SimpleUnorderedArrayList;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.fobs.INewTaskListener;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.fobs.NewTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class StorageResourceManager<T extends StorageType<T>> implements INewTaskListener
{
    /**
     * Resource this instance manages.
     */
    public final IResource<T> resource;
    
    /**
     * Total quantityStored of this resource within the domain.
     */
    private long quantityStored;
    
    private long quantityAllocated;
    
    /**
     * List of all storage instances in the domain that contain this resource.
     */
    private final SimpleUnorderedArrayList<IResourceContainer<T>> stores = new SimpleUnorderedArrayList<IResourceContainer<T>>();

    /**
     * Single listener or list of resource listeners.
     */
    private Object listeners = null;

    /**
     * True if listener notification is needed. Set false
     * at start of notification process.  Prevents
     * redundant spamming of listeners when lots of changes
     * happening at once.
     */
    private volatile boolean isListenerNotificationDirty = false;
    
    /**
     * Pair or Map of all allocations for this resource.
     */
    private Object allocations = null;
    
    /**
     * If request is non-null, initial quantityIn will be allocated to that request.
     * Quantity is NOT used to track overall storage amount - that is retrieved from
     * the storage, if non-null.
     * Useful when fabrication is storing items made for procurement requests.
     */
    public StorageResourceManager(IResource<T> resource, IResourceContainer<T> firstStorage, long allocatedQuantity, @Nullable NewProcurementTask<T> request)
    {
        this.resource = resource;
        if(firstStorage != null)
        {
            this.quantityStored = firstStorage.getQuantityStored(resource);
            this.stores.add(firstStorage);
        }
        
        if(request != null)
        {
            this.setAllocation(request, allocatedQuantity);
        }
    }
    
    /**
     * Adds delta to the allocation of this resource for the given request.
     * Return value is the quantityIn removed or added, which could be different than
     * amount requested if not enough is available or would reduce allocation below 0.
     * Total quantityIn allocated can be different from return value if request already had an allocation.
     * Provides no notification to the request.
     */
    public synchronized long changeAllocation(NewProcurementTask<T> request, long quantityRequested)
    {
        long allocated = this.getAllocation(request);
        long newAllocation = Useful.clamp(allocated + quantityRequested, 0, allocated + this.quantityAvailable());
        return this.setAllocation(request, newAllocation) - allocated;
    }
    
    /**
     * Returns current allocation for the given request.
     */
    public synchronized long getAllocation(NewProcurementTask<T> request)
    {
        if(this.allocations == null) return 0;
        
        if(this.allocations instanceof Pair)
        {
            @SuppressWarnings("unchecked")
            Pair<NewProcurementTask<T>, Long> pair = (Pair<NewProcurementTask<T>, Long>)this.allocations;
            return pair.getLeft() == request ? pair.getRight() : 0;
        }
        else
        {
            @SuppressWarnings("unchecked")
            Object2LongOpenHashMap<NewProcurementTask<T>> map = (Object2LongOpenHashMap<NewProcurementTask<T>>)this.allocations;
            return map.getLong(request);
        }
    }
    
    /**
     * Sets allocation for the given request to the provided, non-negative value.
     * Returns allocation that was actually set.  Will not set negative allocations
     * and will not set allocation so that total allocated is more the total available.
     * Provides no notification to the request.
     */
    public synchronized long setAllocation(NewProcurementTask<T> request, long requestedAllocation)
    {
        /**
         * 1 = start listening, -1 = stop
         */
        byte listenBehavior = 0;
        
        if(requestedAllocation < 0)
        {
            HardScience.INSTANCE.warn("StorageResourceManager received request to set resource allocation less than zero. This is a bug.");
            requestedAllocation = 0;
        }

        // exit if no change to allocation for this request
        final long currentAllocation = this.getAllocation(request);
        long delta = requestedAllocation - currentAllocation;
        if(delta == 0) return currentAllocation;
        
        long startingAvailable = this.quantityAvailable();
        
        // don't allocate more than we are storing
        if(delta > startingAvailable)
        {
            delta = startingAvailable;
            requestedAllocation = currentAllocation + delta;
        }
        
        if(delta == 0) return currentAllocation;
        
        // update tracking total
        this.quantityAllocated += delta;
        
        if(this.allocations == null)
        {
            // start allocation is stored as a Pair
            if(requestedAllocation > 0)
            {
                this.allocations = Pair.of(request, requestedAllocation);
                listenBehavior = 1;
            }
        }
        else if(this.allocations instanceof Pair)
        {
            @SuppressWarnings("unchecked")
            Pair<NewProcurementTask<T>, Long> pair = (Pair<NewProcurementTask<T>, Long>)this.allocations;
            if(pair.getLeft() == request)
            {
                // already tracking this request as the only allocation
                if(requestedAllocation == 0)
                {
                    this.allocations = null;
                    listenBehavior = -1;
                }
                else
                {
                    pair.setValue(requestedAllocation);
                }
            }
            else
            {
                assert requestedAllocation > 0
                    : "Logic error resulted in tracking a zero allocation";
                
                // need to start tracking more than one allocation
                // upgrade tracking data structure to HashMap
                Object2LongOpenHashMap<NewProcurementTask<T>> map = new Object2LongOpenHashMap<NewProcurementTask<T>>();
                map.put(pair.getLeft(), pair.getRight());
                map.put(request, requestedAllocation);
                listenBehavior = 1;
                this.allocations = map;
            }
        }
        else
        {
            //already using a hash map
            @SuppressWarnings("unchecked")
            Object2LongOpenHashMap<NewProcurementTask<T>> map = (Object2LongOpenHashMap<NewProcurementTask<T>>)this.allocations;
            if(requestedAllocation == 0)
            {
                map.remove(request);
                listenBehavior = -1;
            }
            else
            {
                map.put(request, requestedAllocation);
                listenBehavior = 1;
            }
        }
        
        // asynch - returns immediately
        if(this.quantityAvailable() != startingAvailable) this.notifyListenersOfAvailability();
        
        // listen for event termination to remove allocations
        // if the event is cancelled unexpectedly
        if(listenBehavior == 1 )
            request.addListener(this);
        else if(listenBehavior == -1)
            request.removeListener(this);
        
        return requestedAllocation;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void notifyStatusChange(NewTask task, RequestStatus oldStatus)
    {
        if(task instanceof NewProcurementTask && task.status().isTerminated && !oldStatus.isTerminated)
        {
            // this will also remove us as listener
            this.setAllocation((NewProcurementTask)task, 0);
        }
    }

    /**
     * Does not include allocated amounts.
     */
    public synchronized long quantityAvailable()
    {
        return Math.max(0, this.quantityStored - this.quantityAllocated);
    }
    
    /**
     * Includes allocated amounts.
     */
    public synchronized long quantityStored()
    {
        return this.quantityStored;
    }
    
    /**
     * Amount of stored resource that is allocated to procurement tasks.
     */
    public synchronized long quantityAllocated()
    {
        return this.quantityAllocated;
    }
    
    /**
     * Called by storage when resources are removed.
     * If request is non-null, then the amount taken reduces any allocation to that request.
     */
    public synchronized void notifyTaken(IResourceContainer<T> storage, long taken, @Nullable NewProcurementTask<T> request)
    {
        if(taken == 0) return;
        
        assert storage.containerUsage().isListed
            : "Notification for unlisted storage";
        
        if(taken > this.quantityStored)
        {
            taken = this.quantityStored;
            HardScience.INSTANCE.warn("Resource manager encounted request to take more than current inventory level.  This is a bug.");
        }
        
        // remove storage from list if no longer holding resource
        if(storage.getQuantityStored(resource) == 0)
        {
            stores.removeIfPresent(storage);
        }
        
        /**
         * To check for availability notification at end
         */
        final long startingAvailable = this.quantityAvailable();
        
        // update resource qty
        this.quantityStored -= taken;
        
        // update allocation if request provided
        if(request != null) this.changeAllocation(request, -taken);
        
        // check for and notify of broken allocations
        if(this.quantityStored < this.quantityAllocated)
        {
            if(this.allocations == null)
            {
                HardScience.INSTANCE.warn("Storage Resource Manager tracking non-zero allocation but has no allocation requests. This is a bug.");
            }
            else if(this.allocations instanceof Pair)
            {
                // single allocation, set allocation to total stored and notify
                @SuppressWarnings("unchecked")
                Pair<NewProcurementTask<T>, Long> pair = (Pair<NewProcurementTask<T>, Long>)this.allocations;
                long newAllocation = this.setAllocation(pair.getLeft(), this.quantityStored);
                pair.getLeft().breakAllocation(this.resource, newAllocation);
            }
            else
            {
                // Multiple allocations, have to decide which one is impacted.
                @SuppressWarnings("unchecked")
                Object2LongOpenHashMap<NewProcurementTask<T>> map = (Object2LongOpenHashMap<NewProcurementTask<T>>)this.allocations;
                
                // Sort by priority and seniority
                List<Entry<NewProcurementTask<T>>> list =
                map.object2LongEntrySet().stream()
                .sorted(new Comparator<Entry<NewProcurementTask<T>>>() 
                {
                    @Override
                    public int compare(@Nullable Entry<NewProcurementTask<T>> o1, @Nullable Entry<NewProcurementTask<T>> o2)
                    {
                        // note reverse order
                        NewProcurementTask<T> k1 = o2.getKey();
                        NewProcurementTask<T> k2 = o1.getKey();
                        return k1.priority().compareTo(k2.priority());
                    }
                })
                .collect(Collectors.toList());
                
                // Break allocations until allocation total is within the stored amount.
                for(Entry<NewProcurementTask<T>> entry : list)
                {
                    long gap = this.quantityAllocated - this.quantityStored;
                    if(gap <= 0) break;
                    
                    long delta = Math.min(gap, entry.getLongValue());
                    if(delta > 0)
                    {
                        NewProcurementTask<T> brokenRequest = entry.getKey();
                        if(this.changeAllocation(brokenRequest, -delta) != 0)
                        {
                            brokenRequest.breakAllocation(this.resource, this.getAllocation(brokenRequest));
                        }
                    }
                }
            }
        }
        
        // asynch - returns immediately
        if(startingAvailable != this.quantityAvailable()) this.notifyListenersOfAvailability();
    }

    /**
     * If request is non-null, then the amount added is immediately allocated to that request.
     */
    public synchronized void notifyAdded(IResourceContainer<T> storage, long added, @Nullable NewProcurementTask<T> request)
    {
        if(added == 0) return;
        
        assert storage.containerUsage().isListed
            : "Notification for unlisted storage";
        
        // track store for this resource
        if(this.stores.addIfNotPresent(storage))
        {
            // if store is newly added, amount of delta should match
            // total amount in the store, UNLESS this is an output buffer
            // because output buffers can get new content outside the service thread
            
            assert storage.getQuantityStored(resource) == added
                    : "Storage Resource Manager encountered request to add a quanity in a new storage"
                        + " that did not match the quantity in the storage.";
        }
        
        /**
         * To check for availability notification at end
         */
        final long startingAvailable = this.quantityAvailable();
        
        // update resource qty
        this.quantityStored += added;
        
        // update allocation if request provided
        if(request != null) this.changeAllocation(request, added);
        
        // asynch - returns immediately
        if(startingAvailable != this.quantityAvailable()) this.notifyListenersOfAvailability();
    }
    
    /**
     * Returns all locations where the resource is stored,
     * irrespective of allocation.
     */
    public synchronized ImmutableList<StorageWithQuantity<T>> getLocations(IResource<T> resource)
    {
        ImmutableList.Builder<StorageWithQuantity<T>> builder = ImmutableList.builder();
        for(IResourceContainer<T> store : this.stores)
        {
            long quantity = store.getQuantityStored(resource);
            if(quantity > 0)
            {
                builder.add(store.withQuantity(quantity));
            }
        }
        return builder.build();
    }
    
    /**
     * Adds all non-zero storage locations to the given list builder.
     */
    public synchronized void addStoragesWithQuantityToBuilder(ImmutableList.Builder<StorageWithResourceAndQuantity<T>> builder)
    {
        for(IResourceContainer<T> store : this.stores)
        {
            long quantity = store.getQuantityStored(this.resource);
            if(quantity > 0)
            {
                builder.add(store.withResourceAndQuantity(this.resource, quantity));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public synchronized void registerResourceListener(IStorageResourceListener<T> listener)
    {
        if(this.listeners == null)
        {
            this.listeners = listener;
        }
        else if(this.listeners instanceof IStorageResourceListener)
        {
            if(this.listeners != listener)
            {
            // upgrade to list
                SimpleUnorderedArrayList<IStorageResourceListener<T>> list
                     = new SimpleUnorderedArrayList<IStorageResourceListener<T>>();
                list.add((IStorageResourceListener<T>)this.listeners);
                list.add(listener);
            }
        }
        else
        {
            // should already be using list
            ((SimpleUnorderedArrayList<IStorageResourceListener<T>>)this.listeners)
                .addIfNotPresent(listener);
        }
    }
    
    public synchronized void unregisterResourceListener(IStorageResourceListener<T> listener)
    {
        if(this.listeners == null)
        {
            return;
        }
        else if(this.listeners instanceof IStorageResourceListener)
        {
            // should already be using list
            if(this.listeners == listener)
            {
                this.listeners = null;
            }
        }
        else
        {
            @SuppressWarnings("unchecked")
            SimpleUnorderedArrayList<IStorageResourceListener<T>> list
             = (SimpleUnorderedArrayList<IStorageResourceListener<T>>)this.listeners;
            list.removeIfPresent(listener);
            if(list.isEmpty()) this.listeners = null;
        }
    }
    
    /**
     * Called whenever availability changes.  Returns immediately
     * and schedules notification to happen asynchronously. This
     * prevents a chain of notification-driven changes involving
     * listener actions that would all have to be resolved before
     * the initiating listener gets a result.<p>
     * 
     * Sets the {@link #isListenerNotificationDirty} flag so that
     * it is not necessary to do this in caller.
     */
    @SuppressWarnings({ "unchecked" })
    protected void notifyListenersOfAvailability()
    {
        if(this.listeners == null)
        {
            return;
        }
        
        this.isListenerNotificationDirty = true;
        
        Simulator.CONTROL_THREAD.execute(new Runnable()
        {
            @Override
            public void run()
            {
                // could get submitted multiple times before we get executed, 
                // but only need to run once until there is another change
                if(!StorageResourceManager.this.isListenerNotificationDirty) return;
                
                StorageResourceManager.this.isListenerNotificationDirty = false;
                
                // just in case listeners reference changes while we are executing
                Object listeners = StorageResourceManager.this.listeners;
                
                if(listeners == null) return;
                
                if(listeners instanceof IStorageResourceListener)
                {
                    ((IStorageResourceListener<T>)listeners).onAvailabilityChange(resource, quantityAvailable());
                }
                else
                {
                    // Using an array copy of the list because
                    // a listener might unregister as a result of being called 
                    // and then the order could change and we'd skip a listener.
                    for(Object listener : ((SimpleUnorderedArrayList<IStorageResourceListener<T>>)listeners).toArray())
                    {
                        // Note that we query available quantityIn each time
                        // because some listeners might take action that would 
                        // change the availability.
                        ((IStorageResourceListener<T>)listener).onAvailabilityChange(resource, quantityAvailable());
                    }
                }
            }
    
        });
    }
    
    /**
     * If true, has no storages and no listeners and can thus be safely discarded by the storage manager.
     * Implies all quantities are zero.
     */
    @SuppressWarnings("unchecked")
    public boolean isEmpty()
    {
        if(this.listeners == null && this.stores.isEmpty())
        {
            assert(this.allocations == null 
                    || ((Object2LongOpenHashMap<NewProcurementTask<T>>)this.allocations).isEmpty())
                : "StorageResourceManager empty with non-null allocations";
            
            assert(this.quantityAllocated() == 0)
                : "StorageResourceManager empty with non-zero allocations";
            
            assert(this.quantityAvailable() == 0)
                : "StorageResourceManager empty with non-zero available";

            assert(this.quantityStored() == 0)
                : "StorageResourceManager empty with non-zero stored";

            return true;
        }
        return false;
    }
    
    public IResource<T> resource()
    {
        return this.resource;
    }
}