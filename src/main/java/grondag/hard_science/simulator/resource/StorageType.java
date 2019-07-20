package grondag.hard_science.simulator.resource;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.HardScience;
import grondag.hard_science.init.ModBulkResources;
import grondag.hard_science.machines.energy.MachinePower;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.storage.FluidStorageEvent;
import grondag.hard_science.simulator.storage.FluidStorageManager;
import grondag.hard_science.simulator.storage.IStorageEventFactory;
import grondag.hard_science.simulator.storage.ItemStorageEvent;
import grondag.hard_science.simulator.storage.ItemStorageManager;
import grondag.hard_science.simulator.storage.PowerStorageEvent;
import grondag.hard_science.simulator.storage.PowerStorageManager;
import grondag.hard_science.simulator.storage.StorageManager;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;


/**
 * Set up like an enum but using classes to enable generic-based type safety for bulkResource classes.
 * Probably a better way to do this but I don't think a regular enum will do.
 */
public abstract class StorageType<T extends StorageType<T>>
{
    public static final String NBT_RESOURCE_IDENTITY = NBTDictionary.claim("resID");
    public static final String NBT_RESOURCE_TYPE = NBTDictionary.claim("resType");

    public static StorageType<?> fromEnum(EnumStorageType e)
    {
        switch(e)
        {
        case FLUID:
            return StorageType.FLUID;
        case ITEM:
            return StorageType.ITEM;
        case POWER:
            return StorageType.POWER;
            
        case PRIVATE:
            assert false : "Unsupported private storage type reference";
            return null;
            
        default:
            assert false : "Missing enum mapping for storage type";
            return null;
        }
    }
    
    public  final EnumStorageType enumType;
    public final IResource<T> emptyResource;
    public final int ordinal;
    public final IResourcePredicate<T> MATCH_ANY;
    public final IResourcePredicate<T> MATCH_NONE;
    
    private StorageType(EnumStorageType enumType, IResource<T> emptyResource)
    {
        this.enumType = enumType;
        this.ordinal = enumType.ordinal();
        this.emptyResource = emptyResource;
        this.MATCH_ANY = new IResourcePredicate<T>()
        {
            @Override
            public boolean test(@Nullable IResource<T> t) { return true; }
        };
        
        this.MATCH_NONE = new IResourcePredicate<T>()
        {
            @Override
            public boolean test(@Nullable IResource<T> t) { return false; }
        };
    }
    
    public abstract Class<? extends StorageManager<T>> domainCapability();
    
    @Nullable
    public abstract IResource<T> fromBytes(PacketBuffer pBuff);
    
    @Nullable
    public AbstractResourceWithQuantity<T> fromBytesWithQty(PacketBuffer pBuff)
    {
        return fromBytes(pBuff).withQuantity(pBuff.readVarLong());
    }
    
    @Nullable
    public abstract IResource<T> fromNBT(NBTTagCompound nbt);
    
    public abstract void toBytes(IResource<T> resource, PacketBuffer pBuff);
    
    public void toBytes(AbstractResourceWithQuantity<T> rwq, PacketBuffer pBuff)
    {
        this.toBytes(rwq.resource(), pBuff);
        pBuff.writeVarLong(rwq.quantity);
    }
    
    public abstract String toCSV(IResource<T> resource);
    
    @Nullable
    public abstract IResource<T> fromCSV(String csv);
    
    @Nullable
    public abstract NBTTagCompound toNBT(IResource<T> resource);
    
    @Nullable
    public abstract AbstractResourceWithQuantity<T> fromNBTWithQty(NBTTagCompound nbt);
    
    public abstract IStorageEventFactory<T> eventFactory();
    
    public abstract LogisticsService<T> service();
    
    /**
     * Units per tick throughput for the given level on storage
     * transport networks of this storage type.  Channel only
     * matter for fluid networks, which have fixed channels for
     * each type of transportable fluid. For other storage types
     * (power and items) this depends solely on level.
     */       
    public abstract long transportCapacity(CarrierLevel level, int channel);
    
