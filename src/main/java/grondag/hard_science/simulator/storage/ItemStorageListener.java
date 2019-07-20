package grondag.hard_science.simulator.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.function.IFunction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;

import grondag.exotic_matter.network.PacketHandler;
import grondag.exotic_matter.simulator.domain.Domain;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.hard_science.network.server_to_client.PacketOpenContainerItemStorageRefresh;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ItemStorageEvent.AfterItemStorageConnect;
import grondag.hard_science.simulator.storage.ItemStorageEvent.BeforeItemStorageDisconnect;
import grondag.hard_science.simulator.storage.ItemStorageEvent.ItemCapacityChange;
import grondag.hard_science.simulator.storage.ItemStorageEvent.ItemStoredUpdate;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

// TODO: add and handle events for connectivity changes in connected mode
// TODO: add and handle events for machine on/off changes in connected mode

public class ItemStorageListener implements IStorageAccess<StorageTypeStack>
{
    private static enum Mode
    {
        /**
         * View all items in the domain.
         */
        DOMAIN,
        
        /**
         * View all items in or connected to the given store
         */
        CONNECTED,
        
        /**
         * View all items in a single store.
         */
        STORE;
    }
    
    private final Mode mode;
    
    /**
     * Null if in domain mode.
     */
    private final ItemContainer storage;
    
    /**
     * Domain if we are in domain mode, and last
     * known domain of storage in other modes.
     */
    private IDomain domain;
    
    protected final Container container;
    protected final EntityPlayerMP player;
    
    protected long totalCapacity;
    
    /**
     * Used in connected mode to track which stores are currently
     * connected to the store in {@link #storage}. Includes 
     * the primary store.  Null in domain mode.
     */
    protected final Set<IResourceContainer<StorageTypeStack>> stores;
    
    /**
     * Set true once disconnected and deregistered.
     */
    private boolean isDead = false;
    
    /**
     * Sequence counter for generating bulkResource handles.
     */
    private int nextHandle = 1;
    
    private final static IFunction<ItemResourceDelegate, Integer> RESOURCE_HANDLE_MAPPER
    = new IFunction<ItemResourceDelegate, Integer>() {
       @Override
       public Integer apply(@Nullable ItemResourceDelegate elem)
       {
           return elem.handle();
       }};
       
    /**
     * All resources currently tracked along with a client-side handle
     */
    protected Key2List<ItemResourceDelegate, ItemResource, Integer> slots 
        = new Key2List.Builder<ItemResourceDelegate, ItemResource, Integer>().
              withPrimaryKey1Map(ItemResourceDelegate::resource).
              withUniqueKey2Map(RESOURCE_HANDLE_MAPPER).
              build();
    
    /**
     * Constructor for domain-level listener.<p>
     * 
     * Creates a new storage listener, but does not subscribe.
     * Subscription should be done via the LogisticsService to 
     * ensure that the initial snapshot is consistent with all 
     * later updates.<p>
     */
    public ItemStorageListener(Domain domain, EntityPlayerMP player)
    {
        this.player = player;
        this.container = player.openContainer;
        this.mode = Mode.DOMAIN;
        this.domain = domain;
        this.storage = null;
        this.stores = null;
    }
    
   /**
    * Creates a new storage listener, but does not subscribe.
    * Subscription should be done via the LogisticsService to 
    * ensure that the initial snapshot is consistent with all 
    * later updates.<p>
    * 
    * @param storage  Storage to listen to. 
    * @param connectedMode If true, will listen to given storage plus 
    * all physically connected storage machines.
    */
            
    public ItemStorageListener(ItemContainer storage, EntityPlayerMP player)
    {
        this.player = player;
        this.container = player.openContainer;
        this.mode = storage.isOn() ? Mode.CONNECTED : Mode.STORE;
        this.stores = storage.isOn() 
                ? new HashSet<IResourceContainer<StorageTypeStack>>()
                : ImmutableSet.of(storage);
        this.storage = storage;
        this.domain = storage.getDomain();
    }
    
