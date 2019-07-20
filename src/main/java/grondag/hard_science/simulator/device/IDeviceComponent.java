package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.management.ITransportManager;

public interface IDeviceComponent extends IDomainMember
{
    public IDevice device();
    
    /**
     * Shorthand for {@link #device()#getDomain()}
     */
    @Override
    public default @Nullable IDomain getDomain()
    {
        return this.device().getDomain();
    }
    
    /**
     * Implement and call from {@link IDevice#onConnect()} if this component 
     * needs to do something when connection happens.
     */
    default void onConnect() {}


    /**
     * Implement and call from {@link IDevice#onDisconnect()} if this component 
     * needs to do something when disconnect happens.
     */
    default void onDisconnect() {}

    /**
     * Shorthand for {@link #device()#isConnected()}
     */
    public default boolean isConnected()
    {
        return this.device().isConnected();
    }
    
    /**
     * Shorthand for {@link #device()#setDirty()}
     */
    public default void setDirty()
    {
        this.device().setDirty();
    }
    
    /** Shorthand for {@link #device()#isOn()} */
    public default boolean isOn()
    {
        return this.device().isOn();
    }
    
    /** Shorthand for {@link #device()#powerTransport() */
    public default ITransportManager<StorageTypePower> powerTransport() { return this.device().powerTransport(); }

    /** Shorthand for {@link #device()#itemTransport() */
    public default ITransportManager<StorageTypeStack> itemTransport() { return this.device().itemTransport(); }

    /** Shorthand for {@link #device()#fluidTransport() */
    public default ITransportManager<StorageTypeFluid> fluidTransport() { return this.device().fluidTransport(); }

}