    public static <V extends StorageType<V>> NBTTagCompound toNBTWithType(IResource<V> resource)
    {
        NBTTagCompound result = resource.storageType().toNBT(resource);
        Useful.saveEnumToTag(result, NBT_RESOURCE_TYPE, resource.storageType().enumType);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static <V extends StorageType<V>> IResource<V> fromNBTWithType(NBTTagCompound tag)
    {
        StorageType<?> sType = StorageType
                .fromEnum(Useful.safeEnumFromTag(tag, NBT_RESOURCE_TYPE, EnumStorageType.ITEM));
        return (IResource<V>) sType.fromNBT(tag);
    }
     
    public static <V extends StorageType<V>> String toCSVWithType(IResource<V> resource)
    {
        return resource.storageType().enumType.name() 
                + ","
                + resource.storageType().toCSV(resource);
    }
    
    @Nullable
    public static IResource<?> fromCSVWithType(String csv)
    {
        String[] split = csv.split(",");
        if(split.length < 2) return null;
        
        try
        {
            EnumStorageType sType = EnumStorageType.valueOf(split[0]);
            return StorageType.fromEnum(sType)
                    .fromCSV(csv.substring(split[0].length() + 1)); 
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    /**
     * Materials stored as item stacks. AbstractStorage managers for other storage types that can be encapsulated
     * as item stacks will use the item stack storage manager as a subsystem.
     */
    public static final StorageTypeStack ITEM = new StorageTypeStack();
    public static class StorageTypeStack extends StorageType<StorageTypeStack> 
    { 
        private StorageTypeStack()
        {
            super(EnumStorageType.ITEM, 
            new ItemResource(ItemStack.EMPTY.getItem(), ItemStack.EMPTY.getMetadata(), null, null));
        }
        
        @Override
        public Class<? extends StorageManager<StorageTypeStack>> domainCapability()
        {
            return ItemStorageManager.class;
        }
        
        /**
         * Note that this expects to get an ItemStack NBT, which
         * is what ItemResource serialization outputs.
         */
        @Override
        public @Nullable IResource<StorageTypeStack> fromNBT(NBTTagCompound nbt) 
        {
            if(nbt == null) return this.emptyResource;
            
            return ItemResource.fromStack(new ItemStack(nbt));
        }

        @Override
        public @Nullable NBTTagCompound toNBT(IResource<StorageTypeStack> resource)
        {
            return ((ItemResource)resource).sampleItemStack().serializeNBT();
        }

        @Override
        public @Nullable AbstractResourceWithQuantity<StorageTypeStack> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new ItemResourceWithQuantity(nbt);
        }

        @Override
        public IStorageEventFactory<StorageTypeStack> eventFactory()
        {
            return ItemStorageEvent.INSTANCE;
        }

        @Override
        public LogisticsService<StorageTypeStack> service()
        {
            return LogisticsService.ITEM_SERVICE;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            //TODO: make configurable
            switch(level)
            {
            case BOTTOM:
                return 1;
            case MIDDLE:
                return 4;
            case TOP:
                return 16;
                
            default:
                assert false: "Unhandled enum mapping";
                return 0;
            }
        }

        @Override
        public @Nullable IResource<StorageTypeStack> fromBytes(PacketBuffer pBuff)
        {
            try
            {
                return ItemResource.fromStack(pBuff.readItemStack());
            }
            catch (IOException e)
            {
                return this.emptyResource;
            }
        }

        @Override
        public void toBytes(IResource<StorageTypeStack> resource, PacketBuffer pBuff)
        {
            pBuff.writeItemStack(((ItemResource)resource).sampleItemStack());
        }

        @Override
        public String toCSV(IResource<StorageTypeStack> resource)
        {
            ItemStack stack = ((ItemResource)resource).sampleItemStack();
            String result = stack.getItem().getRegistryName().toString()
                    + "," + stack.getMetadata();
            
            if(stack.hasTagCompound())
            {
                result += "," + stack.getTagCompound().toString();
            }
            return result;
        }

        @Override
        public @Nullable IResource<StorageTypeStack> fromCSV(String csv)
        {
            String[] args = csv.split(",");
            
            if (args.length < 2) return null;
            
            try
            {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(args[0]));
                if(item == null) return null;
                int meta = Integer.parseInt(args[1]);
                ItemStack stack = new ItemStack(item, 1, meta);
                if(args.length == 3)
                {
                    stack.setTagCompound(JsonToNBT.getTagFromJson(args[2]));
                }
                return ItemResource.fromStack(stack);
            }
            catch(Exception e)
            {
                HardScience.INSTANCE.error("Unable to parse Item CSV", e);
            }
            return null;
        }
    }
            
    /**
     * Has to be encapsulated or stored in a tank or basin.
     */
    public static final StorageTypeFluid FLUID = new StorageTypeFluid();
    public static class StorageTypeFluid extends StorageType<StorageTypeFluid>
    {
        private StorageTypeFluid()
        {
            super(EnumStorageType.FLUID, new FluidResource(null, null));
        }
        
        @Override
        public Class<? extends StorageManager<StorageTypeFluid>> domainCapability()
        {
            return FluidStorageManager.class;
        }

        @Override
        public @Nullable IResource<StorageTypeFluid> fromNBT(NBTTagCompound nbt)
        {
            if(nbt == null) return this.emptyResource;
            return FluidResource.fromStack(FluidStack.loadFluidStackFromNBT(nbt));
        }

        @Override
        public @Nullable NBTTagCompound toNBT(IResource<StorageTypeFluid> resource)
        {
            NBTTagCompound tag = new NBTTagCompound();
            return ((FluidResource)resource).sampleFluidStack().writeToNBT(tag);
        }

        @Override
        public @Nullable AbstractResourceWithQuantity<StorageTypeFluid> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new FluidResourceWithQuantity(nbt);
        }

        @Override
        public IStorageEventFactory<StorageTypeFluid> eventFactory()
        {
            return FluidStorageEvent.INSTANCE;
        }

        @Override
        public LogisticsService<StorageTypeFluid> service()
        {
            return LogisticsService.FLUID_SERVICE;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            //TODO: make configurable
            switch(level)
            {
            case BOTTOM:
                return VolumeUnits.KILOLITER.nL;
            case MIDDLE:
                return VolumeUnits.KILOLITER.nL * 4;
            case TOP:
                return VolumeUnits.KILOLITER.nL * 16;
                
            default:
                assert false: "Unhandled enum mapping";
                return 0;
            }
        }

        @Override
        public @Nullable IResource<StorageTypeFluid> fromBytes(PacketBuffer pBuff)
        {
            Fluid f = FluidRegistry.getFluid(pBuff.readString(256));
            return new FluidResource(f, null);
        }

        @Override
        public void toBytes(IResource<StorageTypeFluid> resource, PacketBuffer pBuff)
        {
            pBuff.writeString(((FluidResource)resource).getFluid().getName());
        }

        @Override
        public String toCSV(IResource<StorageTypeFluid> resource)
        {
            Fluid fluid = ((FluidResource)resource).getFluid();
            return FluidRegistry.getFluidName(fluid);
        }

        @Override
        public @Nullable IResource<StorageTypeFluid> fromCSV(String csv)
        {
            Fluid fluid = FluidRegistry.getFluid(csv);
            return new FluidResource(fluid, null);
        }
    }
    
    
    /**
     * Must be stored in a battery.  Note that fuel is not counted as power 
     * because making power from fuel is a non-trivial production step. 
     */
    public static final StorageTypePower POWER = new StorageTypePower();
    public static class StorageTypePower extends StorageType<StorageTypePower>
    {
        private StorageTypePower()
        {
            super(EnumStorageType.POWER, new PowerResource("empty"));
        }

