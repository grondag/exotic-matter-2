package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.exotic_matter.concurrency.PerformanceCollector;
import grondag.exotic_matter.concurrency.PerformanceCounter;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.ISimulationTickable;
import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.persistence.ISimulationTopNode;
import grondag.exotic_matter.simulator.persistence.SimulationTopNode;
import grondag.hard_science.Configurator;
import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.device.blocks.DeviceWorldManager;
import grondag.hard_science.simulator.device.blocks.IDeviceBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class DeviceManager extends SimulationTopNode implements ISimulationTickable
{
    ///////////////////////////////////////////////////////////
    //  STATIC MEMBERS
    ///////////////////////////////////////////////////////////
    
    private static final String NBT_DEVICE_MANAGER_DEVICES = NBTDictionary.claim("dmDevices");
    private static final String NBT_DEVICE_MANAGER_SELF = NBTDictionary.claim("devMgr");
    private static final String NBT_DEVICE_MANAGER_DEVICE_TYPE = NBTDictionary.claim("dmDevType");
    
    /**
     * A bit ugly but covenient.  Set to null when any 
     * instance is created, retrieved lazily from Simulator.
     */
    private static @Nullable DeviceManager instance;
    
    public static DeviceManager instance()
    {
        DeviceManager dm = instance;
        if(dm == null)
        {
            dm = Simulator.instance().getNode(DeviceManager.class);
            instance = dm;
        }
        return dm;
    }
    
    private static final RegistryNamespaced < ResourceLocation, Class <? extends IDevice >> REGISTRY = new RegistryNamespaced < ResourceLocation, Class <? extends IDevice >> ();
    
    public static void register(String id, Class <? extends IDevice > clazz)
    {
        REGISTRY.putObject(new ResourceLocation(id), clazz);
    }
    
    @Nullable
    public static ResourceLocation getKey(Class <? extends IDevice > clazz)
    {
        return REGISTRY.getNameForObject(clazz);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nullable
    public static IDevice create(NBTTagCompound compound)
    {
        IDevice device = null;
        String s = compound.getString(NBT_DEVICE_MANAGER_DEVICE_TYPE);
        Class <? extends IDevice > oclass = null;

        try
        {
            oclass = (Class)REGISTRY.getObject(new ResourceLocation(s));

            if (oclass != null)
            {
                device = oclass.newInstance();
            }
        }
        catch (Throwable throwable1)
        {
            HardScience.INSTANCE.error("Failed to create device {}", s, throwable1);
        }

        if (device != null)
        {
            try
            {
                device.deserializeNBT(compound);
            }
            catch (Throwable throwable)
            {
                HardScience.INSTANCE.error("Failed to load data for device {}", s, throwable);
                device = null;
            }
        }
        else
        {
            HardScience.INSTANCE.warn("Skipping device with id {}", (Object)s);
        }

        return device;
    }
    
    public static IDevice getDevice(int deviceId)
    {
        return instance().devices.get(deviceId);
    }
    
    public static IDevice getDevice(World world, BlockPos pos)
    {
        IDeviceBlock block =  blockManager().getBlockDelegate(world, pos);
        return block == null ? null : block.device();
    }
    
    /**
     * Records device for access and persistence.</p>
     * Note that adding a device does NOT connect it.
     * That is done either after deserialization (for existing devices)
     * or after block placement (for new devices)
     */
    public static void addDevice(IDevice device)
    {
        instance().addDeviceInconveniently(device);
    }
    
    public static void removeDevice(IDevice device)
    {
        instance().removeDeviceInconveniently(device);
    }
    
    public static DeviceWorldManager blockManager()
    {
        return instance().deviceBlocks;
    }
    
    ///////////////////////////////////////////////////////////
    //  INSTANCE MEMBERS
    ///////////////////////////////////////////////////////////
    
    private final Int2ObjectOpenHashMap<IDevice> devices =
            new Int2ObjectOpenHashMap<IDevice>();
    
    private final DeviceWorldManager deviceBlocks = new DeviceWorldManager();
    
    private boolean isDirty = false;
    
    private @Nullable IDevice[] offTickDevices = null;
    private @Nullable IDevice[] onTickDevices = null;
    
    public final PerformanceCollector perfCollectorOnTick = new PerformanceCollector("Machine Simulator On tick");
    public final PerformanceCollector perfCollectorOffTick = new PerformanceCollector("Machine Simulator Off tick");
    
    private final PerformanceCounter perfCounterOffTick = PerformanceCounter.create(Configurator.MACHINES.enablePerformanceLogging, "Machine off-stick update processing", perfCollectorOffTick);
    private final PerformanceCounter perfCounterOnTick = PerformanceCounter.create(Configurator.MACHINES.enablePerformanceLogging, "Machine on-stick update processing", perfCollectorOnTick);
    
    public DeviceManager()
    {
        // force refresh of singleton access method
        instance = null;
    }
    
    @Override
    public void afterDeserialization()
    {
        for(IDevice device : this.devices.values())
        {
            device.onConnect();
        }
    }
    
    public void addDeviceInconveniently(IDevice device)
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager.addDevice: " + device.getId());
        
        assert this.devices.put(device.getId(), device) == null
                : "Duplicate device registration.";
        this.onTickDevices = null;
        this.offTickDevices = null;
        this.isDirty = true;
        
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager device count = " + this.devices.size());

    }
    
    public void removeDeviceInconveniently(IDevice device)
    {
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager.removeDevice: " + device.getId());

        IDevice oldDevice = this.devices.remove(device.getId());
        if(oldDevice == device)
        {
            device.onDisconnect();
        }
        else if(oldDevice == null)
        {
            device.onDisconnect();
            assert false : "Removal request device mismatch";
        }
        else
        {
            assert false : "Removal request for missing device.";
        }        
        this.isDirty = true;
        this.onTickDevices = null;
        this.offTickDevices = null;
        
        if(Configurator.logDeviceChanges)
            HardScience.INSTANCE.info("DeviceManager device count = " + this.devices.size());
    }
    
    @Override
    public boolean isSaveDirty()
    {
        return this.isDirty;
    }

    @Override
    public void setSaveDirty(boolean isDirty)
    {
        this.isDirty = isDirty;
    }

    public void clear()
    {
        this.devices.clear();
        this.deviceBlocks.clear();
    }
    
    /**
     * Called by simulator at shutdown
     */
    @Override
    public void unload()
    {
        this.clear();
    }
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        if(tag == null) return;
        
        NBTTagList nbtDevices = tag.getTagList(NBT_DEVICE_MANAGER_DEVICES, 10);
        if( nbtDevices != null && !nbtDevices.hasNoTags())
        {
            for (int i = 0; i < nbtDevices.tagCount(); ++i)
            {
                IDevice device = create(nbtDevices.getCompoundTagAt(i));
                if(device != null) this.devices.put(device.getId(), device);
            }   
        }
        this.onTickDevices = null;
        this.offTickDevices = null;
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        NBTTagList nbtDevices = new NBTTagList();
        
        if(!devices.isEmpty())
        {
            for (IDevice device : this.devices.values())
            {
                if(device.doesPersist())
                {
                    ResourceLocation resourcelocation = REGISTRY.getNameForObject(device.getClass());
                    
                    if (resourcelocation == null)
                    {
                        HardScience.INSTANCE.error("Error saving device state because " + device.getClass() + " is missing a mapping");
                    }
                    else
                    {
                        NBTTagCompound deviceTag = device.serializeNBT();
                        deviceTag.setString(NBT_DEVICE_MANAGER_DEVICE_TYPE, resourcelocation.toString());
                        nbtDevices.appendTag(deviceTag);
                    }
                }
            }
        }
        tag.setTag(NBT_DEVICE_MANAGER_DEVICES, nbtDevices);        
    }

    @Override
    public String tagName()
    {
        return NBT_DEVICE_MANAGER_SELF;
    }

    @Override
    public void doOnTick()
    {
        IDevice[] devs = this.onTickDevices;
        if(devs == null)
        {
            devs = devices.size() == 0
                ? new IDevice[0]
                : devices.values().stream().filter(d -> d.doesUpdateOnTick()).toArray(IDevice[]::new);
            this.onTickDevices = devs;
        }
        
        if(devs.length > 0)
        {
            this.perfCounterOnTick.startRun();
            this.perfCounterOnTick.addCount(devs.length);
            for(IDevice d : devs) d.doOnTick();
            this.perfCounterOnTick.endRun();
        }
    }

    @Override
    public void doOffTick()
    {
        IDevice[] devs = this.offTickDevices;
        if(devs == null)
        {
            devs = devices.size() == 0
                ? new IDevice[0]
                : devices.values().stream().filter(d -> d.doesUpdateOffTick()).toArray(IDevice[]::new);
            this.offTickDevices = devs;
        }
        
        if(devs.length > 0)
        {
            this.perfCounterOffTick.startRun();
            this.perfCounterOffTick.addCount(devs.length);
            Simulator.SCATTER_GATHER_POOL.completeTask(devs, d -> d.doOffTick());
            this.perfCounterOffTick.endRun();
        }
    }

}