    /**
     * To be called by LogisticsService from service thread.  Will gather starting
     * inventory snapshot, register self for events, and send initial refresh to client.
     */
    public void initialize()
    {
        ItemStorageManager ism = this.domain.getCapability(ItemStorageManager.class);
        
        HashMap<IResource<StorageTypeStack>, ItemResourceDelegate> map = new HashMap<IResource<StorageTypeStack>, ItemResourceDelegate>();
        switch(this.mode)
        {
        case STORE:
            this.addStore(this.storage, map);
            break;
            
        case CONNECTED:
            for(IResourceContainer<StorageTypeStack> store : ism.stores())
            {
                ItemContainer itemStore = (ItemContainer)store;
                if(itemStore.isOn() && LogisticsService.ITEM_SERVICE.areDevicesConnected(this.storage.device(), itemStore.device(), null))
                {
                    this.stores.add(store);
                    this.addStore(itemStore, map);
                }
            }
            break;
            
        case DOMAIN:
            this.totalCapacity = ism.getCapacity();
            for(AbstractResourceWithQuantity<StorageTypeStack> rwq : ism.findQuantityStored(StorageType.ITEM.MATCH_ANY))
            {
                map.put(rwq.resource(), this.changeQuantity(rwq.resource(), rwq.getQuantity()));
            }
            break;
            
        default:
            assert false : "Missing enum mapping";
            return;
        }
        this.domain.eventBus().register(this);
        PacketHandler.CHANNEL.sendTo(
                new PacketOpenContainerItemStorageRefresh(
                        ImmutableList.copyOf(map.values()), this.totalCapacity, true), player);
    }
    
    /**
     * Returns true if player has disconnected or closed the container.
     */
    public boolean isDead()
    {
        assert !this.isDead : "Dead item storage listener received events";
    
        if(this.player.hasDisconnected() || this.player.openContainer != this.container)
        {
            this.die();
        }
        
        return this.isDead;
    }
    
    /**
     * Adds store and adds or updates item delegates in the map.
     * Have to use a map because could have same bulkResource and
     * thus same delegate for more than one store. We only send
     * a single delegate per bulkResource.  
     */
    private void addStore(ItemContainer storage, HashMap<IResource<StorageTypeStack>, ItemResourceDelegate> map)
    {
        this.totalCapacity += storage.getCapacity();
        for(AbstractResourceWithQuantity<StorageTypeStack> rwq : storage.find(StorageType.ITEM.MATCH_ANY))
        {
            map.put(rwq.resource(), this.changeQuantity(rwq.resource(), rwq.getQuantity()));
        }
    }
    
    private void removeStore(ItemContainer storage, ImmutableList.Builder<ItemResourceDelegate> builder)
    {
        this.totalCapacity -= storage.getCapacity();
        for(AbstractResourceWithQuantity<StorageTypeStack> rwq : storage.find(StorageType.ITEM.MATCH_ANY))
        {
            builder.add(this.changeQuantity(rwq.resource(), -rwq.getQuantity()));
        }
    }
    
    /**
     * Returns the delegate that was added or changed so that it
     * may be sent to client if desired.  Does not update client.<p>
     * 
     * Does not remove resources that are set to zero quantity because
     * handle values may still be retained on client.
     */
    private ItemResourceDelegate changeQuantity(ItemResource resource, long delta)
    {
        ItemResourceDelegate d = this.slots.getByKey1(resource);
        if(d == null)
        {
            d = new ItemResourceDelegate(this.nextHandle++, resource, delta);
            this.slots.add(d);
        }
        else
        {
            d.setQuantity(d.getQuantity() + delta);
            
            assert d.getQuantity() >= 0 : "Negative bulkResource quantity";
        }
        return d;
    }
   
    private ItemResourceDelegate changeQuantity(IResource<StorageTypeStack> resource, long delta)
    {
        return this.changeQuantity((ItemResource)resource, delta);
    }
    