        @Override
        public Class<? extends StorageManager<StorageTypePower>> domainCapability()
        {
            return PowerStorageManager.class;
        }
        
        @Override
        public @Nullable IResource<StorageTypePower> fromNBT(NBTTagCompound nbt)
        {
            return PowerResource.JOULES;
        }

        @Override
        public @Nullable NBTTagCompound toNBT(IResource<StorageTypePower> resource)
        {
            return new NBTTagCompound();
        }

        @Override
        public @Nullable AbstractResourceWithQuantity<StorageTypePower> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new PowerResourceWithQuantity(nbt);
        }

        @Override
        public IStorageEventFactory<StorageTypePower> eventFactory()
        {
            return PowerStorageEvent.INSTANCE;
        }

        @Override
        public LogisticsService<StorageTypePower> service()
        {
            return LogisticsService.POWER_SERVICE;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            //TODO: make configurable
            switch(level)
            {
            case BOTTOM:
                return MachinePower.POWER_BUS_JOULES_PER_TICK;
            case MIDDLE:
                return MachinePower.POWER_BUS_JOULES_PER_TICK * 1000;
            case TOP:
                return MachinePower.POWER_BUS_JOULES_PER_TICK * 1000000;
                
            default:
                assert false: "Unhandled enum mapping";
                return 0;
            }
        }

