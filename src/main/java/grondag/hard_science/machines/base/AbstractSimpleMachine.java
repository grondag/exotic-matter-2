package grondag.hard_science.machines.base;

import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.device.blocks.SimpleBlockHandler;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.endpoint.IPortLayout;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import grondag.hard_science.simulator.transport.management.ITransportManager;
import grondag.hard_science.simulator.transport.management.SimpleTransportManager;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Base class for single-block machines.
 *
 */
public abstract class AbstractSimpleMachine extends AbstractMachine
{
    private static final String NBT_PORT_LAYOUT = NBTDictionary.claim("portLayout");
    
    private IPortLayout portLayout;
    
    public void setPortLayout(IPortLayout portLayout)
    {
        assert !this.isConnected() : "Machine port layout changed while connected.";
        this.portLayout = portLayout;
    }
    
    @Override
    protected ITransportManager<StorageTypeStack> createItemTransportManager()
    {
        return new SimpleTransportManager<StorageTypeStack>(this, StorageType.ITEM);
    }

    @Override
    protected ITransportManager<StorageTypeFluid> createFluidTransportManager()
    {
        return new SimpleTransportManager<StorageTypeFluid>(this, StorageType.FLUID);
    }
    
    @Override
    protected ITransportManager<StorageTypePower> createPowerTransportManager()
    {
        return new SimpleTransportManager<StorageTypePower>(this, StorageType.POWER);
    }
    
    @Override
    protected IDeviceBlockManager createBlockManager()
    {
        SimpleBlockHandler result = new SimpleBlockHandler(this, this.portLayout);
        return result;
    }
    
    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        super.serializeNBT(tag);
        tag.setTag(NBT_PORT_LAYOUT, PortLayout.toNBT(this.portLayout));
    }
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        super.deserializeNBT(tag);
        this.portLayout = PortLayout.fromNBT(tag.getCompoundTag(NBT_PORT_LAYOUT));
    }
}