    @Subscribe
    public void onItemUpdate(ItemStoredUpdate event)
    {
        if(this.isDead()) return;
        
        switch(this.mode)
        {
        case CONNECTED:
        case STORE:
            if(this.stores.contains(event.storage))
            {
                PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(
                        ImmutableList.of(this.changeQuantity(event.resource, event.delta)),
                        this.totalCapacity, 
                        false), player);
            }
            break;
            
        case DOMAIN:
            PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(
                    ImmutableList.of(this.changeQuantity(event.resource, event.delta)),
                    this.totalCapacity, 
                    false), player);
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }
    
    @Subscribe
    public void afterItemStorageConnect(AfterItemStorageConnect event)
    {
        if(this.isDead()) return;
        
        assert LogisticsService.ITEM_SERVICE.confirmServiceThread() 
            : "Storage listener events outside service thread.";
        
        switch(this.mode)
        {
        case CONNECTED:
        {
            assert event.storage != this.storage : "Attempt to reconnect primary storage to listener.";
            
            if(event.storage.isOn() && !this.stores.contains(event.storage) 
                    && LogisticsService.ITEM_SERVICE.areDevicesConnected(this.storage.device(), event.storage.device(), null))
            {
                    HashMap<IResource<StorageTypeStack>, ItemResourceDelegate> map = new HashMap<IResource<StorageTypeStack>, ItemResourceDelegate>();
                    this.addStore((ItemContainer) event.storage, map);
                    this.stores.add((ItemContainer) event.storage);
                    PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(
                            ImmutableList.copyOf(map.values()), this.totalCapacity, false), player);
            }
            break;
        }
        
        case DOMAIN:
        {
            HashMap<IResource<StorageTypeStack>, ItemResourceDelegate> map = new HashMap<IResource<StorageTypeStack>, ItemResourceDelegate>();
            this.addStore((ItemContainer) event.storage, map);
            PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(
                    ImmutableList.copyOf(map.values()), this.totalCapacity, false), player);
            break;
        }
        case STORE:
            assert false : "Attempt to add single item storage to listener outside initialization";
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }
    
    @Subscribe
    public void beforeItemStorageDisconnect(BeforeItemStorageDisconnect event)
    {
        if(this.isDead()) return;
        
        assert LogisticsService.ITEM_SERVICE.confirmServiceThread() 
            : "Storage listener events outside service thread.";
        
        switch(this.mode)
        {
        case CONNECTED:
        {
            if(event.storage == this.storage)
            {
                this.slots.clear();
                this.totalCapacity = 0;
                PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, true), player);
                this.die();
            }
            else if(this.stores.contains(event.storage) 
                    && !LogisticsService.ITEM_SERVICE.areDevicesConnected(this.storage.device(), event.storage.device(), null))
            {
                    ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
                    this.removeStore((ItemContainer) event.storage, builder);
                    this.stores.remove((ItemContainer) event.storage);
                    PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(
                            builder.build(), this.totalCapacity, false), player);
                
            }
            break;
        }
        
        case DOMAIN:
        {
            ImmutableList.Builder<ItemResourceDelegate> builder = ImmutableList.builder();
            this.removeStore((ItemContainer) event.storage, builder);
            PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(
                    builder.build(), this.totalCapacity, false), player);
            break;
        }
        
        case STORE:
            if(event.storage == this.storage)
            {
                this.slots.clear();
                this.totalCapacity = 0;
                PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, true), player);
                this.die();
            }
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }
    
    @Subscribe
    public void onCapacityChange(ItemCapacityChange event)
    {
        if(this.isDead()) return;
        
        switch(this.mode)
        {
        case CONNECTED:
            if(this.stores.contains(event.storage))
            {
                this.totalCapacity += event.delta;
                PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, false), player);
            }
            break;
            
        case DOMAIN:
            this.totalCapacity += event.delta;
            PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, false), player);
            break;
            
        case STORE:
            if(event.storage == this.storage)
            {
                this.totalCapacity = event.storage.getCapacity();
                PacketHandler.CHANNEL.sendTo(new PacketOpenContainerItemStorageRefresh(ImmutableList.of(), this.totalCapacity, false), player);
            }
            break;
            
        default:
            assert false : "Missing enum mapping";
            break;
        }
    }

    /**
     * Will deregister for events if haven't already and release bulkResource references.
     */
    public void die()
    {
        if(!this.isDead)
        {
            this.domain.eventBus().unregister(this);
            this.slots.clear();
            this.totalCapacity = 0;
            this.isDead = true;
        }
    }

    public ItemResource getResourceForHandle(int resourceHandle)
    {
        ItemResourceDelegate d = this.slots.getByKey2(resourceHandle);
        return d == null ? null : d.resource();
    }

    @Override
    public ImmutableList<IResourceContainer<StorageTypeStack>> stores()
    {
        return ImmutableList.copyOf(this.stores);
    }

    /**
     * For non-domain listeners, limits to connectivity from 
     * the primary storage device.  Always empty for domain listener.
     */
    public ImmutableList<IResourceContainer<StorageTypeStack>> findSpaceFor(IResource<StorageTypeStack> resource)
    {
        return this.storage == null
                ? ImmutableList.of()
                : IStorageAccess.super.findSpaceFor(resource, this.storage.device());
    }

    /**
     * For non-domain listeners, limits to connectivity from 
     * the primary storage device.  Always fails (returns 0) for domain listener.
     */
    public long add(IResource<StorageTypeStack> resource, final long howMany, boolean simulate, @Nullable NewProcurementTask<StorageTypeStack> request)
    {
        return this.storage == null
                ? 0
                : IStorageAccess.super.add(resource, howMany, simulate, request, this.storage.device());
    }
    
    @Override
    public ImmutableList<IResourceContainer<StorageTypeStack>> getLocations(IResource<StorageTypeStack> resource)
    {
        return this.mode == Mode.DOMAIN
                ? this.domain.getCapability(ItemStorageManager.class).getLocations(resource)
                : IStorageAccess.super.getLocations(resource);
    }

    public long getQuantityStored(ItemResource targetResource)
    {
        ItemResourceDelegate d = this.slots.getByKey1(targetResource);
        return d == null ? 0 : d.getQuantity();
    }
}