        @Override
        public @Nullable IResource<StorageTypePower> fromBytes(PacketBuffer pBuff)
        {
            return PowerResource.JOULES;
        }

        @Override
        public void toBytes(IResource<StorageTypePower> resource, PacketBuffer pBuff)
        {
            // NOOP - always joules
        }

        @Override
        public String toCSV(IResource<StorageTypePower> resource)
        {
            return "joules";
        }

        @Override
        public @Nullable IResource<StorageTypePower> fromCSV(String csv)
        {
            return PowerResource.JOULES;
        }
    }
   
    /**
     * Must be stored in a battery.  Note that fuel is not counted as power 
     * because making power from fuel is a non-trivial production step. 
     */
    public static final StorageTypeBulk PRIVATE = new StorageTypeBulk();
    public static class StorageTypeBulk extends StorageType<StorageTypeBulk>
    {
        private StorageTypeBulk()
        {
            super(EnumStorageType.PRIVATE, ModBulkResources.FRESH_AIR);
        }
        
        @Override
        public Class<? extends StorageManager<StorageTypeBulk>> domainCapability()
        {
            // no capability for bulk
            return null;
        }
        
        @Override
        public @Nullable IResource<StorageTypeBulk> fromNBT(NBTTagCompound nbt)
        {
            return nbt != null && nbt.hasKey(NBT_RESOURCE_IDENTITY)
//                ? ModRegistries.bulkResourceRegistry.getValue(new ResourceLocation(nbt.getString(NBT_RESOURCE_IDENTITY)))
                    ? ModBulkResources.get(nbt.getString(NBT_RESOURCE_IDENTITY))
                    : this.emptyResource;
        }

        @Override
        public @Nullable NBTTagCompound toNBT(IResource<StorageTypeBulk> resource)
        {
            NBTTagCompound result = new NBTTagCompound();
            result.setString(NBT_RESOURCE_IDENTITY, ((BulkResource)resource).systemName());
            return result;
        }

        @Override
        public @Nullable AbstractResourceWithQuantity<StorageTypeBulk> fromNBTWithQty(NBTTagCompound nbt)
        {
            return new BulkResourceWithQuantity(nbt);
        }

        @Override
        public IStorageEventFactory<StorageTypeBulk> eventFactory()
        {
            return null;
        }

        @Override
        public LogisticsService<StorageTypeBulk> service()
        {
            return null;
        }

        @Override
        public long transportCapacity(CarrierLevel level, int channel)
        {
            return 0;
        }

        @Override
        public @Nullable IResource<StorageTypeBulk> fromBytes(PacketBuffer pBuff)
        {
            return ModBulkResources.get(pBuff.readString(256));
        }

        @Override
        public void toBytes(IResource<StorageTypeBulk> resource, PacketBuffer pBuff)
        {
            pBuff.writeString(((BulkResource)resource).systemName());
        }

        @Override
        public String toCSV(IResource<StorageTypeBulk> resource)
        {
            return "bulk_unsupported";
        }

        @Override
        public @Nullable IResource<StorageTypeBulk> fromCSV(String csv)
        {
            return null;
        }
    }
}
